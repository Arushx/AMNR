package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Uses a fake HTTP client (an interceptor that answers every request), so nothing is downloaded. */
class FinbertModelFilesTest {

  @TempDir Path dir;

  private final List<String> requested = new ArrayList<>();

  private OkHttpClient fakeHttp(int status, String body) {
    return new OkHttpClient.Builder()
        .addInterceptor(chain -> {
          requested.add(chain.request().url().toString());
          return new Response.Builder()
              .request(chain.request())
              .protocol(Protocol.HTTP_1_1)
              .code(status)
              .message("fake")
              .body(ResponseBody.create(body, MediaType.get("application/octet-stream")))
              .build();
        })
        .build();
  }

  @Test
  void emptyFolderIsNotComplete() {
    assertFalse(new FinbertModelFiles(dir).isComplete());
  }

  @Test
  void wrongChecksumIsRejectedAndNothingIsKept() {
    FinbertModelFiles files = new FinbertModelFiles(dir, "https://example.test", fakeHttp(200,
        "not the real file"));

    IOException e = assertThrows(IOException.class, files::downloadMissing);

    assertEquals("Downloaded onnx/model_quantized.onnx has the wrong checksum", e.getMessage());
    assertFalse(Files.exists(files.modelFile()));
    assertFalse(Files.exists(dir.resolve("model_quantized.onnx.part")));
  }

  @Test
  void httpErrorIsReported() {
    FinbertModelFiles files = new FinbertModelFiles(dir, "https://example.test", fakeHttp(404,
        ""));

    IOException e = assertThrows(IOException.class, files::downloadMissing);

    assertEquals("Could not download onnx/model_quantized.onnx (HTTP 404)", e.getMessage());
  }

  @Test
  void downloadsFromThePinnedCommit() {
    FinbertModelFiles files = new FinbertModelFiles(dir, "https://example.test", fakeHttp(404,
        ""));

    assertThrows(IOException.class, files::downloadMissing);

    assertEquals(
        "https://example.test/Xenova/finbert/resolve/" + FinbertModelFiles.REVISION
            + "/onnx/model_quantized.onnx",
        requested.get(0));
  }

  @Test
  void filesWithTheRightSizeAreNotDownloadedAgain() throws IOException {
    for (FinbertModelFiles.ModelFile file : FinbertModelFiles.FILES) {
      try (var out = Files.newOutputStream(dir.resolve(file.localName))) {
        out.write(new byte[0]);
      }
      try (var raf = new java.io.RandomAccessFile(dir.resolve(file.localName).toFile(), "rw")) {
        raf.setLength(file.size);
      }
    }
    FinbertModelFiles files = new FinbertModelFiles(dir, "https://example.test", fakeHttp(500,
        ""));

    assertTrue(files.isComplete());
    files.downloadMissing();
    assertTrue(requested.isEmpty());
  }

  @Test
  void checksumIsSha256() throws IOException {
    String checksum =
        FinbertModelFiles.saveWithChecksum(
            new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), dir.resolve("x"));

    assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", checksum);
    assertEquals("abc", Files.readString(dir.resolve("x")));
  }

  @Test
  void configuredFolderWinsOverTheDefault() {
    assertEquals(java.nio.file.Paths.get("D:/models/finbert"),
        FinbertModelFiles.directoryOrDefault(" D:/models/finbert "));
    assertEquals(FinbertModelFiles.defaultDirectory(), FinbertModelFiles.directoryOrDefault(""));
    assertEquals(FinbertModelFiles.defaultDirectory(), FinbertModelFiles.directoryOrDefault(null));
  }

  @Test
  void defaultFolderIsInTheUsersHome() {
    assertTrue(FinbertModelFiles.defaultDirectory().toString().contains(".amnr"));
    assertTrue(FinbertModelFiles.defaultDirectory().endsWith("finbert-8f269ab"));
  }
}

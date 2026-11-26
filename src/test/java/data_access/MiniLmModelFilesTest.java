package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

class MiniLmModelFilesTest {

  private static final Path REAL_FOLDER = Path.of("data", "models", "all-MiniLM-L6-v2");

  @Test
  void computesSha256Fingerprint(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("abc.txt");
    Files.writeString(file, "abc", StandardCharsets.UTF_8);

    // The standard SHA-256 test value for "abc".
    assertEquals(
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
        MiniLmModelFiles.sha256Of(file));
  }

  @Test
  @EnabledIf("vocabDownloaded")
  void reusesAnIntactLocalFileWithoutDownloading(@TempDir Path dir) throws IOException {
    Files.copy(REAL_FOLDER.resolve(MiniLmModelFiles.VOCAB_FILE),
        dir.resolve(MiniLmModelFiles.VOCAB_FILE));

    Path vocab = new MiniLmModelFiles(dir).vocabFile();

    assertEquals(dir.resolve(MiniLmModelFiles.VOCAB_FILE), vocab);
    assertEquals(MiniLmModelFiles.VOCAB_SHA256, MiniLmModelFiles.sha256Of(vocab));
  }

  static boolean vocabDownloaded() {
    return Files.exists(REAL_FOLDER.resolve(MiniLmModelFiles.VOCAB_FILE));
  }
}

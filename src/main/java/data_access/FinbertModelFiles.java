package data_access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * The FinBERT model file on this computer, downloaded once from Hugging Face.
 *
 * <p>Only the model itself ({@value #MODEL_NAME}, 111 MB) is downloaded: it is too big for GitHub.
 * The two small files that go with it, {@code vocab.txt} and {@code config.json}, are bundled in
 * {@code src/main/resources/finbert/}.
 *
 * <p>The model comes from the ONNX copy of ProsusAI/finbert at <a
 * href="https://huggingface.co/Xenova/finbert">huggingface.co/Xenova/finbert</a>, pinned to one
 * commit so the model can't change under us. Each download is checked against a fixed SHA-256
 * checksum before it is used, and is saved under a temporary name first, so an interrupted
 * download is never mistaken for a good file. FinBERT is licensed CC BY-NC-SA 4.0 (non-commercial
 * use, with credit).
 */
public class FinbertModelFiles {

  static final String REPOSITORY = "Xenova/finbert";
  static final String REVISION = "8f269abebfdd9009d7d9b5e96af7e5c6bfe50b20";
  static final String BASE_URL = "https://huggingface.co";

  static final String MODEL_NAME = "model_quantized.onnx";
  static final ModelFile MODEL =
      new ModelFile(
          "onnx/model_quantized.onnx", MODEL_NAME, 110_717_965,
          "56cd19c7068ca5670c326816845bb7d7edda508b68a228697a1460d0e821ce76");
  static final List<ModelFile> FILES = List.of(MODEL);

  private final Path directory;
  private final String baseUrl;
  private final OkHttpClient httpClient;

  /**
   * Uses the given folder, downloading from Hugging Face.
   *
   * @param directory where the files are, or will be saved
   */
  public FinbertModelFiles(Path directory) {
    this(directory, BASE_URL,
        new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build());
  }

  FinbertModelFiles(Path directory, String baseUrl, OkHttpClient httpClient) {
    this.directory = directory;
    this.baseUrl = baseUrl;
    this.httpClient = httpClient;
  }

  /**
   * Returns the usual folder: {@code ~/.amnr/models/finbert-<commit>}.
   *
   * @return the folder path
   */
  public static Path defaultDirectory() {
    return Paths.get(System.getProperty("user.home"), ".amnr", "models",
        "finbert-" + REVISION.substring(0, 7));
  }

  /**
   * Returns the folder to use: the one given (e.g. from the FINBERT_MODEL_DIR environment
   * variable, read by the caller) if it isn't blank, otherwise {@link #defaultDirectory()}.
   *
   * @param configured a folder path, or null or blank for the default
   * @return the folder path
   */
  public static Path directoryOrDefault(String configured) {
    return configured == null || configured.isBlank()
        ? defaultDirectory() : Paths.get(configured.strip());
  }

  public Path getDirectory() {
    return directory;
  }

  /**
   * Returns the path of the ONNX model file.
   *
   * @return the file path
   */
  public Path modelFile() {
    return directory.resolve(MODEL.localName);
  }

  /**
   * Tells whether the model file is present with the right size. The checksum is checked when it
   * is downloaded, so this quick check is enough at startup.
   *
   * @return true if the model can be loaded without downloading
   */
  public boolean isComplete() {
    for (ModelFile file : FILES) {
      final Path path = directory.resolve(file.localName);
      try {
        if (!Files.isRegularFile(path) || Files.size(path) != file.size) {
          return false;
        }
      } catch (IOException e) {
        return false;
      }
    }
    return true;
  }

  /**
   * Downloads the model if it is missing or incomplete (about 111 MB, the first time only).
   *
   * @throws IOException if a download fails or a file's checksum is wrong
   */
  public void downloadMissing() throws IOException {
    Files.createDirectories(directory);
    for (ModelFile file : FILES) {
      final Path target = directory.resolve(file.localName);
      if (Files.isRegularFile(target) && Files.size(target) == file.size) {
        continue;
      }
      download(file, target);
    }
  }

  private void download(ModelFile file, Path target) throws IOException {
    final String url =
        baseUrl + "/" + REPOSITORY + "/resolve/" + REVISION + "/" + file.remotePath;
    final Path temporary = target.resolveSibling(target.getFileName() + ".part");
    final Request request = new Request.Builder().url(url).build();
    try (Response response = httpClient.newCall(request).execute()) {
      final ResponseBody body = response.body();
      if (!response.isSuccessful() || body == null) {
        throw new IOException(
            "Could not download " + file.remotePath + " (HTTP " + response.code() + ")");
      }
      final String checksum = saveWithChecksum(body.byteStream(), temporary);
      if (!checksum.equals(file.sha256)) {
        throw new IOException("Downloaded " + file.remotePath + " has the wrong checksum");
      }
      Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  /**
   * Copies a stream to a file and returns its SHA-256 checksum in hex.
   *
   * @param in the data
   * @param target the file to write
   * @return the checksum
   * @throws IOException if writing fails
   */
  static String saveWithChecksum(InputStream in, Path target) throws IOException {
    final MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is always available in Java", e);
    }
    try (InputStream hashing = new DigestInputStream(in, digest);
        OutputStream out = Files.newOutputStream(target)) {
      hashing.transferTo(out);
    }
    final StringBuilder hex = new StringBuilder();
    for (byte b : digest.digest()) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }

  /** One file of the model: where it is on Hugging Face, its local name, size and checksum. */
  static final class ModelFile {
    final String remotePath;
    final String localName;
    final long size;
    final String sha256;

    ModelFile(String remotePath, String localName, long size, String sha256) {
      this.remotePath = remotePath;
      this.localName = localName;
      this.size = size;
      this.sha256 = sha256;
    }
  }
}

package data_access;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Provides the files of the all-MiniLM-L6-v2 embedding model, downloading them once if needed.
 *
 * <p>The model (23 MB) is too big to keep in git, so the first time semantic search runs it is
 * downloaded from Hugging Face (public, no key needed) into a local folder, e.g.
 * {@code data/models/all-MiniLM-L6-v2}. Every file is checked against a known SHA-256 fingerprint,
 * so a corrupted or tampered download is rejected instead of being run.
 *
 * <p>The URLs point at one fixed version (commit) of the model, so everyone gets identical files.
 */
public class MiniLmModelFiles {

  /** The model version we use: a fixed commit of sentence-transformers/all-MiniLM-L6-v2. */
  private static final String BASE_URL =
      "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/"
          + "1110a243fdf4706b3f48f1d95db1a4f5529b4d41/";

  static final String MODEL_FILE = "model_qint8.onnx";
  static final String VOCAB_FILE = "vocab.txt";

  private static final String MODEL_URL = BASE_URL + "onnx/model_qint8_arm64.onnx";
  private static final String VOCAB_URL = BASE_URL + "vocab.txt";
  static final String MODEL_SHA256 =
      "4278337fd0ff3c68bfb6291042cad8ab363e1d9fbc43dcb499fe91c871902474";
  static final String VOCAB_SHA256 =
      "07eced375cec144d27c900241f3e339478dec958f92fddbc551f295c992038a3";

  private final Path folder;
  private final OkHttpClient httpClient = new OkHttpClient();

  /**
   * Creates the provider.
   *
   * @param folder where the files are kept, e.g. data/models/all-MiniLM-L6-v2
   */
  public MiniLmModelFiles(Path folder) {
    this.folder = folder;
  }

  /**
   * Returns the model file (ONNX format), downloading it first if it isn't there.
   *
   * @return the path to the model
   * @throws IOException if it can't be downloaded or doesn't match its fingerprint
   */
  public Path modelFile() throws IOException {
    return ensure(MODEL_FILE, MODEL_URL, MODEL_SHA256);
  }

  /**
   * Returns the tokenizer vocabulary, downloading it first if it isn't there.
   *
   * @return the path to vocab.txt
   * @throws IOException if it can't be downloaded or doesn't match its fingerprint
   */
  public Path vocabFile() throws IOException {
    return ensure(VOCAB_FILE, VOCAB_URL, VOCAB_SHA256);
  }

  /** Returns the local file if it is intact; otherwise downloads, checks and saves it. */
  private Path ensure(String name, String url, String sha256) throws IOException {
    final Path file = folder.resolve(name);
    if (Files.exists(file) && sha256Of(file).equals(sha256)) {
      return file;
    }
    Files.createDirectories(folder);
    // Download next to the target first, so a half-finished download never looks complete.
    final Path partial = folder.resolve(name + ".part");
    download(url, partial);
    if (!sha256Of(partial).equals(sha256)) {
      Files.deleteIfExists(partial);
      throw new IOException("Downloaded " + name + " does not match its expected fingerprint");
    }
    Files.move(partial, file, StandardCopyOption.REPLACE_EXISTING);
    return file;
  }

  private void download(String url, Path target) throws IOException {
    final Request request = new Request.Builder().url(url).build();
    try (Response response = httpClient.newCall(request).execute()) {
      final ResponseBody body = response.body();
      if (!response.isSuccessful() || body == null) {
        throw new IOException("Could not download " + url + " (HTTP " + response.code() + ")");
      }
      try (InputStream in = body.byteStream()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  /**
   * Computes a file's SHA-256 fingerprint as lowercase hex.
   *
   * @param file the file
   * @return 64 hex characters
   * @throws IOException if the file can't be read
   */
  static String sha256Of(Path file) throws IOException {
    final MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is always available in Java", e);
    }
    try (InputStream in = Files.newInputStream(file)) {
      final byte[] buffer = new byte[8192];
      int read;
      while ((read = in.read(buffer)) != -1) {
        digest.update(buffer, 0, read);
      }
    }
    final StringBuilder hex = new StringBuilder();
    for (byte b : digest.digest()) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }
}

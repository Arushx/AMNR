package data_access;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import use_case.semantic_search.ProfileVectorDataAccessInterface;

/**
 * Saves the stock profile embeddings in a binary file, e.g. {@code data/profile_vectors.bin}.
 *
 * <p>This is the semantic search's persistence: about 6,000 vectors of 384 numbers each (about
 * 9 MB) are computed once and read back in well under a second on later runs. File layout:
 *
 * <pre>
 *   int    format version
 *   String key (model + data fingerprint)
 *   int    number of vectors, int numbers per vector
 *   then per vector: String symbol, float[] numbers
 * </pre>
 */
public class FileProfileVectorDataAccessObject implements ProfileVectorDataAccessInterface {

  private static final int FORMAT_VERSION = 1;

  private final Path file;

  /**
   * Creates the data access object.
   *
   * @param file where the vectors are saved
   */
  public FileProfileVectorDataAccessObject(Path file) {
    this.file = file;
  }

  @Override
  public Map<String, float[]> loadVectors(String key) {
    if (!Files.exists(file)) {
      return Collections.emptyMap();
    }
    try (DataInputStream in =
        new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
      if (in.readInt() != FORMAT_VERSION || !in.readUTF().equals(key)) {
        return Collections.emptyMap(); // made by another model or from other data: recompute
      }
      final int count = in.readInt();
      final int dimensions = in.readInt();
      final Map<String, float[]> vectors = new HashMap<>();
      for (int i = 0; i < count; i++) {
        final String symbol = in.readUTF();
        final float[] vector = new float[dimensions];
        for (int d = 0; d < dimensions; d++) {
          vector[d] = in.readFloat();
        }
        vectors.put(symbol, vector);
      }
      return vectors;
    } catch (IOException e) {
      return Collections.emptyMap(); // damaged file: treat it as missing
    }
  }

  @Override
  public void saveVectors(String key, Map<String, float[]> vectors) {
    final int dimensions = vectors.isEmpty() ? 0 : vectors.values().iterator().next().length;
    try {
      if (file.getParent() != null) {
        Files.createDirectories(file.getParent());
      }
      // Write to a temporary file first, so a crash never leaves a half-written file behind.
      final Path partial = file.resolveSibling(file.getFileName() + ".part");
      try (DataOutputStream out =
          new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(partial)))) {
        out.writeInt(FORMAT_VERSION);
        out.writeUTF(key);
        out.writeInt(vectors.size());
        out.writeInt(dimensions);
        for (Map.Entry<String, float[]> entry : vectors.entrySet()) {
          out.writeUTF(entry.getKey());
          for (float value : entry.getValue()) {
            out.writeFloat(value);
          }
        }
      }
      Files.move(partial, file, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      // Only a cache: if it can't be saved, the vectors are computed again next run.
    }
  }
}

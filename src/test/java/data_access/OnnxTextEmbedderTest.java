package data_access;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import use_case.semantic_search.EmbeddingUnavailableException;

class OnnxTextEmbedderTest {

  private static final Path MODEL_FOLDER = Path.of("data", "models", "all-MiniLM-L6-v2");

  @Test
  void meanPoolAveragesEachDimension() {
    float[][] tokens = {{1f, 2f}, {3f, 6f}};

    assertArrayEquals(new float[] {2f, 4f}, OnnxTextEmbedder.meanPool(tokens), 1e-6f);
  }

  @Test
  void normaliseScalesToLengthOne() {
    assertArrayEquals(
        new float[] {0.6f, 0.8f}, OnnxTextEmbedder.normalise(new float[] {3f, 4f}), 1e-6f);
  }

  @Test
  void normaliseLeavesZeroVectorAlone() {
    assertArrayEquals(new float[] {0f, 0f}, OnnxTextEmbedder.normalise(new float[] {0f, 0f}));
  }

  /** Runs the real model, only on machines that already have the model files. */
  @Test
  @EnabledIf("modelDownloaded")
  void realModelPutsOilQueryNearOilCompany() throws EmbeddingUnavailableException {
    OnnxTextEmbedder embedder = new OnnxTextEmbedder(new MiniLmModelFiles(MODEL_FOLDER));

    float[] query = embedder.embed("best selling oil stocks");
    float[] exxon = embedder.embed(
        "Exxon Mobil Corporation. Energy, Oil, Gas & Fuels. Explores for and produces crude oil "
            + "and natural gas.");
    float[] apple = embedder.embed(
        "Apple Inc. Information Technology, Computers & Hardware. Designs smartphones and "
            + "personal computers.");

    assertEquals(384, query.length);
    assertEquals(1.0, dot(query, query), 1e-3);
    assertTrue(dot(query, exxon) > dot(query, apple));
  }

  static boolean modelDownloaded() {
    return Files.exists(MODEL_FOLDER.resolve(MiniLmModelFiles.MODEL_FILE))
        && Files.exists(MODEL_FOLDER.resolve(MiniLmModelFiles.VOCAB_FILE));
  }

  private static double dot(float[] a, float[] b) {
    double sum = 0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i] * b[i];
    }
    return sum;
  }
}

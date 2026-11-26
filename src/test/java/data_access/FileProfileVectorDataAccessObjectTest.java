package data_access;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileProfileVectorDataAccessObjectTest {

  @Test
  void savedVectorsLoadBackUnderTheSameKey(@TempDir Path dir) {
    Path file = dir.resolve("sub").resolve("vectors.bin");
    new FileProfileVectorDataAccessObject(file)
        .saveVectors("model:2", Map.of("XOM", new float[] {0.6f, 0.8f}, "AAPL", new float[] {1f, 0f}));

    Map<String, float[]> loaded = new FileProfileVectorDataAccessObject(file).loadVectors("model:2");

    assertEquals(2, loaded.size());
    assertArrayEquals(new float[] {0.6f, 0.8f}, loaded.get("XOM"));
    assertArrayEquals(new float[] {1f, 0f}, loaded.get("AAPL"));
  }

  @Test
  void differentKeyLoadsNothing(@TempDir Path dir) {
    Path file = dir.resolve("vectors.bin");
    FileProfileVectorDataAccessObject store = new FileProfileVectorDataAccessObject(file);
    store.saveVectors("model-a", Map.of("XOM", new float[] {1f}));

    assertTrue(store.loadVectors("model-b").isEmpty());
  }

  @Test
  void missingFileLoadsNothing(@TempDir Path dir) {
    assertTrue(
        new FileProfileVectorDataAccessObject(dir.resolve("none.bin")).loadVectors("k").isEmpty());
  }

  @Test
  void damagedFileLoadsNothing(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("vectors.bin");
    Files.write(file, new byte[] {0, 0, 0, 1, 0});

    assertTrue(new FileProfileVectorDataAccessObject(file).loadVectors("k").isEmpty());
  }
}

package use_case.semantic_search;

import java.util.HashMap;
import java.util.Map;

/** Keeps saved vectors in memory instead of a file, and counts saves. */
class InMemoryVectorStore implements ProfileVectorDataAccessInterface {

  String savedKey;
  Map<String, float[]> saved = new HashMap<>();
  int saves;

  @Override
  public Map<String, float[]> loadVectors(String key) {
    return key.equals(savedKey) ? saved : Map.of();
  }

  @Override
  public void saveVectors(String key, Map<String, float[]> vectors) {
    savedKey = key;
    saved = new HashMap<>(vectors);
    saves++;
  }
}

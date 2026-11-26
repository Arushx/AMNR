package use_case.semantic_search;

import java.util.Map;

/**
 * Saves and loads the embeddings of the stock profiles, so they are only computed once.
 *
 * <p>Embedding all profiles takes about 15 seconds; loading saved ones takes a fraction of that.
 * This is a cache: if loading or saving fails, the vectors are simply computed again.
 */
public interface ProfileVectorDataAccessInterface {

  /**
   * Loads saved vectors.
   *
   * @param key identifies the model and data they were made from; vectors saved under a different
   *     key are ignored
   * @return each stock symbol mapped to its vector, or an empty map if none are saved for this key
   */
  Map<String, float[]> loadVectors(String key);

  /**
   * Saves vectors, replacing any saved before.
   *
   * @param key identifies the model and data they were made from
   * @param vectors each stock symbol mapped to its vector
   */
  void saveVectors(String key, Map<String, float[]> vectors);
}

package use_case.semantic_search;

import entity.StockProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scores profiles by meaning: the cosine similarity between the topic's embedding and each
 * profile's embedding. "oil" scores high against "explores for and produces crude oil" even
 * though the words differ.
 *
 * <p>Profile embeddings are computed once, saved through a
 * {@link ProfileVectorDataAccessInterface}, and kept in memory. Only the topic is embedded on each
 * search.
 */
public class SemanticProfileMatcher implements ProfileMatcher {

  private final TextEmbeddingGateway embedder;
  private final ProfileVectorDataAccessInterface vectorStore;
  private String loadedKey;
  private Map<String, float[]> vectors = new HashMap<>();

  /**
   * Creates the matcher.
   *
   * @param embedder turns text into embeddings
   * @param vectorStore saves profile embeddings between runs
   */
  public SemanticProfileMatcher(
      TextEmbeddingGateway embedder, ProfileVectorDataAccessInterface vectorStore) {
    this.embedder = embedder;
    this.vectorStore = vectorStore;
  }

  @Override
  public double[] score(String topic, List<StockProfile> profiles)
      throws EmbeddingUnavailableException {
    final float[] query = embedder.embed(topic);
    final Map<String, float[]> profileVectors = vectorsFor(profiles);
    final double[] scores = new double[profiles.size()];
    for (int i = 0; i < profiles.size(); i++) {
      scores[i] = dot(query, profileVectors.get(profiles.get(i).getSymbol()));
    }
    return scores;
  }

  /** Returns a vector for every profile: from memory, from the saved file, or newly computed. */
  private synchronized Map<String, float[]> vectorsFor(List<StockProfile> profiles)
      throws EmbeddingUnavailableException {
    final String key = keyFor(profiles);
    if (!key.equals(loadedKey)) {
      vectors = new HashMap<>(vectorStore.loadVectors(key));
      loadedKey = key;
    }
    boolean computedAny = false;
    for (StockProfile profile : profiles) {
      if (!vectors.containsKey(profile.getSymbol())) {
        vectors.put(profile.getSymbol(), embedder.embed(ProfileText.of(profile)));
        computedAny = true;
      }
    }
    if (computedAny) {
      vectorStore.saveVectors(key, vectors);
    }
    return vectors;
  }

  /**
   * Identifies the model and the exact profile texts, so saved vectors are reused only if both are
   * unchanged. {@code List.hashCode} is defined by Java, so it is the same on every run.
   */
  private String keyFor(List<StockProfile> profiles) {
    final List<String> texts = new ArrayList<>();
    for (StockProfile profile : profiles) {
      texts.add(profile.getSymbol() + "|" + ProfileText.of(profile));
    }
    return embedder.modelName() + ":" + profiles.size() + ":"
        + Integer.toHexString(texts.hashCode());
  }

  /** For vectors of length 1, the dot product is the cosine similarity (1 = same meaning). */
  private static double dot(float[] a, float[] b) {
    double sum = 0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i] * b[i];
    }
    return sum;
  }
}

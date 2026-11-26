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
 * <p>The score blends two similarities, half each:
 *
 * <ul>
 *   <li><b>Profile:</b> the topic against the whole profile (name, sector, industry, description).
 *   <li><b>Category:</b> the topic against the profile's sector or industry name, whichever is
 *       closer. Broad searches like "tech" match the sector "Information Technology" well, but
 *       on whole profiles they mostly hit companies with "Technologies" in their name.
 * </ul>
 *
 * <p>Profile embeddings are computed once, saved through a
 * {@link ProfileVectorDataAccessInterface}, and kept in memory. Only the topic is embedded on each
 * search.
 */
public class SemanticProfileMatcher implements ProfileMatcher {

  /** How much the category similarity counts; the profile similarity gets the rest. */
  static final double CATEGORY_WEIGHT = 0.5;

  /** Category vectors are saved with the profile vectors, under this prefix. */
  private static final String CATEGORY_PREFIX = "category:";

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
    final Map<String, Double> categoryScores = new HashMap<>();
    final double[] scores = new double[profiles.size()];
    for (int i = 0; i < profiles.size(); i++) {
      final StockProfile profile = profiles.get(i);
      final double profileScore = dot(query, profileVectors.get(profile.getSymbol()));
      final double categoryScore =
          Math.max(
              categoryScore(query, profile.getSector(), profileVectors, categoryScores),
              categoryScore(query, profile.getIndustry(), profileVectors, categoryScores));
      scores[i] = (1 - CATEGORY_WEIGHT) * profileScore + CATEGORY_WEIGHT * categoryScore;
    }
    return scores;
  }

  /** The topic's similarity to a sector or industry name (0 if there is none), worked out once. */
  private static double categoryScore(
      float[] query, String category, Map<String, float[]> vectors, Map<String, Double> cache) {
    if (category.isEmpty()) {
      return 0;
    }
    return cache.computeIfAbsent(category, name -> dot(query, vectors.get(CATEGORY_PREFIX + name)));
  }

  /**
   * Returns a vector for every profile and every sector and industry name: from memory, from the
   * saved file, or newly computed.
   */
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
      for (String category : new String[] {profile.getSector(), profile.getIndustry()}) {
        final String categoryKey = CATEGORY_PREFIX + category;
        if (!category.isEmpty() && !vectors.containsKey(categoryKey)) {
          vectors.put(categoryKey, embedder.embed(category));
          computedAny = true;
        }
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

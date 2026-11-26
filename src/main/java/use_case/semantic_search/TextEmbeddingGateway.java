package use_case.semantic_search;

/**
 * Turns text into an embedding: a list of numbers that captures the text's meaning.
 *
 * <p>Texts with similar meaning get vectors pointing in a similar direction, so "oil stocks" lands
 * near "explores for and produces crude oil". The semantic search use case only knows this
 * interface, not which model produces the numbers (Dependency Inversion), so the model can be
 * swapped without changing the use case.
 */
public interface TextEmbeddingGateway {

  /**
   * Embeds one text.
   *
   * @param text the text to embed, e.g. "best selling oil stocks"
   * @return the embedding, scaled to length 1 so cosine similarity is a plain dot product
   * @throws EmbeddingUnavailableException if the model can't be loaded or run
   */
  float[] embed(String text) throws EmbeddingUnavailableException;

  /**
   * Names the model, so saved embeddings can be thrown away if the model changes.
   *
   * @return a short model identifier, e.g. "all-MiniLM-L6-v2"
   */
  String modelName();
}

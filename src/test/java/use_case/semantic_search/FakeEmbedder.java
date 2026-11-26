package use_case.semantic_search;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A stand-in for the real model: a 3-number "embedding" that counts oil, tech and bank words.
 * Good enough to test the use case without loading a 23 MB model.
 */
class FakeEmbedder implements TextEmbeddingGateway {

  final List<String> embedded = new ArrayList<>();
  boolean unavailable;
  String modelName = "fake-model";

  @Override
  public float[] embed(String text) throws EmbeddingUnavailableException {
    if (unavailable) {
      throw new EmbeddingUnavailableException("model can't run here");
    }
    embedded.add(text);
    final String lower = text.toLowerCase(Locale.ROOT);
    final float[] vector = {
        count(lower, "oil") + count(lower, "crude"),
        count(lower, "computer") + count(lower, "smartphone"),
        count(lower, "bank")
    };
    double length = 0;
    for (float value : vector) {
      length += value * value;
    }
    length = Math.sqrt(length);
    for (int i = 0; i < vector.length && length > 0; i++) {
      vector[i] /= length;
    }
    return vector;
  }

  @Override
  public String modelName() {
    return modelName;
  }

  private static int count(String text, String word) {
    return text.split(word, -1).length - 1;
  }
}

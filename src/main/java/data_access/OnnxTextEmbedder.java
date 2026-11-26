package data_access;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import use_case.semantic_search.EmbeddingUnavailableException;
import use_case.semantic_search.TextEmbeddingGateway;

/**
 * Embeds text with the all-MiniLM-L6-v2 model, running locally through ONNX Runtime.
 *
 * <p>No API, key or internet is needed once the model files are on disk. For each text:
 *
 * <ol>
 *   <li>The {@link WordPieceTokenizer} turns it into token ids.
 *   <li>The model outputs 384 numbers for every token.
 *   <li>Those are averaged into one vector for the whole text ("mean pooling").
 *   <li>The vector is scaled to length 1, so comparing two texts is a dot product.
 * </ol>
 *
 * <p>The model is loaded the first time {@link #embed} is called (it takes about a second, and may
 * download the files), so call it off the Swing event thread.
 */
public class OnnxTextEmbedder implements TextEmbeddingGateway {

  /** The model was trained on texts up to this many tokens; longer ones are cut off. */
  static final int MAX_TOKENS = 256;

  private final MiniLmModelFiles files;
  private OrtEnvironment environment;
  private OrtSession session;
  private WordPieceTokenizer tokenizer;

  /**
   * Set if the model can never run on this computer (no ONNX Runtime build for its processor), so
   * later searches fail fast with the same message instead of trying again.
   */
  private EmbeddingUnavailableException unsupported;

  /**
   * Creates the embedder. Nothing is loaded until the first call to {@link #embed}.
   *
   * @param files provides (and if needed downloads) the model and vocabulary files
   */
  public OnnxTextEmbedder(MiniLmModelFiles files) {
    this.files = files;
  }

  @Override
  public String modelName() {
    return "all-MiniLM-L6-v2-qint8";
  }

  @Override
  public float[] embed(String text) throws EmbeddingUnavailableException {
    loadIfNeeded();
    final long[] ids = tokenizer.encode(text, MAX_TOKENS);
    final long[] mask = new long[ids.length];
    Arrays.fill(mask, 1L);
    final long[] typeIds = new long[ids.length];

    final Map<String, OnnxTensor> inputs = new HashMap<>();
    try {
      inputs.put("input_ids", OnnxTensor.createTensor(environment, new long[][] {ids}));
      inputs.put("attention_mask", OnnxTensor.createTensor(environment, new long[][] {mask}));
      if (session.getInputNames().contains("token_type_ids")) {
        inputs.put("token_type_ids", OnnxTensor.createTensor(environment, new long[][] {typeIds}));
      }
      try (OrtSession.Result result = session.run(inputs)) {
        // Shape [1 text][tokens][384 numbers per token].
        final float[][] tokenVectors = ((float[][][]) result.get(0).getValue())[0];
        return normalise(meanPool(tokenVectors));
      }
    } catch (OrtException e) {
      throw new EmbeddingUnavailableException("The embedding model failed to run", e);
    } finally {
      for (OnnxTensor tensor : inputs.values()) {
        tensor.close();
      }
    }
  }

  /** Loads the tokenizer and model once; later calls do nothing. */
  private synchronized void loadIfNeeded() throws EmbeddingUnavailableException {
    if (session != null) {
      return;
    }
    if (unsupported != null) {
      throw unsupported;
    }
    try {
      tokenizer = WordPieceTokenizer.fromVocabFile(files.vocabFile());
      environment = OrtEnvironment.getEnvironment();
      session =
          environment.createSession(files.modelFile().toString(), new OrtSession.SessionOptions());
    } catch (IOException e) {
      throw new EmbeddingUnavailableException(
          "Could not get the embedding model files: " + e.getMessage(), e);
    } catch (OrtException e) {
      throw new EmbeddingUnavailableException("The embedding model could not be loaded", e);
    } catch (LinkageError | RuntimeException e) {
      // ONNX Runtime's native library has no build for this computer. Java reports that as an
      // UnsatisfiedLinkError the first time and as a NoClassDefFoundError every time after
      // (both are LinkageErrors), so remember it rather than letting a later search crash.
      unsupported =
          new EmbeddingUnavailableException(
              "the AI search model isn't supported on this computer", e);
      throw unsupported;
    }
  }

  /** Averages the token vectors into one vector for the whole text. */
  static float[] meanPool(float[][] tokenVectors) {
    final float[] mean = new float[tokenVectors[0].length];
    for (float[] token : tokenVectors) {
      for (int d = 0; d < mean.length; d++) {
        mean[d] += token[d];
      }
    }
    for (int d = 0; d < mean.length; d++) {
      mean[d] /= tokenVectors.length;
    }
    return mean;
  }

  /** Scales a vector to length 1 (leaves an all-zero vector unchanged). */
  static float[] normalise(float[] vector) {
    double sumOfSquares = 0;
    for (float value : vector) {
      sumOfSquares += value * value;
    }
    final double length = Math.sqrt(sumOfSquares);
    if (length > 0) {
      for (int d = 0; d < vector.length; d++) {
        vector[d] /= length;
      }
    }
    return vector;
  }
}

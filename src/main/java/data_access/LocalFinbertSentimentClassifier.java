package data_access;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import entity.NewsArticle;
import entity.Sentiment;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import use_case.explain_movement.SentimentClassifier;
import use_case.explain_movement.SentimentUnavailableException;

/**
 * Labels articles with FinBERT, a BERT model trained on financial news, running on this computer
 * through ONNX Runtime. No API key or internet is needed once the model file is downloaded.
 *
 * <p>For each article, the headline and summary are tokenized (at most {@value #MAX_TOKENS}
 * tokens), articles are run through the model {@value #BATCH_SIZE} at a time, and the model's
 * highest of three scores gives the label. The label order is read from the model's {@code
 * config.json}, not assumed.
 *
 * <p>The tokenizer vocabulary and {@code config.json} are bundled in {@code
 * src/main/resources/finbert/}; only the model file comes from {@link FinbertModelFiles}.
 *
 * <p>Loading takes a second or two, so create one instance and reuse it; {@link #close()} it when
 * the app shuts down.
 */
public class LocalFinbertSentimentClassifier implements SentimentClassifier, AutoCloseable {

  static final String VOCAB_RESOURCE = "/finbert/vocab.txt";
  static final String CONFIG_RESOURCE = "/finbert/config.json";
  static final int MAX_TOKENS = 128;
  static final int BATCH_SIZE = 16;

  private static final String INPUT_IDS = "input_ids";
  private static final String ATTENTION_MASK = "attention_mask";
  private static final String TOKEN_TYPE_IDS = "token_type_ids";

  private final OrtEnvironment environment;
  private final OrtSession session;
  private final BertTokenizer tokenizer;
  private final List<Sentiment> labels;

  private LocalFinbertSentimentClassifier(
      OrtEnvironment environment, OrtSession session, BertTokenizer tokenizer,
      List<Sentiment> labels) {
    this.environment = environment;
    this.session = session;
    this.tokenizer = tokenizer;
    this.labels = labels;
  }

  /**
   * Loads the model from a file that is already on this computer, plus the bundled vocabulary and
   * config.
   *
   * @param files the model folder; see {@link FinbertModelFiles#downloadMissing()}
   * @return the classifier
   * @throws IOException if the model file is missing or can't be loaded
   */
  public static LocalFinbertSentimentClassifier load(FinbertModelFiles files) throws IOException {
    if (!files.isComplete()) {
      throw new IOException("FinBERT model file is missing in " + files.getDirectory());
    }
    final List<Sentiment> labels;
    try (InputStream config = openResource(CONFIG_RESOURCE)) {
      labels = readLabels(new String(config.readAllBytes(), StandardCharsets.UTF_8));
    }
    final BertTokenizer tokenizer;
    try (InputStream vocab = openResource(VOCAB_RESOURCE)) {
      tokenizer = BertTokenizer.fromVocab(vocab);
    }
    final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    try {
      final OrtSession session =
          environment.createSession(files.modelFile().toString(), new OrtSession.SessionOptions());
      return new LocalFinbertSentimentClassifier(environment, session, tokenizer, labels);
    } catch (OrtException e) {
      throw new IOException("Could not load the FinBERT model: " + e.getMessage(), e);
    }
  }

  private static InputStream openResource(String name) throws IOException {
    final InputStream in = LocalFinbertSentimentClassifier.class.getResourceAsStream(name);
    if (in == null) {
      throw new IOException("missing bundled resource " + name);
    }
    return in;
  }

  @Override
  public List<Sentiment> classify(List<NewsArticle> articles)
      throws SentimentUnavailableException {
    final Sentiment[] results = new Sentiment[articles.size()];
    final List<Integer> pending = new ArrayList<>();
    final List<long[]> pendingIds = new ArrayList<>();
    for (int i = 0; i < articles.size(); i++) {
      pending.add(i);
      pendingIds.add(tokenizer.encode(textOf(articles.get(i)), MAX_TOKENS));
      if (pending.size() == BATCH_SIZE) {
        runBatch(pending, pendingIds, results);
      }
    }
    if (!pending.isEmpty()) {
      runBatch(pending, pendingIds, results);
    }
    return Arrays.asList(results);
  }

  private void runBatch(List<Integer> indexes, List<long[]> ids, Sentiment[] results)
      throws SentimentUnavailableException {
    final long[][] inputIds = pad(ids, tokenizer.getPadId());
    final Map<String, OnnxTensor> inputs = new HashMap<>();
    try {
      inputs.put(INPUT_IDS, OnnxTensor.createTensor(environment, inputIds));
      if (session.getInputNames().contains(ATTENTION_MASK)) {
        inputs.put(ATTENTION_MASK, OnnxTensor.createTensor(environment, attentionMask(ids,
            inputIds[0].length)));
      }
      if (session.getInputNames().contains(TOKEN_TYPE_IDS)) {
        inputs.put(TOKEN_TYPE_IDS,
            OnnxTensor.createTensor(environment, new long[ids.size()][inputIds[0].length]));
      }
      try (OrtSession.Result result = session.run(inputs)) {
        final float[][] logits = (float[][]) result.get(0).getValue();
        for (int row = 0; row < indexes.size(); row++) {
          results[indexes.get(row)] = labelOf(logits[row], labels);
        }
      }
    } catch (OrtException | ClassCastException e) {
      throw new SentimentUnavailableException("FinBERT failed: " + e.getMessage(), e);
    } finally {
      for (OnnxTensor tensor : inputs.values()) {
        tensor.close();
      }
      indexes.clear();
      ids.clear();
    }
  }

  /** Headline and summary as one text; FinBERT reads up to {@value #MAX_TOKENS} tokens of it. */
  static String textOf(NewsArticle article) {
    return (article.getHeadline() + " " + article.getSummary()).trim();
  }

  /**
   * Makes every row the same length by adding padding ids at the end.
   *
   * @param ids token ids per article
   * @param padId the padding token's id
   * @return a rectangular array, as long as the longest row
   */
  static long[][] pad(List<long[]> ids, long padId) {
    int longest = 0;
    for (long[] row : ids) {
      longest = Math.max(longest, row.length);
    }
    final long[][] padded = new long[ids.size()][longest];
    for (int i = 0; i < ids.size(); i++) {
      Arrays.fill(padded[i], padId);
      System.arraycopy(ids.get(i), 0, padded[i], 0, ids.get(i).length);
    }
    return padded;
  }

  /**
   * Marks real tokens with 1 and padding with 0, so the model ignores the padding.
   *
   * @param ids token ids per article, before padding
   * @param length the padded length
   * @return the attention mask
   */
  static long[][] attentionMask(List<long[]> ids, int length) {
    final long[][] mask = new long[ids.size()][length];
    for (int i = 0; i < ids.size(); i++) {
      Arrays.fill(mask[i], 0, ids.get(i).length, 1L);
    }
    return mask;
  }

  /**
   * Picks the label with the highest score.
   *
   * @param logits the model's scores, one per label
   * @param labels the label for each position
   * @return the label of the highest score
   */
  static Sentiment labelOf(float[] logits, List<Sentiment> labels) {
    int best = 0;
    for (int i = 1; i < logits.length; i++) {
      if (logits[i] > logits[best]) {
        best = i;
      }
    }
    return labels.get(best);
  }

  /**
   * Reads the label order from a model's config.json ({@code "id2label": {"0": "positive", ...}}).
   *
   * @param configJson the file's contents
   * @return the label for each output position
   * @throws IOException if the labels are missing or aren't positive/negative/neutral
   */
  static List<Sentiment> readLabels(String configJson) throws IOException {
    try {
      final JSONObject id2label = new JSONObject(configJson).getJSONObject("id2label");
      final Sentiment[] labels = new Sentiment[id2label.length()];
      for (String key : id2label.keySet()) {
        labels[Integer.parseInt(key)] =
            Sentiment.valueOf(id2label.getString(key).trim().toUpperCase(Locale.ROOT));
      }
      final List<Sentiment> list = Arrays.asList(labels);
      if (list.contains(null)) {
        throw new IOException("config.json labels have gaps");
      }
      return list;
    } catch (JSONException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
      throw new IOException("config.json has no usable id2label: " + e.getMessage(), e);
    }
  }

  @Override
  public void close() {
    try {
      session.close();
    } catch (OrtException e) {
      // Nothing useful to do while shutting down.
    }
  }
}

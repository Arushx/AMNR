package use_case.semantic_search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class SemanticProfileMatcherTest {

  private final FakeEmbedder embedder = new FakeEmbedder();
  private final InMemoryVectorStore store = new InMemoryVectorStore();
  private final List<entity.StockProfile> profiles =
      List.of(TestProfiles.EXXON, TestProfiles.APPLE, TestProfiles.BANK);

  @Test
  void scoresProfilesByMeaning() throws EmbeddingUnavailableException {
    double[] scores = new SemanticProfileMatcher(embedder, store).score("crude", profiles);

    assertTrue(scores[0] > scores[1]);
    assertTrue(scores[0] > scores[2]);
  }

  @Test
  void embedsProfilesOnceAndSavesThem() throws EmbeddingUnavailableException {
    SemanticProfileMatcher matcher = new SemanticProfileMatcher(embedder, store);

    matcher.score("oil", profiles);
    matcher.score("bank", profiles);

    // 3 profiles + 6 sector/industry names + 2 queries; the second search reuses the vectors.
    assertEquals(11, embedder.embedded.size());
    assertEquals(1, store.saves);
  }

  @Test
  void reusesSavedVectorsOnTheNextRun() throws EmbeddingUnavailableException {
    new SemanticProfileMatcher(embedder, store).score("oil", profiles);
    embedder.embedded.clear();

    new SemanticProfileMatcher(embedder, store).score("oil", profiles);

    assertEquals(List.of("oil"), embedder.embedded);
    assertEquals(1, store.saves);
  }

  @Test
  void ignoresSavedVectorsFromAnotherModel() throws EmbeddingUnavailableException {
    new SemanticProfileMatcher(embedder, store).score("oil", profiles);
    embedder.modelName = "another-model";
    embedder.embedded.clear();

    new SemanticProfileMatcher(embedder, store).score("oil", profiles);

    // Everything is embedded again: 3 profiles + 6 sector/industry names + the query.
    assertEquals(10, embedder.embedded.size());
  }

  @Test
  void categoryMatchLiftsProfilesWhoseTextBarelyMentionsTheTopic()
      throws EmbeddingUnavailableException {
    // The bank's industry is "Banks"; its own text mentions banking once, Exxon's not at all.
    double[] scores = new SemanticProfileMatcher(embedder, store).score("bank", profiles);

    assertEquals(1.0, scores[2], 1e-6);
    assertEquals(0.0, scores[0], 1e-6);
  }

  @Test
  void reportsWhenTheModelCannotRun() {
    embedder.unavailable = true;

    assertThrows(EmbeddingUnavailableException.class,
        () -> new SemanticProfileMatcher(embedder, store).score("oil", profiles));
  }
}

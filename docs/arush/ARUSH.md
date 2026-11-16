# Arush's Project Notebook

A running log of my AMNR work with Claude, oldest first. Each entry covers what we talked about,
what we added, and what I learned.

---

## 1. Reviewed the repo and course requirements (2026-11-16)

**Talked about:** where the project stands compared with the course requirements and rubric.

**Found:**
- No code yet; `Main.java` didn't compile (`public class JoomBerg()`).
- `pom.xml` had no dependencies; `.idea/workspace.xml` was committed.
- Use cases in the README were one-liners; the MVP table, entities and API sections were empty.
- User stories 1 and 4 overlap; story 3 is vague.

**Learned:**
- The course wants us to mirror the CA lab ([ca-login](https://github.com/CSC207-2026F-UofT/ca-login)): packages `entity`, `use_case`, `interface_adapter`, `view`, `data_access`, `app`.
- Rubric: CheckStyle is required for 4/5 in code quality; we need a design pattern that isn't from the starter code; the demo must show saving **and** loading data.

---

## 2. Scaffold fix (PR #16)

**Added:**
- `.gitignore`: ignore `.idea/` and `.env`.
- `pom.xml`: org.json, OkHttp, JUnit 5, CheckStyle (course config, fails the build on style errors).
- Renamed `adapter` → `interface_adapter`, `infrastructure` → `data_access`; added `app`.
- `app/Main` (fixed) and `app/AppBuilder`.

**Learned:**
- `AppBuilder` is the "Main component": the only class that knows every layer, because it creates the objects and connects them (dependency injection).
- Each use case adds `addXView()` / `addXUseCase()` methods to `AppBuilder`. That's the Builder pattern, and adding methods without editing existing ones is the Open/Closed Principle.
- `CardLayout` stacks views like cards, with one visible at a time.

---

## 3. Questions: constructor, invokeLater, Swing

**Talked about:** why `Main` had a private constructor, what `invokeLater` does, and how Swing works.

**Learned:**
- The private constructor only stops `new Main()`. It isn't required by CA or CheckStyle, so we removed it.
- Swing runs all drawing and clicks on one thread, the **EDT**. GUI code should run there, and `invokeLater` queues code onto it.
- Slow work, like API calls, must **not** run on the EDT, or the window freezes.
- Swing basics: `JFrame` = window, `JPanel` = container, `JButton`/`JLabel`/`JTextField` = widgets, layout managers position them, listeners react to clicks.
- How Swing fits CA: view → controller → interactor → presenter → view model → view refreshes (Observer).

---

## 4. Main constructor fix (PR #19)

**Added:** removed `private Main() {}` and `final` from `Main`; kept `invokeLater`.

---

## 5. Alpha Vantage API key setup

**Talked about:** where to keep the key safely.

**Added:** nothing in code. The key goes in the environment variable `ALPHA_VANTAGE_API_KEY`:
- IntelliJ: *Run* → *Edit Configurations* → `Main` → *Environment variables*.
- Terminal: `export ALPHA_VANTAGE_API_KEY="..."` in `~/.zshrc`.

**Learned:**
- The IntelliJ setting stays as long as that run configuration exists on my Mac. `~/.zshrc` is permanent, but IntelliJ opened from the Dock doesn't see it.
- Only `AppBuilder` reads the key (`System.getenv(...)`) and passes it to the DAO's constructor, so the DAO is easy to test.
- If a key is ever committed, regenerate it; it stays in Git history.

---

## 6. Alpha Vantage research

**Talked about:** how the API works, its limits, response formats, Java options, charts, and how it fits CA.

**Learned:**
- One URL for everything: `https://www.alphavantage.co/query?function=...&symbol=...&apikey=...`
- **Free key: 25 requests/day.** Educational projects can apply for unlimited access.
- Free daily history = last **100 trading days** (`compact`). Weekly/monthly give the full history for free. Intraday is premium. `NEWS_SENTIMENT` may be premium, which matters for story 5, so test it with a real key.
- **Errors come back as HTTP 200** with an `"Information"`, `"Note"` or `"Error Message"` key, so check the JSON, not just the status code.
- Numbers come as strings (`"247.1300"`), keys are numbered (`"4. close"`), and the daily series is an object keyed by date (unordered), so sort it ourselves.

**Endpoints per use case:**

| Use case | Endpoint |
|---|---|
| Search + quote | `SYMBOL_SEARCH`, `GLOBAL_QUOTE` |
| Watchlist | `GLOBAL_QUOTE` (+ our own JSON file) |
| Pair comparison | `TIME_SERIES_DAILY` ×2 |
| Historical range | `TIME_SERIES_DAILY` / `WEEKLY` / `MONTHLY` |
| News at peaks/dips | `NEWS_SENTIMENT` (check if premium) |

**Real response, `GLOBAL_QUOTE` (demo key):**
```json
{ "Global Quote": { "01. symbol": "IBM", "05. price": "247.1300",
  "07. latest trading day": "2026-11-13", "09. change": "2.4600", "10. change percent": "1.0054%" } }
```

**Real response, `TIME_SERIES_DAILY`:**
```json
{ "Meta Data": { "2. Symbol": "IBM", ... },
  "Time Series (Daily)": { "2026-11-13": { "1. open": "246.63", "2. high": "247.85",
    "3. low": "244.83", "4. close": "247.13", "5. volume": "2669242" }, ... } }
```

**Decisions (proposed):**
- **Write our own small client** with OkHttp + org.json (already in `pom.xml`, matches the course), not the [crazzyghost wrapper](https://github.com/crazzyghost/alphavantage-java), which isn't on Maven Central and hides the JSON.
- **CA layout:**
  - `entity`: `PricePoint` (date, OHLC, volume), `PriceHistory` (sorted points + rules), `Quote`.
  - `use_case`: a small data access interface per use case (ISP), e.g. `getDailyHistory(symbol)`.
  - `data_access`: `AlphaVantageClient` (HTTP + error check) and `AlphaVantageStockDataAccessObject` (JSON → entities).
- **Caching DAO** that saves responses to files and wraps the real DAO: protects the 25/day limit, and it's the **Decorator/Proxy pattern**, which counts for the rubric.
- **Charts: JFreeChart**, used only in `view`. The presenter puts plain lists (`dates`, `closes`) in the view model, and the view turns them into a chart.
- API calls run off the EDT (`SwingWorker`); screen updates go back with `invokeLater`.

**Open questions for the team:** is `NEWS_SENTIMENT` free? Should we apply for educational access? Is 100 days of daily data enough? Line chart or candlesticks?

**Sources:** [docs](https://www.alphavantage.co/documentation/), [limits](https://www.alphavantage.co/support/), [premium](https://www.alphavantage.co/premium/), [JFreeChart](https://github.com/jfree/jfreechart), [XChart](https://knowm.org/open-source/xchart/)

---

## 7. Entities: PricePoint, PriceHistory, Quote

**Added** (`src/main/java/entity/`, tests in `src/test/java/entity/`, 18 tests passing):
- `PricePoint`: one trading day (date, open, high, low, close, volume). Rejects a missing date, prices ≤ 0, low > high, negative volume.
- `PriceHistory`: a symbol + its `PricePoint`s, always sorted oldest → newest. Methods: `between(start, end)`, `getLatest()`, `getHighestClose()` (peak), `getLowestClose()` (dip). Rejects duplicate dates.
- `Quote`: symbol, price, change, change %, latest trading day. Change can be negative.

**Learned:**
- Entities are the innermost layer: they import only Java itself (`java.time`, `java.util`), with no JSON, HTTP or Swing. That's why the Alpha Vantage parsing goes in `data_access`, not here.
- Entities enforce **business rules** (a low can't be above the high) by throwing `IllegalArgumentException`, so a bad object can never exist.
- **Immutable** (`private final` fields, no setters, `Collections.unmodifiableList`): once made, it can't change, so it's safe to pass between layers.
- `between()` returns a **new** `PriceHistory` instead of changing the old one.
- Peak/dip methods are there for story 5 (news at peaks and dips); `between()` is for story 4 (date ranges).
- Entity tests are plain unit tests: no fakes needed, because entities depend on nothing.

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

---

## 8. Alpha Vantage client (data_access)

**Added** (`src/main/java/data_access/`):
- `AlphaVantageClient`: builds the request URL (with the API key), sends it with OkHttp, and returns the JSON. Methods: `fetchGlobalQuote(symbol)`, `fetchDailyTimeSeries(symbol)`. Throws if the key is missing.
- `AlphaVantageParser`: turns that JSON into our entities (`parseQuote` → `Quote`, `parseDailyHistory` → `PriceHistory`). All of Alpha Vantage's key names (`"05. price"`, `"4. close"`, ...) are constants here.
- `AlphaVantageException`: one checked exception for every failure (no network, bad key, limit hit, unknown symbol, weird format), with a message safe to show the user.
- Tests: `AlphaVantageParserTest` uses **saved real responses** in `src/test/resources/alphavantage/`, so it never uses up requests. `AlphaVantageClientTest.fetchesRealQuote` calls the real API, but only runs when `ALPHA_VANTAGE_API_KEY` is set (it costs 1 request).
- CheckStyle: turned off only the `PackageName` rule, because Google style bans `_` but the course layout uses `data_access` / `use_case`.

**Learned:**
- **SRP split:** the client changes if *how we talk to the API* changes; the parser changes if *the JSON format* changes. Different reasons to change, so different classes. It also makes the parser testable without the internet.
- **Errors come as HTTP 200**, so `checkForApiError` looks for `"Error Message"`, `"Information"` or `"Note"` in the JSON. An unknown symbol comes back as an empty `"Global Quote": {}`.
- **Exception wrapping:** low-level errors (`IOException`, `JSONException`, `NumberFormatException`) are caught and rethrown as `AlphaVantageException(message, cause)`, so callers deal with one type and the original cause is kept for debugging.
- `try (Response response = ...)` is **try-with-resources**: it closes the network connection automatically.
- `HttpUrl.Builder.addQueryParameter` encodes the URL safely (spaces, symbols like `BRK.B`).
- Bad data that breaks an entity rule (e.g. a price of 0) is also treated as a bad response.

**Not done yet:** nothing calls this from `AppBuilder`; it will be used by the DAO that implements a use case's data access interface.

---

## 9. Team CLAUDE.md for AI tools

**Added** (branch `docs/team-ai-guide`, from `main`): `CLAUDE.md` at the repo root, plus `AGENTS.md`, which points Codex and other tools to it.

**What it covers:** course requirements, the package/layer table and Dependency Rule, the 7-step CA flow, `AppBuilder` wiring, SOLID with examples, patterns that count for the rubric, Java 11 limits, CheckStyle, Alpha Vantage rules (key, 25/day, HTTP-200 errors), Swing threading, testing with fakes, the README spec format, diagrams, the Git/PR workflow, and the team contract's AI rules.

**Learned:** Claude Code reads `CLAUDE.md` automatically; Codex reads `AGENTS.md`. Putting the rules in the repo means every teammate's AI follows the same CA/SOLID rules, not just mine.

---

## 10. Walkthrough: every file, and where the Data Access Interface fits

**Talked about:** why each file exists; what data access interface the Alpha Vantage classes implement.

**Learned:**
- `AlphaVantageClient` and `AlphaVantageParser` implement **no interface**. They're helpers that know HTTP and JSON. They don't belong to any use case.
- A **Data Access Interface** is declared by a *use case* (in `use_case/<name>/`), in entity terms only, e.g. `PriceHistory getDailyHistory(String symbol)`.
- A DAO in `data_access` (e.g. `AlphaVantageStockDataAccessObject`) **implements** it by calling client → parser, and translates `AlphaVantageException` into a use-case exception, because the interactor isn't allowed to know about `data_access`.
- That's DIP (the interactor depends on its own interface), ISP (small interface per use case; one DAO can implement several), and it makes interactor tests easy with a fake DAO.

**Next:** pick which use case I lead, then write its interface first.

---

## 11. The CA engine slide and "implementing from below"

**Talked about:** the CA engine diagram (lecture 7, slide 37), and how a lower layer implements an interface owned by a higher layer.

**Learned:**
- The Dependency Rule is about **source code direction**, not data flow. Outer code may depend on inner code; inner code never names outer code.
- `Data Access` (outer, blue) **implements** `Data Access Interface` (inner, pink): the hollow-triangle arrow points up/inward. So the interactor calls the interface, and at runtime the real DAO answers.
- Mapping to our code: Entities ✅; "Database" = Alpha Vantage via `AlphaVantageClient` + `AlphaVantageParser` ✅; "Data Access" box = the DAO we still need, which implements the use case's interface.
- Keep a thin DAO above the client/parser (SRP + translating `AlphaVantageException` into a use-case exception), instead of making the client implement the interface directly.

---

## 12. Data Access Interface + Data Access (matching the CA engine)

**Talked about:** making the data side match the CA engine slide, without building a whole use case yet.

**Added:**
- `use_case/QuoteDataAccessInterface`: `Quote getQuote(String symbol)`.
- `use_case/PriceHistoryDataAccessInterface`: `PriceHistory getDailyHistory(String symbol)`.
- `use_case/StockDataUnavailableException`: the one error the use cases see.
- `data_access/AlphaVantageStockDataAccessObject`: **implements both interfaces**, calling client → parser, and converting `AlphaVantageException` into `StockDataUnavailableException`.
- `AlphaVantageStockDataAccessObjectTest`: uses a `FakeClient` (subclass returning saved JSON, or failing), so no network. 31 tests pass.

**How it maps to the slide:**

| Slide box | Our class |
|---|---|
| Entities | `PricePoint`, `PriceHistory`, `Quote` |
| Data Access Interface `<I>` | `QuoteDataAccessInterface`, `PriceHistoryDataAccessInterface` |
| Data Access | `AlphaVantageStockDataAccessObject` |
| Database | Alpha Vantage, via `AlphaVantageClient` + `AlphaVantageParser` |

**Learned:**
- The interfaces live in `use_case` (inner), and the DAO in `data_access` (outer) implements them: "implementing from below".
- The interfaces are shared and small (one kind of data each) instead of one per use case, because no use case exists yet. Any use case can depend on just the one it needs (ISP). One DAO implements both.
- The DAO receives the client and parser through its constructor (dependency injection), which is why the test can pass in a fake client.
- Nothing in `use_case` imports `data_access`, so the Dependency Rule holds.

---

## 13. README: entities and API sections

**Added:** filled in the README's **Entities** section (`PricePoint`, `PriceHistory`, `Quote`, with fields and rules) and **API Usage** section (Alpha Vantage, docs link, which endpoints are used for what, whether they've been called successfully, the 25/day limit, and key setup).

**Learned:** the course README must say whether each API was **called successfully**. Ours is honest: it works with the demo key; the check with my own key is still pending.

---

## 14. Code walkthrough: PricePoint

**Talked about:** every line of `PricePoint`, and how it works at runtime.

**Learned:**
- It imports only `java.time.LocalDate`, so the entity depends on nothing outward. `LocalDate` = a date with no time, comparable, and can't be invalid.
- `private final` fields + no setters = **immutable**. `double` for prices, `long` for volume (can exceed `int`).
- The constructor checks the business rules first (date not null, prices > 0, low ≤ high, volume ≥ 0) and **throws `IllegalArgumentException`** before storing anything, so an invalid `PricePoint` can never exist.
- **Runtime path:** JSON text → parser converts it (`LocalDate.parse`, `Double.parseDouble`, `Long.parseLong`) → `new PricePoint(...)` → checks → fields set → object frozen → others only read it via getters.
- **Bad data path:** constructor throws → parser catches it as `AlphaVantageException` → DAO turns that into `StockDataUnavailableException` → the use case reports "couldn't get data".
- Tip: debug `PricePointTest.rejectsLowAboveHigh` with a breakpoint on the `low > high` check to watch it happen.

---

## 15. Code walkthrough: PriceHistory (part 1: fields + constructor)

**Talked about:** what `PriceHistory` holds in terms of the API's `TIME_SERIES_DAILY` data, and its constructor.

**Learned:**
- `symbol` comes from `"Meta Data" → "2. Symbol"`; `points` = one `PricePoint` per date in `"Time Series (Daily)"` (~100 days).
- It rejects a blank symbol or a `null` list; an **empty** list is allowed (no days in range).
- The API's days come in **no guaranteed order** (JSON object keys aren't ordered), so the constructor **copies** the list and **sorts** it by date: oldest → newest, like a chart.
- After sorting, duplicates sit next to each other, so one loop comparing each day with the previous catches them. Use `.equals()` for dates, not `==`.
- `toUpperCase()` normalises the symbol; `Collections.unmodifiableList` makes the stored list read-only.

---

## 16. Code walkthrough: PriceHistory (part 2: methods)

**Learned:**
- `points.get(0)` is always the **oldest** day after the constructor sorts (Nov 11 in the example).
- `between(start, end)`: keeps days that are "not before start and not after end" (both ends included) and returns a **new** history, so the screen can show different ranges from **one** API response (saves our 25/day).
- `getLatest()`: the last item in the sorted list = the most recent trading day (matches `"3. Last Refreshed"`).
- `getHighestClose()` / `getLowestClose()`: one pass through the days, keeping the best so far (`>` / `<`, so ties go to the earlier day). They return the whole `PricePoint`, so we also get the **date** of the peak or dip for the news search (story 5).
- `requireNotEmpty()`: throws `IllegalStateException` instead of a confusing index crash when there are no days (e.g. a weekend range). `IllegalState` = "can't ask this right now" vs. `IllegalArgument` = "you passed something bad".

---

## 17. Code walkthrough: Quote

**Learned:**
- **Purpose:** a snapshot of "price now + today's move" for one stock (stories 1 and 2: search result, watchlist row). `PriceHistory` is for change over time; `Quote` is for right now.
- Comes from the `GLOBAL_QUOTE` request. Kept: `"01. symbol"`, `"05. price"`, `"09. change"`, `"10. change percent"` (minus the `%`), `"07. latest trading day"`. Dropped open/high/low/volume/previous close: not needed for this job (SRP; can add later).
- Rules: symbol not blank, price > 0, day not null. `change`/`changePercent` **may be negative** (stock fell), so they aren't checked.
- Immutable: a refresh makes a **new** `Quote`.
- `latestTradingDay` lets the UI say "as of Nov 13" on weekends instead of pretending it's live.

---

## 18. Code walkthrough: QuoteDataAccessInterface

**Learned:**
- It's the **Data Access Interface** box: `Quote getQuote(String symbol) throws StockDataUnavailableException`.
- Lives in `use_case` (inner), because the use case **owns** what it needs; the outer `data_access` layer implements it (DIP).
- Mentions no API details (no `GLOBAL_QUOTE`, URL, key, JSON); only our `Quote` entity and our own exception. So the interactor never knows Alpha Vantage exists.
- Benefits: swap the API by changing only the implementing class (OCP); test interactors with a fake implementation (no network); one small method (ISP).

---

## 19. Code walkthrough: AlphaVantageClient

**Learned:**
- The **only** class that talks to the internet: the "Data Access → Database" arrow. It returns raw JSON; it doesn't understand prices.
- Imports OkHttp (HTTP) and org.json (JSON). They're outer-layer tools, which is why it's in `data_access`.
- The API key is **passed into the constructor** (`AppBuilder` will read the env var); a missing key fails immediately with a fix-it message.
- `fetchGlobalQuote` / `fetchDailyTimeSeries` are one-liners that call the private `query(function, param, value)`.
- `query` builds `https://www.alphavantage.co/query?function=...&symbol=...&apikey=...` with `HttpUrl.Builder` (safe encoding), sends it with `.execute()` (it **waits**, so never on the EDT), and closes the connection with try-with-resources.
- Checks, in order: HTTP status → body exists → parse JSON → `checkForApiError` → return.
- `IOException` / `JSONException` are rethrown as **`AlphaVantageException`** (message + original cause), so callers handle one error type.
- `checkForApiError`: Alpha Vantage sends errors with **HTTP 200**, so look for `"Error Message"`, `"Information"` or `"Note"` in the JSON. It's package-private and static, so tests can call it without the network.
- SRP: it changes only if *how we talk to the API* changes.

---

## 20. Code walkthrough: AlphaVantageParser

**Learned:**
- A **translator**: raw Alpha Vantage JSON in → our entities out (the "Data Access → Entities" arrow). No network, so it's tested with saved files.
- Every key name (`"05. price"`, `"4. close"`, ...) is a constant in one place: readable, and only one spot to fix if the API changes.
- `parseQuote`: `optJSONObject("Global Quote")`; an **empty** `"Global Quote": {}` means an unknown symbol (HTTP 200, no error key, so the parser must catch it). Strip `%`, then `Double.parseDouble` / `LocalDate.parse` → `new Quote(...)`.
- `parseDailyHistory`: symbol from `"Meta Data" → "2. Symbol"`; loop over `"Time Series (Daily)".keySet()`. Each **key is the date**, and its box holds the prices → one `PricePoint` per day → `new PriceHistory(...)`, which does the sorting (no need to sort here).
- One multi-catch (`JSONException | IllegalArgumentException | DateTimeParseException`) turns a missing key, bad number (`NumberFormatException` is an `IllegalArgumentException`), entity rule violation or bad date into `AlphaVantageException`. One bad day rejects the whole history.
- SRP: client = *how we talk*; parser = *shape of the JSON*.

---

## 21. Code walkthrough: AlphaVantageStockDataAccessObject

**Learned:**
- It's the **"Data Access" box**: it `implements QuoteDataAccessInterface, PriceHistoryDataAccessInterface` (one class can implement several small interfaces, thanks to ISP).
- It imports `use_case` and `entity`, so outer points inward: "implementing from below".
- The client and parser come in through the **constructor** (dependency injection): `AppBuilder` passes the real ones, and the test passes a fake client, so no network.
- Each method is one line, read inside out: `parser.parseQuote(client.fetchGlobalQuote(symbol))`. `@Override` makes the compiler check it matches the interface.
- **Error translation:** catches `AlphaVantageException` (a `data_access` type) and rethrows `StockDataUnavailableException` (a `use_case` type). Otherwise the interactor would have to import `data_access`, breaking the Dependency Rule.
- The full path: DAO → client (URL, send, check) → parser (text → numbers → entity) → entity rules → back to the use case.
- To switch APIs: write a new class implementing the same interfaces and change one line in `AppBuilder` (OCP/DIP).

---

## 22. Use Case 1 (my use case), step 1: ticker search engine, no view yet

**Talked about:** I lead Use Case 1 ("search stocks by name and ticker and view historical data"). "Ticker" = the short trading code (`AAPL`). Alpha Vantage's price requests **only accept tickers**, so a company name has to be turned into a ticker first. We chose a **local listing file** (download every ticker + name once, save it, search offline) over calling `SYMBOL_SEARCH` each time. Plan: step 1 = ticker search; step 2 = name search with the local file.

**Added** (branch `use-case-1-search-up-stock-historical-data`), one file per CA engine box:

| Slide box | File |
|---|---|
| Input Data | `use_case/search_stock/SearchStockInputData` (the text typed) |
| Input Boundary | `SearchStockInputBoundary` (`execute(inputData)`) |
| Interactor | `SearchStockInteractor` |
| Output Boundary | `SearchStockOutputBoundary` (`prepareSuccessView` / `prepareFailView`) |
| Output Data | `SearchStockOutputData` (quote values + `dates`/`closes` lists, oldest first) |
| Data Access Interface | the existing `QuoteDataAccessInterface` + `PriceHistoryDataAccessInterface` |
| Data Access | the existing `AlphaVantageStockDataAccessObject` |
| Controller | `interface_adapter/search_stock/SearchStockController` |
| Presenter | `SearchStockPresenter` (formats `$247.13`, `+2.46 (+1.01%)`, `As of Nov 13, 2026`) |
| View Model | `SearchStockViewModel` + `SearchStockState` + `SearchStockViewModelListener` |
| View | **not yet** (Swing, later) |

Plus `AppBuilder.addSearchStockUseCase()`, which builds client → DAO → presenter → interactor → controller. It isn't called from `Main` yet (the view PR will chain it).

**Interactor rules:** trim + uppercase (`" ibm "` → `IBM`); blank → "Please enter a ticker symbol."; anything other than letters/digits/`.`/`-` (1–10 chars) → "isn't a valid ticker". Both are rejected **before** calling the API, so they don't waste requests. Then get the quote + history; a `StockDataUnavailableException` → fail view with its message.

**Tests:** `SearchStockInteractorTest` (7, using a fake data source + recording presenter) and `SearchStockPresenterTest` (4). 42 tests total, CheckStyle clean, no use_case/interface_adapter file imports an outer layer.

**Learned:**
- Output Data holds **plain values**, not entities, and the presenter does all the **formatting**. The state holds display-ready strings + chart lists.
- The controller must be called **off the EDT** (it calls the API).
- One DAO object is passed twice into the interactor (`stockDataAccess, stockDataAccess`), because it implements both small interfaces.

---

## 23. Trying Use Case 1 without a view: console demo

**Added:** `src/test/java/app/SearchStockConsoleDemo.java`, a dev-only program (in `src/test`, so it never ships). It builds the real engine like `AppBuilder` does, and **stands in for the view**: it reads a ticker from the keyboard, calls the controller, and prints the view model's state when the presenter notifies it.
- With `ALPHA_VANTAGE_API_KEY` set → real data (2 requests per search).
- Without it → **offline**, using the saved IBM responses (every ticker returns IBM).

**How to run:** in IntelliJ, open the file and click ▶ next to `main`; type tickers in the Run console. For live data, add the key under *Run → Edit Configurations → Environment variables*.

**Learned:** the view's only job is "call the controller, then redraw when the view model notifies". The demo does exactly that with `System.out` instead of Swing, which proves the engine works end to end before any UI exists.

---

## 24. How ticker matching works, "live" data, and testing with my key

**Learned:**
- **No matching on our side:** the tidied ticker is sent as `symbol=AAPL`; Alpha Vantage does an **exact** lookup. A typo (`APPL`) → empty `"Global Quote": {}` → "No quote found". Turning names into tickers is step 2 (local listing).
- **Not live:** the free quote is **end-of-day** (the last close); real-time/15-min delayed and intraday charts are **premium**. Our history = one point per day, ~100 days. That's why we show "As of <date>". In the presentation, say "end-of-day data", not "live".
- **Testing with my key:**
  1. Browser: `https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=AAPL&apikey=KEY` shows the raw JSON (1 request).
  2. Console demo: *Run → Edit Configurations → SearchStockConsoleDemo → Environment variables* → `ALPHA_VANTAGE_API_KEY`; the first line says ONLINE (2 requests per search).
  3. Compare with Google: our price = the close on our "As of" date (Google's previous close during trading hours).
  4. Run `AlphaVantageClientTest.fetchesRealQuote` with the key, then update the README's "pending" line.

---

## 25. Use Case 1, step 2: company-name search with top 5 matches

**Talked about:** typing "Apple" should find AAPL. Alpha Vantage only understands tickers, so we look names up **locally**. If what's typed isn't a known ticker, show the **top 5 name matches** and let the user pick.

**Downloaded:** Alpha Vantage's `LISTING_STATUS` (14,528 US stocks and ETFs; CSV: `symbol,name,exchange,assetType,...`) into `data/listing_status.csv` (git-ignored; the app re-downloads it if missing).

**Added:**
- Entities: `StockListing` (symbol, name, exchange) and `StockDirectory` (`findBySymbol`, `searchByName(query, limit)`). Ranking: exact name → name starts with → a word starts with → contains; ties go to the shorter name, then the ticker.
- `use_case/StockListingDataAccessInterface` (`getListings()`).
- `data_access/FileStockListingDataAccessObject`: reads the saved file; if it's missing, downloads it with the client, saves it, then reads it. Keeps it in memory after that. **This is our data persistence for search.**
- Client: `fetchListingStatus()` (CSV, not JSON; errors still come as JSON). Parser: `parseListings(csv)`.
- Use case: `SearchStockMatchesOutputData` + `prepareMatchesView` on the output boundary. The input is now a `query` (ticker or name).
- Presenter/state: `matchLabels` ("AAPL  Apple Inc  (NASDAQ)") + `matchSymbols`. Picking match *i* = `controller.execute(matchSymbols.get(i))`, so **no extra use case** is needed.
- Console demo: type a name, then a number to pick.

**Interactor flow:** blank → error. Load the list. Known ticker → quote + history. Otherwise, name matches → show the top 5. No matches but it looks like a ticker → try it anyway (e.g. `SHOP.TO`). Otherwise "No stocks match". If the list can't load, a ticker still works; a name search fails.

**Tests:** 64 total (1 live test skipped), all passing; CheckStyle clean; no inner layer imports an outer one.

**Learned:** a name search costs **0 API requests** (the local file); only showing a stock costs 2. A `@TempDir` gives tests a throwaway folder, so file-saving can be tested safely.

---

## 26. First live run: the 1-request-per-second limit

**What happened:** with my key, searching `AAPL` gave "Could not get price history for AAPL: ...spreading out your free API requests more sparingly (**1 request per second**)...". The quote request worked; the history request was sent milliseconds later and was rejected.

**Fixed:** `AlphaVantageClient.waitForRateLimit()` remembers when the last request was sent and sleeps until at least **1.1 s** has passed before sending the next. It's `synchronized` so two threads can't send at once. Searching a stock now takes about 1 extra second.

**Learned:**
- The free key has **two** limits: **25/day** and **1/second**. Both come back as HTTP 200 + `"Information"`, and our error handling showed the message correctly.
- Rate limiting is an **API detail**, so it belongs in the client (`data_access`), not the interactor.
- My key works (the quote request succeeded), so the README's "pending" line can be updated.

**Update:** after the fix, the live search works with my key. README updated: the API is confirmed working with our own key, and it now lists both limits (25/day, 1/second). Idea for later if the daily limit becomes a problem: take the quote from the daily history, so each search uses 1 request instead of 2.

---

## 27. Overview: everything in Use Case 1

**Scenario A, "apple"** (0 API requests): controller → interactor → listing DAO (saved CSV, or download once) → parser → `StockDirectory` → not a ticker → `searchByName` top 5 → `prepareMatchesView` → presenter makes labels → view model notifies the view.

**Scenario B, pick "1" / "AAPL"** (2 requests): controller → interactor → known ticker → DAO `getQuote` (GLOBAL_QUOTE) + `getDailyHistory` (TIME_SERIES_DAILY, after the 1.1 s wait) → output data (quote values + dates/closes) → presenter formats `$247.13`, `+2.46 (+1.01%)`, "As of ..." → view model → view.

**Failure:** the DAO throws `StockDataUnavailableException` → `prepareFailView(message)`.

**Four ideas:**
1. The interactor only knows interfaces (DIP / Dependency Rule).
2. `execute()` returns void; results flow out through the presenter.
3. The presenter does all the formatting (SRP).
4. Picking a match = the same use case run again with that ticker, not a new use case.

---

## 28. Team status check (2026-11-23)

**Merged into main:** my Use Case 1 (PR #27), plus everything before it.

**Open PRs / branches:**

| Who | Branch / PR | What | Has a view? |
|---|---|---|---|
| Andrew | `auth`, PR #26 | Login/signup, SQLite users, a generic `ViewModel<T>` + `ViewManagerModel` | no |
| Andrew | `stock-watchlist`, PR #31 (includes auth) | Watchlist entity/use case/adapters, SQLite storage | no |
| Matteo | `pair-analysis` (no PR yet) | Pair correlation (Use Case 3) + **`PairAnalysisView`**, wired in `AppBuilder`/`Main` | **yes** (the only one) |
| Reuben | `use-case-5-news-summary`, PR #29 | News around moves via **Finnhub** (2nd API), sentiment lexicon | no (no controller/presenter yet either) |
| Andrew | `update-project-desc`, PR #18 | README description | — |
| Matteo | `uml_diagrams` | First PlantUML drafts | — |

**Integration test:** main + watchlist + pair-analysis + news merged with **no conflicts**; **262 tests pass**, CheckStyle clean, **no Dependency Rule violations** across all code.

**Problems spotted:**
- Three different view-model styles (Andrew's generic `ViewModel<T>`, Matteo's own `PropertyChangeSupport`, my custom listener). We should pick one, probably Andrew's.
- No screen-switching yet: nothing shows more than one view.
- My terminal's `JAVA_HOME` points to Java 8, so `mvn` from the terminal fails; the project needs Java 11+.

**Course rule to remember:** each lead builds the UI for **their own** use case. I shouldn't write teammates' views; I can build mine and a shared navigation shell everyone plugs into (agreed with the team first).

---

## 29. Use Case 7 plan: semantic search ("best selling oil stocks")

**Talked about:** how to find stocks from a sentence, not just a ticker or name.

**Learned:**
- **Embedding:** a model turns text into a list of numbers (a vector). Texts with similar meaning get vectors pointing the same way, so "oil" lands near "crude", "petroleum" and "energy".
- **Cosine similarity:** how close two vectors point; 1 means the same meaning, 0 means unrelated.
- **Asymmetric search:** a short query against longer stock descriptions. We embed every description once and save it; at search time we embed only the query.
- **A name alone isn't enough.** "Exxon Mobil Corp" says nothing about oil. Each stock needs a profile text: sector, industry and a short description.
- **The sentence has two parts:**
  - the *topic* ("oil"), handled by the embedding;
  - the *ranking intent* ("best selling", "cheapest", "most traded"), handled by a rule-based parser that maps it to an entity field to sort by.
- **The request budget matters.** A quote costs 1 of our 25 daily requests, so we only fetch live prices for the top 5 results.

**Plan (CA):**

| Layer | Pieces |
|---|---|
| `entity` | `StockProfile` (symbol, name, sector, industry, description), cosine-similarity maths |
| `use_case/semantic_search` | Input/Output data and boundaries, interactor, `TextEmbeddingGateway` interface (DIP: the interactor doesn't know which model), profile data-access interface, query-intent parser |
| `data_access` | the embedding model (Java, on our CPU, or an API), a file DAO for profiles plus saved vectors |
| UI | result list; picking one calls Use Case 1's search |

**Still to decide:** where the embeddings come from (a local Java model vs a hosted API) and where the stock profiles come from.

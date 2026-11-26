# Project Name
## Team Information
### Team Members
- Arush Gupta
- Matteo Grassia
- Reuben Kurian Mathew
- Andrew Tu
---
## Project Description
### Domain
Our project aims to provide a finance terminal experience that gives users a comprehensive view of stock market data. Users will be able to search for stocks, create saved watchlists, and explore historical prices through interactive graphs with customizable date ranges. They will also be able to compare multiple stocks, analyze correlations, and view news events alongside significant price movements to help contextualize market activity.

We also aim to provide a backtesting system that allows users to evaluate quantitative trading strategies suite, such as pairs trading and moving-average crossover strategies, using historical stock data. Users will be able to configure strategy parameters, select a testing period and initial investment, and evaluate simulated performance through portfolio value graphs, trade histories, and metrics such as total return and maximum drawdown. 
By saving strategies and comparing their results against a buy-and-hold benchmark, users can explore how different approaches would have performed under past market conditions.
---
## User Stories
### User Story 1
As a retail investor, I want to search for a stock by ticker symbol or company name so I can quickly find its latest available quote.
### User Story 2
As a retail investor, I want to add and remove stocks from a saved watchlist so I can monitor them without searching for them each time.
### User Story 3
As a retail investor, I want to determine if two stocks trade in pairs.
### User Story 4
As a retail investor, I want to search for historical stock data so I can forecast future performance.
### User Story 5
As a retail investor, I want to relate the peaks and dips of each stock to a popular news event that occurred at that moment so that I can find out why the stock price fluctuated.
### User Story 6
As a retail investor, I want to get the notification for the stock price and trend, so that I can act when the price hit my desired range.

(Add additional user stories as needed.)
---
## Use Cases
### Use Case 1
Search stocks by name and ticker and view historical data.
### Use Case 2
Save stocks to a watchlist for easy reference/monitoring
### Use Case 3
Compare historical data for two stocks and calculate correlations between the stocks' daily returns.
### Use Case 4
Tell the user of popular news events that occurred at the moment a specific stock peaked or dipped.
### Use Case 5
View past strategies and view strategy performance.
### Use Case 6
User alert for stock price 
### Use Case 7
Asymmetric semantic search for stocks
### Use Case 8
N/A



(Add additional use cases as needed.)
---
## Minimal Viable Product (MVP)
| Team Member | MVP Use Case |
|-------------|--------------|
| Arush Gupta | Use Case 1,7|
| Matteo Grassia| Use Case 3,5|
| Reuben Mathew | Use Case 4,8|
| Andrew Tu | Use Case 2, 6|
---
## Entities
### PricePoint
One trading day of a stock's prices.
- **Fields:** `date`, `open`, `high`, `low`, `close`, `volume`
- **Rules:** prices are positive, the low is not above the high, volume is not negative

### PriceHistory
A stock's daily prices, always ordered from oldest to newest.
- **Fields:** `symbol`, `points` (list of `PricePoint`)
- **Methods:** `between(start, end)`, `getLatest()`, `getHighestClose()` (peak), `getLowestClose()` (dip)
- **Rules:** the symbol is not blank; no two days share a date

### Quote
The latest price of a stock and how much it moved since the previous close.
- **Fields:** `symbol`, `price`, `change`, `changePercent`, `latestTradingDay`

(More entities, such as a watchlist, will be added with their use cases.)
---
## API Usage
**API Name:** Alpha Vantage (stock market data)

**Documentation:** https://www.alphavantage.co/documentation/

**How it is used in the program:**
- `GLOBAL_QUOTE`: the latest price and daily change for a stock (search and watchlist).
- `TIME_SERIES_DAILY`: the last 100 trading days of prices for a stock (historical charts, pair comparison, peaks and dips).
- `LISTING_STATUS`: the list of every US stock and ETF (ticker, company name, exchange), downloaded once and saved to `data/listing_status.csv` so company-name searches ("apple" → AAPL) cost no API requests. Successfully called.
- Planned: `NEWS_SENTIMENT` for news around price peaks and dips (to be confirmed, as it may require a paid key).

**Called successfully?** Yes: `GLOBAL_QUOTE`, `TIME_SERIES_DAILY` and `LISTING_STATUS` all work with our own key (real responses are saved in `src/test/resources/alphavantage/` and used by our tests).

**Limits:** the free key allows 25 requests per day and 1 request per second. The client spaces requests at least 1.1 seconds apart, and tests use saved responses instead of the live API.

**Setup:** set your key in the environment variable `ALPHA_VANTAGE_API_KEY` (for example in IntelliJ: *Run* → *Edit Configurations* → *Environment variables*). Never commit a key.

**API Name:** Finnhub (company news, for Use Case 5)

**Documentation:** https://finnhub.io/docs/api/company-news

**How it is used in the program:**
- `company-news`: news articles about one company in a date range (headline, summary, source, link, publish time, related tickers). Used to explain why a stock rose or fell.

**Called successfully?** Not yet with a real key. The parser is tested against a made-up response in Finnhub's documented format (`src/test/resources/finnhub/`).

**Limits:** the free tier gives 1 year of news history and about 60 requests per minute. One request returns a limited number of articles, so long date ranges are fetched 30 days at a time.

**Setup:**
1. Sign up for a free account at https://finnhub.io/register. Your API key is shown on the dashboard.
2. Put it in the environment variable `FINNHUB_API_KEY`, the same way as the Alpha Vantage key. For example, in IntelliJ go to *Run* → *Edit Configurations* → *Environment variables* and add `FINNHUB_API_KEY=your_key`. Do this for the `Main` configuration and for any test configuration where you want the live test to run.
3. Optional check: `mvn test` runs `FinnhubClientTest.fetchesRealCompanyNews` (1 request) when the variable is set.

The key is sent in the `X-Finnhub-Token` header, not in the URL. Never commit it.

**Sentiment word list (Use Case 5, no API):** `src/main/resources/sentiment/Loughran-McDonald_MasterDictionary_1993-2025.csv` is the Loughran-McDonald Master Dictionary from the [Notre Dame Software Repository for Accounting and Finance](https://sraf.nd.edu/loughranmcdonald-master-dictionary/). It is free for academic use; see their page for the terms. A word counts as positive or negative when its `Positive` or `Negative` column holds a positive year (the year it was added); a negative year such as `-2020` means it was removed, and those words are ignored.

**Sentiment model (Use Case 5, optional, no API key):** [FinBERT](https://huggingface.co/ProsusAI/finbert) by ProsusAI (Araci, 2019, arXiv:1908.10063), a BERT model trained on financial news, labels each article positive, negative or neutral. It catches headline words the dictionary misses ("plunge", "surge", "lawsuit").
- **Files:** from the ONNX copy at [Xenova/finbert](https://huggingface.co/Xenova/finbert), pinned to commit `8f269ab`. `vocab.txt` and `config.json` are small and bundled in `src/main/resources/finbert/`. The model, `onnx/model_quantized.onnx` (111 MB), is too big for GitHub (100 MB limit), so it is downloaded instead.
- **First run:** the model is downloaded once to `~/.amnr/models/finbert-8f269ab/` (on Windows, `C:\Users\<you>\.amnr\...`) and checked against a fixed SHA-256 checksum. If the download fails (e.g. no internet), the app uses the dictionary instead.
- **Speed (measured on a cloud CPU):** about 1 s to load the model, then about 0.8 s for 64 articles.
- **Runs locally** with Microsoft's ONNX Runtime (`com.microsoft.onnxruntime:onnxruntime`, which includes Windows, macOS and Linux support). There is no Hugging Face account or key.
- **License:** FinBERT is CC BY-NC-SA 4.0, so non-commercial use only, with credit to ProsusAI.
- **Testing with the real model:** tests never download anything unless you ask them to. Either:
  - put `FINBERT_DOWNLOAD=true` in your `.env`: `LocalFinbertSentimentClassifierTest.matchesPythonOnRealHeadlines` downloads the model the same way the app will, then checks the Java results against Python's. After the first run it reuses the file. It saves to `FINBERT_MODEL_DIR` if you set it (e.g. `FINBERT_MODEL_DIR=D:/models/finbert`, with forward slashes), otherwise to `~/.amnr/models/finbert-8f269ab/`; or
  - download `onnx/model_quantized.onnx` yourself into a folder (keep the name `model_quantized.onnx`) and set `FINBERT_MODEL_DIR` to that folder; the same test then runs without downloading.

  See `src/test/resources/finbert/README.md` for where the expected answers come from.
---
## Design Diagrams
### UML Class Diagram
### Sequence Diagrams
---
## How to Run
Requires JDK 11+ and Maven.

1. Clone the repo and open it in IntelliJ as a Maven project.
2. Run `app.Main` (or `mvn compile exec:java -Dexec.mainClass=app.Main`).
3. `mvn compile` also runs CheckStyle (Google Java Style); fix any reported violations before opening a PR.
   To see the same warnings in IntelliJ, install CheckStyle-IDEA, add `checkstyle/google_checks.xml`, and set the version to 10.26.1.

API keys go in environment variables (or a local `.env`, which is git-ignored), never in source code.

### Package layout (Clean Architecture)
| Package | Layer | Contains |
|---|---|---|
| `entity` | Enterprise Business Rules | Domain objects (e.g. `Stock`) |
| `use_case/<name>` | Application Business Rules | Interactor, input/output boundaries and data, data access interface |
| `interface_adapter/<name>` | Interface Adapters | Controller, presenter, view model, state |
| `view` | Frameworks & Drivers | Swing views |
| `data_access` | Frameworks & Drivers | API clients and file persistence |
| `app` | Main component | `Main` and `AppBuilder`, which wire everything together |
---
## Notes
[Any additional project information.]

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

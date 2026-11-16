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

(Add additional user stories as needed.)
---
## Use Cases
### Use Case 1
Search stocks by name and ticker and view historical data.
### Use Case 2
Save stocks to a watchlist for easy reference/monitoring
### Use Case 3
Compare historical data for two stocks and calculate correlations between the stocks.
### Use Case 4
View stock data for a specified date range.
### Use Case 5
Tell the user of popular news events that occurred at the moment a specific stock peaked or dipped.
### Use Case 6
View past strategies and view strategy performance.

(Add additional use cases as needed.)
---
## Minimal Viable Product (MVP)
| Team Member | MVP Use Case |
|-------------|--------------|
| Name | Use Case |
| Name | Use Case |
| Name | Use Case |
---
## Entities
### Entity 1
Stock
### Entity 2
(Add additional entities as needed.)
---
## API Usage
**API Name:**
**Documentation:** [Link]
**How it is used in the program:**
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

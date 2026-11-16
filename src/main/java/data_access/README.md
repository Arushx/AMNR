# data_access (Frameworks & Drivers)

Classes that talk to the outside world: stock/news API clients and file
persistence (e.g. a JSON watchlist file). Each implements a data access
interface declared in `use_case`. Read API keys from environment variables,
never hard-code them.

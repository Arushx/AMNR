# use_case (Application Business Rules)

One sub-package per use case, e.g. `use_case/search_stock/`, containing:

- `XInputBoundary` (interface, implemented by the interactor)
- `XInputData`
- `XInteractor`
- `XOutputBoundary` (interface, implemented by the presenter)
- `XOutputData`
- `XDataAccessInterface` (interface, implemented in `data_access`)

May import from `entity` only.

Shared data access interfaces that several use cases need (e.g.
`QuoteDataAccessInterface`, `PriceHistoryDataAccessInterface`) and
`StockDataUnavailableException` live at the top level of `use_case`. Keep each
interface small: one kind of data per interface.

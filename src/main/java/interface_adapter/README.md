# interface_adapter (Interface Adapters)

One sub-package per use case, e.g. `interface_adapter/search_stock/`, containing
the `Controller`, `Presenter` and `ViewModel` for that use case. The view model holds
every field its view displays; there are no separate `State` classes.
Shared view-switching code (e.g. `ViewManagerModel`) lives at the top level.

May import from `use_case` and `entity`, never from `view` or `data_access`.

# entity (Enterprise Business Rules)

Domain objects such as `Stock`. Entities hold data and enforce domain rules.
They must not import anything from `use_case`, `interface_adapter`, `view`,
`data_access` or `app`.

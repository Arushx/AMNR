# Arush's Project Notebook

Everything Arush works on in AMNR, documented as it happens: what was built, why, how each class
fits into Clean Architecture, and what to remember for the presentation and Q&A.

- **Section 0** is the running log of each work session (newest first).
- **Sections 1 onward** are reference notes that get updated as the code changes.

The team's specification (stories, use cases) lives in the [README](../../README.md).

## Contents

0. [Session log](#0-session-log)
1. [Project at a glance](#1-project-at-a-glance)
2. [Clean Architecture in this project](#2-clean-architecture-in-this-project)
3. [Repository map](#3-repository-map)
4. [Code walkthrough](#4-code-walkthrough)
5. [Diagrams](#5-diagrams)
6. [Design decision log](#6-design-decision-log)
7. [SOLID and design patterns tracker](#7-solid-and-design-patterns-tracker)
8. [Use case tracker](#8-use-case-tracker)
9. [PR log](#9-pr-log)
10. [Rubric checklist](#10-rubric-checklist)
11. [Swing primer](#11-swing-primer)
12. [Glossary](#12-glossary)

---

## 0. Session log

Newest first. Each entry: what we did, why, files touched, and what to remember.

### 2026-11-16: Alpha Vantage API key setup (branch `Alpha-Vantage-API-Data-Access`)
- **Did:** decided where the API key lives: the `ALPHA_VANTAGE_API_KEY` environment variable, set in
  IntelliJ's run configuration and/or `~/.zshrc`. Full steps in [§4.1a](#41a-api-keys-alpha-vantage).
- **Why:** a key committed to a public repo can be stolen; reading it in `AppBuilder` and passing it to
  the DAO keeps the DAO testable (decision D5).
- **Files:** none in `src` yet; this notebook.
- **Remember:** if a key is ever committed, regenerate it; deleting the file doesn't remove it from history.

### 2026-11-16: Main constructor fix (PR #19, branch `fix/main-constructor`)
- **Did:** removed `private Main() {}` and `final` so `Main` matches ca-login; kept `invokeLater`.
- **Why:** the private constructor wasn't required by the course or CheckStyle (decision D4).
  `invokeLater` stays because Swing code belongs on the Event Dispatch Thread (decision D3).
- **Files:** `src/main/java/app/Main.java`.
- **Remember:** be able to explain the EDT in one sentence: "Swing isn't thread-safe, so all GUI work
  runs on Swing's own thread, the EDT."

### 2026-11-16: Project scaffold (PR #16, branch `fix/project-scaffold`)
- **Did:** stopped tracking `.idea/` and ignored `.env`; added org.json, OkHttp, JUnit 5 and CheckStyle
  to `pom.xml`; renamed packages to match ca-login; fixed the non-compiling `Main`; added `AppBuilder`;
  added run instructions to the README.
- **Why:** nobody could build the project, and the package names didn't match the course's CA layout
  (decisions D1, D2).
- **Files:** `.gitignore`, `pom.xml`, `checkstyle/*`, `src/main/java/**/README.md`, `app/Main.java`,
  `app/AppBuilder.java`, `README.md`.
- **Remember:** CheckStyle runs on every `mvn compile`; that's the rubric's code-quality evidence.

---

## 1. Project at a glance

| | |
|---|---|
| **Course** | CSC207H1F, Fall 2026 |
| **Team** | Arush Gupta, Matteo Grassia, Reuben Kurian Mathew, Andrew Tu |
| **Domain** | Finance: a Bloomberg-terminal-style stock viewer for retail investors |
| **Language / GUI** | Java 11+, Swing |
| **Build** | Maven, with CheckStyle (Google Java Style) run on every compile |
| **External API** | Alpha Vantage (in progress, branch `Alpha-Vantage-API-Data-Access`) |
| **Persistence** | Planned: a JSON file for the watchlist |
| **Reference design** | [CSC207-2026F-UofT/ca-login](https://github.com/CSC207-2026F-UofT/ca-login) |

User stories and use cases are in the [README](../../README.md).

---

## 2. Clean Architecture in this project

### 2.1 The layers

Clean Architecture splits the program into rings. Inner rings hold the most important,
least changeable rules; outer rings hold details (Swing, files, HTTP) that we might swap out.

| Ring (inner → outer) | Our package | Holds | May import from |
|---|---|---|---|
| Enterprise Business Rules | `entity` | Domain objects (`Stock`, ...) and the rules about them | nothing of ours |
| Application Business Rules | `use_case/<name>` | Interactor, Input/Output Boundary, Input/Output Data, Data Access Interface | `entity` |
| Interface Adapters | `interface_adapter/<name>` | Controller, Presenter, ViewModel, State | `use_case`, `entity` |
| Frameworks & Drivers | `view` | Swing panels | `interface_adapter` |
| Frameworks & Drivers | `data_access` | API clients, file persistence | `use_case`, `entity` |
| Main component | `app` | `Main`, `AppBuilder` | everything (it wires the layers together) |

### 2.2 The Dependency Rule

> Source code dependencies only point **inward**. Nothing in an inner ring may name anything
> declared in an outer ring.

**How we check it:** read the `import` lines at the top of a file. For example, an `entity` class
importing `javax.swing` or anything from `interface_adapter` is a violation. IntelliJ can also draw
a dependency graph (right-click `src/main/java` → *Analyze* → *Dependencies*).

**How information still flows outward:** the Dependency Inversion Principle. The use case layer
*declares* an interface (for example `OutputBoundary`), and an outer class *implements* it (the
`Presenter`). The interactor calls the interface, so at runtime data flows outward, while the
source code dependency (`Presenter implements OutputBoundary`) still points inward.

### 2.3 The CA engine: one round trip

Every use case follows the same seven steps (course notes, chapter 11.4):

1. **View → Controller:** a Swing listener fires and calls the controller with raw input.
2. **Controller → Input Boundary:** the controller packs the input into an `InputData` object and
   calls `execute(inputData)` on the input boundary, which the interactor implements.
3. **Interactor → Data Access Interface:** the interactor asks for the data it needs.
4. **Data Access → Database/API:** the concrete DAO reads the file or calls the API and builds entities.
5. **Interactor → Entities:** the interactor applies the business rules using the entities.
6. **Interactor → Output Boundary:** the interactor builds an `OutputData` object and calls the
   presenter through the output boundary (`prepareSuccessView` / `prepareFailView`).
7. **Presenter → ViewModel:** the presenter turns the output into display strings and updates the
   view model, which notifies the view, and the view redraws.

The controller never gets a return value. Results always come back through the presenter.

### 2.4 Where the engine is built

`AppBuilder` creates one engine per use case: it constructs the DAO, presenter, interactor and
controller and passes each into the next one's constructor (dependency injection). This is the only
place that names concrete classes from several layers, which is why it lives outside the rings.

---

## 3. Repository map

```
AMNR/
├── README.md                 Specification: user stories, use cases, MVP, entities, API
├── TeamContract.md           Team norms
├── pom.xml                   Maven build: dependencies + CheckStyle
├── checkstyle/
│   ├── google_checks.xml     Course's pinned Google Java Style rules (CheckStyle 10.26.1)
│   └── checkstyle-suppressions.xml   Per-file exemptions (empty; keep it that way)
├── docs/
│   ├── arush/ARUSH.md        This notebook
│   └── diagrams/             PlantUML sources (.puml), not created yet
└── src/
    ├── main/java/
    │   ├── app/              Main component: Main, AppBuilder
    │   ├── entity/
    │   ├── use_case/
    │   ├── interface_adapter/
    │   ├── view/
    │   └── data_access/
    └── test/java/            JUnit 5 tests, mirroring the main package layout
```

---

## 4. Code walkthrough

One subsection per class, written when the class is added and updated when it changes.
Each entry says: **layer**, **responsibility**, **depends on**, **used by**, then a walkthrough.

### 4.1 Build and configuration files

**`pom.xml`**
- `maven.compiler.source/target = 11`: the code must compile as Java 11, so teammates on newer JDKs
  can't accidentally use features others can't run.
- Dependencies:
  - `org.json:json`: parse API responses and read/write our JSON save file (`JSONObject`, `JSONArray`).
  - `com.squareup.okhttp3:okhttp`: send HTTP requests to the API.
  - `org.junit.jupiter:junit-jupiter` (test scope): unit tests; only on the classpath for `src/test`.
- `maven-checkstyle-plugin`, bound to the `validate` phase: runs before compilation, so
  `mvn compile` **fails** on any style violation (`failsOnError` + `violationSeverity=warning`).
  This is our evidence of code-quality tooling for the rubric.

**`.gitignore`**
- `.idea/`: IntelliJ settings are personal, so they never go into Git.
- `.env`, `*.env`: ignored in case anyone keeps keys in such a file. Keys are never committed.

### 4.1a API keys (Alpha Vantage)

The key lives **outside the repo**, in an environment variable named `ALPHA_VANTAGE_API_KEY`.
Each teammate sets it on their own machine; it never appears in code, commits, PRs or chat.

**Set it in IntelliJ (recommended, applies when you press Run):**
1. *Run* → *Edit Configurations...* → select the `Main` configuration (run `app.Main` once if it isn't there).
2. In *Environment variables*, click the list icon, add `ALPHA_VANTAGE_API_KEY` with your key as the value.
3. *Apply*. IntelliJ stores this in `.idea/workspace.xml`, which is git-ignored.

**Set it for the terminal (for `mvn` runs), in `~/.zshrc`:**
```bash
export ALPHA_VANTAGE_API_KEY="your-key-here"
```
Then open a new terminal tab.

**Reading it in code:** only the `app` layer reads the variable, and passes the key into the DAO's
constructor:

```java
String apiKey = System.getenv("ALPHA_VANTAGE_API_KEY");   // in AppBuilder
new AlphaVantageStockDataAccessObject(apiKey);             // DAO receives it, never looks it up itself
```

Why in `AppBuilder` and not inside the DAO: the DAO stays easy to test (a test can pass any key),
and all configuration lives in one place, the Main component. If the variable is missing,
`getenv` returns `null`, so the app should show a clear "API key not set" message instead of crashing.

**If a key is ever committed:** deleting the file isn't enough, because it stays in Git history.
Regenerate the key on the Alpha Vantage site so the old one stops working.

### 4.2 `app.Main`

| | |
|---|---|
| **Layer** | Main component (outside the CA rings) |
| **Responsibility** | Start the program |
| **Depends on** | `AppBuilder`, `javax.swing.JFrame`, `javax.swing.SwingUtilities` |
| **Used by** | The JVM (it's the entry point) |

```java
public class Main {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        () -> {
          final JFrame application = new AppBuilder().build();
          application.pack();
          application.setLocationRelativeTo(null);
          application.setVisible(true);
        });
  }
}
```

- `public static void main(String[] args)`: where Java starts running. `args` is unused.
- `SwingUtilities.invokeLater(() -> { ... })`: Swing code must run on the **Event Dispatch Thread**
  (EDT), Swing's own thread for drawing and handling clicks. `main` runs on a different thread, so we
  queue the GUI setup onto the EDT. See [§11](#11-swing-primer) and decision [D3](#6-design-decision-log).
- `new AppBuilder().build()`: `Main` doesn't know how the app is assembled; it delegates to
  `AppBuilder` (SRP: starting vs. assembling are different jobs).
- `pack()`: sizes the window to fit its contents' preferred sizes.
- `setLocationRelativeTo(null)`: centres the window on screen.
- `setVisible(true)`: shows it. Until this call the window exists only in memory.

### 4.3 `app.AppBuilder`

| | |
|---|---|
| **Layer** | Main component (outside the CA rings) |
| **Responsibility** | Build every CA engine and put every view into one window |
| **Depends on** | Swing (`JFrame`, `JPanel`, `JLabel`, `CardLayout`); later, concrete classes from every layer |
| **Used by** | `Main` |

```java
public class AppBuilder {
  private static final String TITLE = "AMNR";
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel cardPanel = new JPanel(cardLayout);

  public JFrame build() {
    if (cardPanel.getComponentCount() == 0) {
      cardPanel.add(new JLabel(TITLE + " - no views yet", SwingConstants.CENTER), "placeholder");
    }
    final JFrame application = new JFrame(TITLE);
    application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    application.add(cardPanel);
    return application;
  }
}
```

- `TITLE`: the window title. `private static final` = one shared constant, can't be changed.
  The final product name is still undecided (TODO in the code).
- `cardLayout` / `cardPanel`: a panel whose children are stacked like cards with one visible at a
  time. Each use case's view will be one card; switching screens means showing a different card.
- `build()`:
  - If no views were added yet, add a centred placeholder label so the window isn't blank.
    This goes away once the first real view exists.
  - Create the window (`JFrame`) with the title.
  - `EXIT_ON_CLOSE`: closing the window ends the program (the default only hides the window).
  - Put the card panel in the window and return it. `Main` makes it visible.

**How it will grow (Builder pattern, as in ca-login):** each use case lead adds two methods that
return `this`, so `Main` can chain them:

```java
JFrame app = new AppBuilder()
    .addSearchView()        // creates the view model + view, adds the view as a card
    .addSearchUseCase()     // creates DAO, presenter, interactor, controller; gives the controller to the view
    .build();
```

Adding a use case means **adding** methods; nothing existing is edited (Open/Closed Principle).

---

## 5. Diagrams

Diagrams will be PlantUML sources in `docs/diagrams/`. To view them in IntelliJ, install the
**PlantUML Integration** plugin; a live preview appears beside the `.puml` file. Export PNGs for the
slides from that preview.

| Diagram | File | Shows | Status |
|---|---|---|---|
| CA layer overview | `ca-layers.puml` | Each package, the classes in it, and allowed dependencies | Not created yet |
| Class diagram | `class-diagram.puml` | Every class with fields, methods and relationships | Not created yet |
| Sequence: one per use case | `sequence-<use-case>.puml` | The seven CA engine steps for that use case | Not created yet |

**Rules for diagrams**
- Update the class diagram in the same PR as the code change.
- Each MVP use case gets its own sequence diagram, written by its lead.
- Arrows on the CA diagram must only point inward. If you can't draw it that way, the code has a
  Dependency Rule violation.

---

## 6. Design decision log

Short records of decisions we may be asked to justify in the presentation.

| # | Decision | Why | Alternatives considered | PR |
|---|---|---|---|---|
| D1 | Package names `entity`, `use_case`, `interface_adapter`, `view`, `data_access`, `app` | Matches ca-login, so TAs recognise the layout, and each package maps to exactly one CA ring | Our earlier `adapter`/`infrastructure` names | #16 |
| D2 | CheckStyle with the course's Google Java Style config, failing the build on violations | Consistent style across 4 people; the rubric requires CheckStyle evidence for code quality ≥ 4/5 | IntelliJ formatter only (no enforcement) | #16 |
| D3 | Build the GUI inside `SwingUtilities.invokeLater` | Swing is not thread-safe; all GUI work belongs on the EDT. Matters once API calls run in the background | Calling `build()` directly from `main` like ca-login (works at startup, but technically breaks the rule) | #16 |
| D4 | No private constructor on `Main` | Not required by the course or CheckStyle; matching ca-login keeps it simple | `private Main() {}` utility-class idiom | #19 |
| D5 | API key read from the `ALPHA_VANTAGE_API_KEY` environment variable in `AppBuilder`, passed into the DAO | Keys in a public repo can be stolen and abused; injecting the key keeps the DAO testable | Hard-coding the key; a `config.properties` file (easy to commit by accident) | #16, guide |

---

## 7. SOLID and design patterns tracker

The rubric wants a rich discussion of **at least two** SOLID principles, a design pattern **beyond
the starter code**, and an **extensibility** example. Record concrete evidence here as it appears.

| Principle / pattern | Where in our code | Status |
|---|---|---|
| **SRP** | `Main` only starts the program; `AppBuilder` only assembles it. Later: each use case's controller, presenter and interactor each have one reason to change | Started |
| **OCP** | New use cases are added as new `addX` methods in `AppBuilder` without changing existing ones | Started |
| **LSP** | Any implementation of a data access interface (real API DAO, in-memory test DAO) must work wherever the interface is expected | Planned |
| **ISP** | One small data access interface per use case instead of one big one | Planned |
| **DIP** | Interactors depend on boundary and data access *interfaces*; `AppBuilder` plugs in the concrete classes | Planned |
| Builder | `AppBuilder` chaining (from ca-login, so it **does not count** as "beyond starter code") | Started |
| Observer | View models notify views (from ca-login, **does not count**) | Planned |
| **Pattern beyond starter code** | Candidates: *Strategy* (swappable correlation/indicator calculations for pair trading), *Adapter* (wrap Alpha Vantage so another provider can be swapped in), *Facade* | To decide |
| **Extensibility example** | Candidate: switching stock data provider by writing one new DAO class and changing one line in `AppBuilder` | To demonstrate |

---

## 8. Use case tracker

| # | Use case | Lead | Branch / PR | Sequence diagram | Tests | Status |
|---|---|---|---|---|---|---|
| UC1 | Search a stock by ticker or name and view its quote | TBD | | | | Not started |
| UC2 | Add/remove stocks on a saved watchlist | TBD | | | | Not started |
| UC3 | Compare two stocks' historical prices (pair trading) | TBD | | | | Not started |
| UC4 | View historical data for a date range | TBD | | | | Not started |
| UC5 | Link price peaks/dips to news events | TBD | | | | Not started |
| — | Alpha Vantage data access (shared by several use cases) | Arush | `Alpha-Vantage-API-Data-Access` | | | In progress |

Each use case is "done" when it has: interactor + boundaries + data, controller, presenter, view
model, view, `AppBuilder` wiring, interactor unit tests, a sequence diagram, and a walkthrough in §4.

---

## 9. PR log

| PR | Title | Author | What it changed |
|---|---|---|---|
| #16 | Fix project scaffold | Arush | Untracked `.idea/`, ignored `.env`; added org.json, OkHttp, JUnit 5 and CheckStyle; renamed packages to the ca-login layout; fixed the non-compiling `Main` and added `AppBuilder` |
| #19 | Remove private constructor from Main | Arush | `Main` now matches ca-login; kept `invokeLater` |

Earlier PRs (#2–#15) covered the team contract, project ideas, user stories and the initial folder structure.

---

## 10. Rubric checklist

| Rubric item (marks) | What we need | Status |
|---|---|---|
| Specification (10) | README stories and use cases in plain language, "what" not "how", full use case format | Needs rewrite |
| Scope (5) | One significant, distinct story per member; fix overlap between stories 1 and 4 | Needs work |
| API (5) | Alpha Vantage used meaningfully; slides explain specifics | In progress |
| Persistence (5) | Demo shows **saving and loading** (close and reopen the app) | Not started |
| Clean Architecture (10) | Architecture diagrams in slides; no Dependency Rule violations | Diagrams started |
| SOLID & patterns (10) | ≥2 SOLID principles discussed richly; one pattern beyond the starter code; extensibility example | See §7 |
| Code quality (10) | CheckStyle evidence; a representative PR with review to show | CheckStyle done |
| Demo (10) | Every member's story working end to end | Not started |
| Slides / delivery / Q&A (35) | 15 minutes, everyone speaks, diagrams from §5 | Later |

---

## 11. Swing primer

| Class | What it is |
|---|---|
| `JFrame` | A window with a title bar and close button |
| `JPanel` | An invisible container that holds other components; panels nest |
| `JLabel`, `JButton`, `JTextField`, `JTable`, `JComboBox` | The things the user sees and uses |
| Layout managers | Decide where a panel's children go: `BorderLayout` (N/S/E/W/centre), `BoxLayout` (a row or column), `GridLayout`, `CardLayout` (one child visible at a time) |

**Listeners** connect user actions to code:

```java
searchButton.addActionListener(e -> searchController.execute(tickerField.getText()));
```

**The Event Dispatch Thread (EDT).** Swing draws the screen and runs every listener on one special
thread. Rules: (1) create and change components only on the EDT (`invokeLater` gets you there);
(2) never do slow work, like an API call, on the EDT, or the window freezes until it finishes.
Slow work goes on a background thread (for example `SwingWorker`), and the result is handed back
to the EDT to update the screen.

**How Swing fits CA:** Swing classes appear only in `view` and `app`. The view calls its controller
and listens to its view model (Observer pattern); it never talks to an interactor or DAO directly.
Replacing Swing would only touch those two packages.

---

## 12. Glossary

| Term | Meaning |
|---|---|
| **Entity** | A domain object holding critical business data and rules (e.g. `Stock`) |
| **Interactor** | The class that carries out one use case; implements the input boundary |
| **Input / Output Boundary** | Interfaces in the use case layer: the controller calls the input boundary; the interactor calls the output boundary |
| **Input / Output Data** | Plain objects carrying data into and out of the interactor |
| **DAO** | Data Access Object: reads/writes files or calls APIs; implements a data access interface |
| **Controller** | Turns raw UI input into `InputData` and starts the use case |
| **Presenter** | Turns `OutputData` into display values and updates the view model |
| **ViewModel / State** | Holds what the view should display; notifies the view on change |
| **Dependency injection** | Passing an object its collaborators (usually through the constructor) instead of it creating them |
| **Dependency Rule** | Source code dependencies point only inward |
| **EDT** | Event Dispatch Thread: Swing's single GUI thread |
| **MVP** | Minimum Viable Product: the use cases we commit to finishing |

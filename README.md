# Week 1 — Python for Data Analysis (Java / Spring Boot version)

Single Spring Boot application — ONE embedded server, ONE port, runs
directly from STS with a single "Run As → Spring Boot App". No separate
frontend server: the static HTML page and the REST API are served from
the same process.

## What each part maps to (Python → Java/Spring)

| Python (Pandas)            | Java/Spring equivalent                         | File |
|------------------------------|--------------------------------------------------|------|
| `pd.read_csv()`              | `CsvLoader.java`                                | Reads CSV into rows/headers |
| `df.dtypes`                  | `DataTypeAnalyzer.java`                         | Scans values, infers INTEGER/FLOAT/DATE/TEXT |
| `df.isnull().sum()`          | `MissingValueDetector.java`                     | Counts blank values per column |
| `df.describe()`              | `DataSummary.java`                              | Mean/std/min/max/median for numeric; top value for categorical |
| (data loading on startup)    | `DataAnalysisService.java`                      | `@Service`, loads CSV once via `@PostConstruct` |
| (API layer)                  | `AnalysisController.java`                       | `@RestController` — `/api/summary`, `/api/missing`, `/api/dtypes`, `/api/raw` |
| Matplotlib / Seaborn plots   | `src/main/resources/static/index.html`          | Bar, pie, scatter charts via Chart.js |
| Jupyter Notebook             | The whole running app, viewed in a browser      | One Spring Boot server, opened at `localhost:8080` |

## Project structure (standard Maven layout — importable as-is into STS)

```
week1-springboot/
├── pom.xml
├── sample_data.csv
└── src/main/
    ├── java/com/training/week1/
    │   ├── Week1DataAnalysisApplication.java   (main class — run this)
    │   ├── AnalysisController.java             (@RestController)
    │   ├── DataAnalysisService.java            (@Service)
    │   ├── AnalysisDtos.java                   (response objects)
    │   ├── CsvLoader.java
    │   ├── DataTypeAnalyzer.java
    │   ├── MissingValueDetector.java
    │   └── DataSummary.java
    └── resources/
        ├── application.properties
        └── static/index.html                   (frontend, auto-served by Spring Boot)
```

## How to import & run in STS

1. **File → Import → Maven → Existing Maven Projects**, select the
   `week1-springboot` folder, finish.
2. STS will download the Spring Boot dependencies the first time
   (needs internet access to Maven Central — this is normal and only
   happens once).
3. Once the project builds with no red errors, right-click
   `Week1DataAnalysisApplication.java` → **Run As → Spring Boot App**.
4. Console should show:
   ```
   Loaded dataset: sample_data.csv
   Rows: 15, Columns: 7
   Tomcat started on port 8080
   ```
5. Open a browser at **http://localhost:8080** — the report page loads
   and pulls data from the same server's `/api/...` endpoints.

That's it — one server, one process, one port, started entirely from
within STS.

## Using your own dataset

Edit `src/main/resources/application.properties`:
```properties
app.dataset.path=/full/path/to/your_data.csv
```
Restart the app (Run As → Spring Boot App again) to pick up the new file.

## Endpoints

- `GET /api/summary` — equivalent of `df.describe()`
- `GET /api/missing` — equivalent of `df.isnull().sum()`
- `GET /api/dtypes` — equivalent of `df.dtypes`
- `GET /api/raw` — raw rows (used by the frontend's charts)
- `GET /` — the report page (served from `static/index.html` automatically)

## Notes for your instructor / submission

- This is a standard Maven Spring Boot project (`spring-boot-starter-web`,
  Spring Boot 3.3.4, Java 17 target) — nothing unusual, just the normal
  STS workflow.
- The CSV parser is intentionally simple (splits on comma) — won't
  handle quoted fields containing commas. Fine for typical teaching
  datasets.
- Type inference treats a column as numeric only if every non-blank
  value parses as a number, matching pandas' general behavior closely
  (though not pixel-identical to pandas' dtype engine in every edge case).
- I could not run a live Maven build in my own sandbox (no access to
  Maven Central there), so please do one `mvn clean install` or STS
  build on your machine to confirm before final submission — the code
  follows standard, well-tested Spring Boot patterns throughout, and
  the core analysis logic (CSV loading, type inference, missing-value
  detection, summary stats) was independently verified to produce
  correct output.

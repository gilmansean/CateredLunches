# Catered Lunch Menu

A small Spring Boot CLI application for managing a weekly catered lunch menu. Reads and writes menu data to a local JSON
file and provides a simple interactive console menu to view, add, and delete weeks.

## Features

- **View the full calendar** — lists every stored week, numbered and dated (e.g. `Week 1: 2026-07-13`), with each day's
  menu item.
- **View a single week** — look up one week by its start date without scrolling through the whole calendar.
- **Add a week** — walks through each weekday (Mon–Fri) prompting for a menu item, then validates and persists the week.
- **Delete a week** — requires an explicit `y`/`yes` confirmation before removing a week, to avoid accidental data loss.
- **Pluggable persistence** — the storage backend is selected via configuration, not hardcoded (
  see [Configuration](#configuration) below).

## Tech Stack

- Java 14, Spring Boot 2.5.15
- Gson for JSON serialization
- JUnit 5 + Mockito for testing

## Architecture

The application uses a small layered design:

- `CommandLineService` runs the interactive loop.
- `MenuService` routes the user's menu choice.
- `CalendarService` coordinates calendar operations and output formatting.
- `MenuRepository` separates the application logic from persistence.
- `FileMenuRepository` validates and stores menu data as JSON using atomic file replacement.

## Data Model

Each week is stored under an ISO-8601 date key representing its Monday start date. A week contains a list of days, each
with a `dayOfTheWeek` (`MONDAY`–`SUNDAY`) and a `menuItem`:

```json
{
  "weeks": {
    "2026-07-13": [
      {
        "dayOfTheWeek": "MONDAY",
        "menuItem": "Tacos"
      },
      {
        "dayOfTheWeek": "TUESDAY",
        "menuItem": "Pasta Primavera"
      }
    ]
  }
}
```

The included `calendar.data.json` contains sample data so the application has a populated menu on its first run. You can
edit or replace it without changing application code.

## Build and Run

### Prerequisites

- JDK 14 or newer
- No separate Maven installation is required; the Maven Wrapper is included.

Build and run all tests:

```shell
./mvnw clean test
```

The current suite contains 77 unit and integration tests covering the model, validation, services, file repository, and
Spring application context.

Run the application:

```shell
./mvnw spring-boot:run
```

Example:

```text
#################### Catered Lunch Menu ####################

Week 1: 2026-07-13
    | Mon    |Tue      |Wed      |Thu   |Fri   |
    | pizza  |hot dogs |burgers  |tacos |salad |

a: Add Week
v: View Week
d: Delete Week
q: Quit
Choice=>
```

## Configuration

### Choosing a persistence backend

The app selects its `MenuRepository` implementation via the `menu.repository.type` property:

| Value            | Implementation        | Notes                                                                                                                    |
|------------------|-----------------------|--------------------------------------------------------------------------------------------------------------------------|
| `file` (default) | `FileMenuRepository`  | JSON file on disk, path via `CALENDAR_DATA_PATH` env/property (defaults to `./calendar.data.json`)                       |
| `mysql`          | `MySqlMenuRepository` | **Stub only** — every method throws `UnsupportedOperationException`. Demonstrates the swap-in pattern; no DB wiring yet. |

Set it in `application.properties`:

```properties
menu.repository.type=file
```

Override the sample-data location with an environment variable:

```shell
CALENDAR_DATA_PATH=/path/to/calendar.json ./mvnw spring-boot:run
```

## License

Licensed under the [Apache License 2.0](LICENSE).

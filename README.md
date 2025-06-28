# pheide-kt

A Kotlin web application using Ktor, JetBrains Exposed, and SQLite.

## Features

- Ktor-based web server
- SQLite database with JetBrains Exposed ORM
- Page and Tab management
- Simple template rendering

## Database Schema

- **page**: `id`, `title`, `header_css_id`, `is_default`
- **tab**: `id`, `page_id`, `title`, `aside`, `content`, `sorting`, `type`

## Getting Started

### Prerequisites

- JDK 21+
- Gradle

### Setup

1. **Clone the repository**

   ```sh
   git clone <repo-url>
   cd pheide-kt
   ```

2. **Build the project**

   ```sh
   ./gradlew build
   ```

3. **Run the application**

   ```sh
   ./gradlew run
   ```

   The server will start and create `data.db` in the project root if it does not exist.

4. **Database Initialization**

   The database schema and test data are created automatically on first run.

5. **E2e tests**

   For end-to-end testing, ensure you have Node.js and npm installed. Then, set up Playwright:

   ```
   npm init -y
   npm install -D @playwright/test
   npm install --save-dev cross-env
   npx playwright install
   ```

   Run the test env via gradlew:
   ```
   DB_NAME=test.db ./gradlew run
   ```

   Then run end-to-end tests:
   ```sh
   npm run test:e2e
   ```

   To manually login and reset the test DB:
   ```
   http://localhost:8080?controller=auth&action=login
   (admin, pass)
   http://localhost:8080?controller=admin&action=reset
    ```

### Project Structure

- `src/main/kotlin/` - Kotlin source code
- `src/main/resources/templates/` - HTML templates
- `scripts/init_db.sql` - Example SQL for schema and test data

### Dependencies

- [Ktor](https://ktor.io/)
- [JetBrains Exposed](https://github.com/JetBrains/Exposed)
- [SQLite JDBC](https://github.com/xerial/sqlite-jdbc)
- [Logback](https://logback.qos.ch/)

## License

MIT


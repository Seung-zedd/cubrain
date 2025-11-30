# Railway Database Setup for PgVector

The application requires a PostgreSQL database with the `pgvector` extension enabled.
The standard managed PostgreSQL service on Railway might not have this extension available by default or enabled.

## Option 1: Enable Extension on Existing Database (If supported)

1.  Go to your Railway project dashboard.
2.  Select your PostgreSQL service.
3.  Go to the **Data** tab.
4.  Run the following SQL query:
    ```sql
    CREATE EXTENSION IF NOT EXISTS vector;
    ```
5.  If you see an error like `ERROR: extension "vector" is not available`, you **MUST** use Option 2.

## Option 2: Deploy a Dockerized PostgreSQL with PgVector (Recommended)

If the managed service doesn't support it, you need to deploy a custom Docker image.

1.  In your Railway project, click **+ New** -> **Service** -> **Docker Image**.
2.  Enter the image name: `pgvector/pgvector:pg16` (or `pg17` if you prefer).
3.  Click **Add**.
4.  Once added, go to the service **Settings** -> **Variables**.
5.  Add the following environment variables (you can copy values from your old DB or set new ones):
    *   `POSTGRES_USER`: `postgres` (or your preferred user)
    *   `POSTGRES_PASSWORD`: (a strong password)
    *   `POSTGRES_DB`: `railway` (or your preferred db name)
6.  Go to the **Settings** tab of this new service.
7.  Under **Networking**, note the internal host (e.g., `postgres-custom.railway.internal`) and port (`5432`).
8.  **Update your Backend Service Variables**:
    *   Go to your Spring Boot backend service settings.
    *   Update `PGHOST` to the new service's internal host.
    *   Update `PGPORT` to `5432`.
    *   Update `PGUSER`, `PGPASSWORD`, `PGDATABASE` to match what you set in step 5.
9.  **Redeploy** your backend service.

## Verification

The application will automatically try to run `CREATE EXTENSION IF NOT EXISTS vector;` on startup. If the database supports it, the application will start successfully.

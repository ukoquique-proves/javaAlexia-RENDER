# Deploying to Render

This guide provides step-by-step instructions for deploying the Alexia application to Render using Docker and connecting it to a PostgreSQL database.

## Prerequisites

1.  **GitHub Repository**: The project code must be pushed to a GitHub repository.
2.  **Render Account**: A free account on [Render](https://render.com/).
3.  **PostgreSQL Database**: A PostgreSQL database. You can use Render's managed PostgreSQL, which is recommended for the free tier.

---

## Deployment Steps

### Step 1: Create a New Web Service on Render

1.  Log in to your Render account.
2.  Click **New +** and select **Web Service**.

### Step 2: Connect Your Repository

1.  Select **Build and deploy from a Git repository**.
2.  Connect your GitHub account if you haven't already.
3.  Choose your project repository.

### Step 3: Configure the Service

Render will automatically detect the `render.yaml` in your repository.

1.  **Name**: Choose a name for your service (e.g., `alexia-app`).
2.  **Region**: Select the region closest to you or your users.
3.  **Branch**: Select `main` (or your default branch).
4.  **Root Directory**: Leave blank (uses repository root).
5.  **Environment**: Select **Docker**.
6.  **Dockerfile Path**: Should auto-detect `./Dockerfile`.

### Step 4: Configure Instance Type

1.  **Instance Type**: Select **Free** for testing, or a paid tier for production.
2.  **Auto-Deploy**: Enable this to automatically deploy when you push to GitHub.

### Step 5: Set Up Database

**Important**: Render's free tier has network restrictions and **CANNOT** connect to external databases like Supabase. You **MUST** use Render's own managed PostgreSQL.

#### Use Render Managed PostgreSQL (Recommended) ⭐

**Step 5.1: Create PostgreSQL Database**

1. In Render Dashboard → **New +** → **PostgreSQL**
2. Configure it, and once it's created, get the connection details.

**Step 5.2: Get Connection Details**

Once created, you'll see the **Internal Database URL**.

**Step 5.3: Convert to JDBC Format**

Spring Boot requires JDBC format. Convert the URL from `postgresql://user:pass@host/database` to `jdbc:postgresql://host/database`.

**Step 5.4: Configure Environment Variables**

The project is configured to use standard `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD` variables.

### Step 5.5: Database Schema Configuration

**Important**: The application uses Hibernate to automatically create the database schema. For the **first deployment** with a fresh database, the configuration is set to `spring.jpa.hibernate.ddl-auto=create` to build all tables and indexes from your Java entities.

**After successful deployment**, update the configuration back to `update`:
1. In your project, edit `src/main/resources/application-prod.properties`
2. Change `spring.jpa.hibernate.ddl-auto=create` to `spring.jpa.hibernate.ddl-auto=update`
3. Commit and push the change

This prevents the database from being recreated on every deployment while still allowing schema updates when you add new entities.

### Step 6: Deploy

1.  Click **Create Web Service**.

Render will build the Docker image and deploy your application. This may take 5-10 minutes on the first build.

---

## ⚠️ Common Deployment Failures

- **Using External DB on Free Tier**: Render's free tier blocks connections to external databases. You must use their internal PostgreSQL.
- **Incorrect Environment Variables**: Ensure `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD` are set correctly.
- **Wrong JDBC Format**: The URL must start with `jdbc:postgresql://`.
- **Vaadin Production Mode**: The `pom.xml` must be configured to build the Vaadin frontend for production. Without this, the app will crash.

---

## ✅ Success Checklist

- [ ] Use Render's PostgreSQL on the free tier.
- [ ] Set `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` environment variables.
- [ ] Ensure `pom.xml` has the `vaadin-maven-plugin` for production builds.
- [ ] Verify the application is live and the database connection works.

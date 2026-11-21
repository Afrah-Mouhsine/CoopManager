# 🌾 CoopManager

Full-stack web application for managing an agricultural cooperative: members, harvest production, revenue distribution, finances and meeting minutes, with role-based access and an analytics dashboard.

> Final-year project (Licence Professionnelle in Full Stack Development & DevOps, Faculté des Sciences Aïn Chock, Casablanca).

## ✨ Features

| Module | What it does |
|---|---|
| **Authentication** | Login with JWT (24 h access token, 7-day refresh token) and automatic token refresh on the client |
| **Members** | Create, edit, list members; deactivate / reactivate accounts |
| **Production** | Members declare harvests (product type, quantity in kg, harvest date). The president or treasurer validates or rejects them. Production dashboard included |
| **Revenue distribution** | Enter total revenue and operating costs for a period. The net revenue is shared between active members in proportion to their **validated** production. Run it as a *simulation* first, then finalize. Members can see their own share |
| **Finance** | Income and expense transactions by category (sale, subsidy, inputs, logistics, equipment, salary, maintenance…), financial summary, **Excel export** |
| **Meeting minutes** | Create, edit, publish and delete minutes |
| **Dashboard** | Overview with charts (Recharts) |

### Roles and permissions

| Role | Access |
|---|---|
| `PRESIDENT` | Everything: manage members, minutes, validate production, revenue, finance |
| `TREASURER` | Finance, production validation, revenue distribution |
| `MEMBER` | Declare own production, view members, minutes and own revenue share |

## 🛠️ Tech stack

**Backend**
- Java 17, Spring Boot 3.2
- Spring Security + JWT (jjwt), method-level security with `@PreAuthorize`
- Spring Data JPA / Hibernate, PostgreSQL 16
- Bean Validation, global exception handler
- MapStruct, Lombok
- Apache POI (Excel export)
- OpenAPI / Swagger UI (springdoc)
- JUnit 5 + Mockito (service tests)

**Frontend**
- React 18, TypeScript, Vite
- Tailwind CSS
- React Router, Zustand (auth state), TanStack React Query, React Hook Form
- Axios (with JWT interceptor), Recharts, Lucide icons
- Vitest + Testing Library

**DevOps**
- Docker multi-stage builds (backend and frontend)
- Docker Compose (PostgreSQL + backend + frontend)
- Nginx serves the React build and proxies `/api` to the backend

## 📁 Project structure

```
coopmanager/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/coopmanager/
│       ├── config/        # Security, OpenAPI, exception handler, data seeder
│       ├── controller/    # REST controllers (auth, members, production, revenue, finance, minutes)
│       ├── dto/           # Request / response objects
│       ├── entity/        # JPA entities
│       ├── repository/    # Spring Data repositories
│       ├── security/      # JWT service and authentication filter
│       └── service/       # Business logic and Excel export
├── frontend/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── api/           # Axios client and API calls
│       ├── components/    # Layout, shared UI
│       ├── pages/         # Dashboard, Members, Production, Revenue, Finance, Minutes, Login
│       ├── store/         # Zustand auth store
│       └── types/
└── docker-compose.yml
```

## 🚀 Getting started

### Option 1: Docker (recommended)

Requirements: Docker and Docker Compose.

```bash
git clone https://github.com/<your-username>/coopmanager.git
cd coopmanager
docker compose up --build
```

| Service | URL |
|---|---|
| Application | http://localhost |
| REST API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

### Option 2: Run locally

Requirements: JDK 17, Maven, Node.js 20+, PostgreSQL 16.

**1. Database**

```sql
CREATE DATABASE coopmanager;
```

**2. Backend**

```bash
cd backend
# optional: override defaults
# export DB_USER=postgres DB_PASSWORD=postgres JWT_SECRET=<your-secret>
mvn spring-boot:run
```

The API starts on http://localhost:8080. Tables are created automatically (`ddl-auto: update`) and demo data is seeded on first start.

**3. Frontend**

```bash
cd frontend
npm install
npm run dev
```

The app starts on http://localhost:5173 and proxies `/api` to the backend.

## 🔑 Demo accounts

Demo data is seeded automatically when the database is empty.

| Role | Email | Password |
|---|---|---|
| President | `president@coopmanager.ma` | `President@123` |
| Treasurer | `tresorier@coopmanager.ma` | `Tresorier@123` |
| Member | `ahmed@coopmanager.ma` | `Membre@123` |

> ⚠️ These accounts are for development and demos only. Remove or change them before any real deployment.

## 🔌 API overview

| Prefix | Description |
|---|---|
| `POST /api/auth/login`, `/api/auth/refresh` | Authentication |
| `/api/members` | Member management |
| `/api/production` | Declare, list, validate, reject production, dashboard |
| `/api/revenue` | Calculate (simulation or final), finalize, list, my share |
| `/api/finance` | Transactions, summary, Excel export |
| `/api/minutes` | Meeting minutes |

The full interactive documentation is available in Swagger UI once the backend is running.

## 🧪 Tests

```bash
# Backend (service layer: members, production, revenue, finance)
cd backend && mvn test

# Frontend
cd frontend && npm test
```

## 🔒 Security notes

- Passwords are hashed with BCrypt, API access is stateless with JWT.
- The default `JWT_SECRET` and database password in `docker-compose.yml` are for development. In production, set your own values through environment variables and do not expose the database port.

## 👤 Author

**Afrah Mouhsine**, Full Stack Developer
📧 afrahmohssine@gmail.com · 🐙 [GitHub](https://github.com/Afrah-Mouhsine)


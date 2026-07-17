# Airbnb Spa Booking - Plan d'Architecture

## Architecture Globale

```
┌─────────────────────────────────────────────────────┐
│                    Nginx (Reverse Proxy)              │
│  Frontend: / → Angular SPA                           │
│  API: /api/* → Spring Boot Backend                   │
│  HTTPS en production                                 │
├──────────────────────┬──────────────────────────────┤
│     Frontend         │        Backend               │
│     (Angular 19)     │     (Spring Boot 3.4)         │
│     Port 80/443      │     Port 8080                 │
├──────────────────────┼──────────────────────────────┤
│  Components:         │  Controllers → Services →     │
│  - Public pages      │  Repositories → Entities      │
│  - User area         │                              │
│  - Admin dashboard   │  Security:                    │
│  Guards / Interceptor│  - HTTP Basic Auth            │
│  AuthService (mem)   │  - SecurityFilterChain        │
├──────────────────────┼──────────────────────────────┤
│    PostgreSQL (Port 5432)                            │
│    Base de données principale                        │
│    Migrations Flyway                                 │
└─────────────────────────────────────────────────────┘
```

## Structure des Dossiers

```
airbnb-spa-booking/
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/airbnbspa/
│   │   ├── AirBnbSpaApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   ├── DataInitializer.java
│   │   │   └── LoginAttemptService.java
│   │   ├── controller/
│   │   │   ├── PublicController.java
│   │   │   ├── UserController.java
│   │   │   └── AdminController.java
│   │   ├── dto/
│   │   │   ├── BookingRequest.java / BookingResponse.java
│   │   │   ├── PriceRuleDTO.java
│   │   │   ├── EquipmentDTO.java
│   │   │   ├── UserDTO.java
│   │   │   ├── AvailabilityBlockDTO.java
│   │   │   └── DashboardDTO.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Booking.java
│   │   │   ├── PriceRule.java
│   │   │   ├── Equipment.java
│   │   │   └── AvailabilityBlock.java
│   │   ├── enums/
│   │   │   ├── Role.java
│   │   │   ├── BookingStatus.java
│   │   │   ├── BookingType.java
│   │   │   └── DayType.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── BookingConflictException.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── BookingRepository.java
│   │   │   ├── PriceRuleRepository.java
│   │   │   ├── EquipmentRepository.java
│   │   │   └── AvailabilityBlockRepository.java
│   │   └── service/
│   │       ├── BookingService.java
│   │       ├── PriceCalculationService.java
│   │       ├── UserService.java
│   │       ├── EquipmentService.java
│   │       └── AvailabilityService.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── db/migration/
│   │       ├── V1__create_users.sql
│   │       ├── V2__create_bookings.sql
│   │       ├── V3__create_price_rules.sql
│   │       ├── V4__create_equipment.sql
│   │       ├── V5__create_availability_blocks.sql
│   │       └── V6__seed_data.sql
│   └── src/test/java/
│       ├── controller/
│       ├── service/
│       └── security/
├── frontend/
│   ├── package.json
│   ├── angular.json
│   ├── tsconfig.json
│   ├── src/
│   │   ├── index.html
│   │   ├── main.ts
│   │   ├── app/
│   │   │   ├── app.routes.ts
│   │   │   ├── app.config.ts
│   │   │   ├── app.component.ts
│   │   │   ├── models/
│   │   │   ├── services/
│   │   │   ├── guards/
│   │   │   ├── interceptors/
│   │   │   ├── pages/public/
│   │   │   │   ├── home/
│   │   │   │   ├── property/
│   │   │   │   ├── spa/
│   │   │   │   ├── gallery/
│   │   │   │   ├── prices/
│   │   │   │   ├── availability/
│   │   │   │   └── booking-form/
│   │   │   ├── pages/user/
│   │   │   │   ├── login/
│   │   │   │   ├── profile/
│   │   │   │   └── my-bookings/
│   │   │   └── pages/admin/
│   │   │       ├── dashboard/
│   │   │       ├── bookings/
│   │   │       ├── calendar/
│   │   │       ├── prices/
│   │   │       ├── equipment/
│   │   │       └── users/
│   │   ├── assets/
│   │   └── styles/
│   └── Dockerfile
├── docker-compose.yml
├── nginx/
│   └── default.conf
├── .env.example
├── README.md
└── api-tests.http
```

## Modèle de Données

```sql
users: id, username, password_hash, first_name, last_name, email, role, enabled, created_at
bookings: id, user_id (FK), first_name, last_name, email, phone, start_date, end_date, number_of_guests, booking_type, status, total_price, message, created_at, updated_at
price_rules: id, name, booking_type, day_type, price, active
equipment: id, name, description, icon, active
availability_blocks: id, start_date, end_date, reason, created_at
```

## Endpoints REST

### Public
- GET /api/public/property → info logement
- GET /api/public/equipment → équipements
- GET /api/public/prices → tarifs
- GET /api/public/availability → disponibilités
- POST /api/public/booking-requests → demande réservation

### User (/api/user)
- GET /api/user/profile → profil
- PUT /api/user/profile → modifier profil
- GET /api/user/bookings → mes réservations
- GET /api/user/bookings/{id} → détail réservation
- PUT /api/user/bookings/{id} → modifier réservation (si PENDING)
- DELETE /api/user/bookings/{id} → annuler réservation (si PENDING)

### Admin (/api/admin)
- GET /api/admin/dashboard → tableau de bord
- GET /api/admin/bookings → toutes les réservations
- GET /api/admin/bookings/{id} → détail
- PUT /api/admin/bookings/{id}/status → accepter/refuser
- GET /api/admin/calendar → calendrier admin
- POST /api/admin/availability-blocks → bloquer dates
- DELETE /api/admin/availability-blocks/{id} → débloquer dates
- PUT /api/admin/prices/{id} → modifier tarif
- GET /api/admin/equipment → équipements
- POST /api/admin/equipment → ajouter équipement
- PUT /api/admin/equipment/{id} → modifier équipement
- DELETE /api/admin/equipment/{id} → supprimer équipement
- GET /api/admin/users → utilisateurs
- POST /api/admin/users → créer utilisateur
- PUT /api/admin/users/{id} → modifier utilisateur

## Sécurité
- HTTP Basic Authentication uniquement
- SecurityFilterChain avec 3 zones (public/user/admin)
- BCrypt pour les mots de passe
- Credentials en mémoire Angular uniquement
- LoginAttemptService (5 tentatives max)
- CORS configuré pour Angular (localhost:4200)
- Headers de sécurité Nginx

## Étapes d'Implémentation

1. Backend: pom.xml, application.yml, entités, enums
2. Backend: repositories, DTOs
3. Backend: services métier
4. Backend: sécurité, controllers, exception handler
5. Backend: migrations Flyway, seed data
6. Backend: tests unitaires et d'intégration
7. Frontend: setup Angular, models, services
8. Frontend: pages publiques (home, property, spa, gallery)
9. Frontend: prices, availability, booking-form
10. Frontend: login, user area
11. Frontend: admin dashboard et CRUD
12. Docker: docker-compose, Dockerfiles, nginx
13. Documentation: README, .env.example, api-tests.http
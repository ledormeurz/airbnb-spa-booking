# 🏡 Airbnb Spa Booking

**Application de réservation pour un spa / gîte de charme** — Backend Spring Boot + Frontend Angular + Base de données PostgreSQL.

Cette application permet aux visiteurs de consulter les disponibilités, d'effectuer des demandes de réservation, et aux administrateurs de gérer les réservations, les prix, les équipements et les utilisateurs via un tableau de bord sécurisé.

---

## 📸 Captures d'écran

> *À venir — Ajoutez ici des captures d'écran de l'application.*

| Page | Aperçu |
|------|--------|
| Page d'accueil | ![](screenshots/home.png) |
| Calendrier des disponibilités | ![](screenshots/calendar.png) |
| Formulaire de réservation | ![](screenshots/booking.png) |
| Tableau de bord admin | ![](screenshots/admin.png) |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Navigateur                            │
│                 (Application Angular SPA)                     │
└────────────────────────┬────────────────────────────────────┘
                         │  HTTP / HTTPS
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     Nginx (Reverse Proxy)                     │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  /api/* → backend:8080  │  /* → index.html (SPA)       │ │
│  └─────────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Backend Spring Boot (Java 21)                    │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Contrôleurs REST  │  Services  │  Repositories JPA     │ │
│  └─────────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────┘
                         │  JDBC
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL 16                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Tables : property, bookings, users, prices, etc.      │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

**Flux réseau (Docker Compose) :**

```
                    app-network
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │ Frontend │◄──►│ Backend  │◄──►│ Postgres │
    │  :80     │    │  :8080   │    │  :5432   │
    └──────────┘    └──────────┘    └──────────┘
         ▲
         │ port 80
    ┌────┴────┐
    │  Host   │
    └─────────┘
```

---

## 📋 Prérequis

| Technologie | Version minimale | Installé avec |
|-------------|-----------------|---------------|
| **Java** | 21 (Eclipse Temurin) | [SDKMAN](https://sdkman.io/) ou package système |
| **Node.js** | 22 LTS | [nvm](https://github.com/nvm-sh/nvm) |
| **Docker** | 24+ | [Docker Desktop](https://www.docker.com/products/docker-desktop/) ou Docker Engine |
| **Docker Compose** | V2 (inclus avec Docker) | — |
| **Maven** | 3.9+ | SDKMAN, package système, ou wrapper (`./mvnw`) |

---

## 🚀 Démarrage rapide (Docker Compose)

La méthode la plus simple pour lancer l'application complète.

```bash
# 1. Cloner le projet
git clone <url-du-depot> airbnb-spa-booking
cd airbnb-spa-booking

# 2. Copier et configurer les variables d'environnement
cp .env.example .env
# Modifiez le mot de passe PostgreSQL dans .env

# 3. Lancer tous les services
docker compose up --build -d

# 4. Vérifier que tout est opérationnel
docker compose ps
docker compose logs -f

# 5. Ouvrir l'application
# Frontend : http://localhost
# Backend API : http://localhost:8080/api/public/property
```

**Arrêter les services :**

```bash
docker compose down
# Pour supprimer aussi les volumes (données DB perdues) :
docker compose down -v
```

---

## 🛠️ Démarrage manuel (sans Docker)

### Backend (Spring Boot)

```bash
cd backend

# Option A — Avec Maven (recommandé)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Option B — Construire puis exécuter
./mvnw clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=dev
```

Le backend sera accessible sur `http://localhost:8080`.

> **Note :** Vous avez besoin d'une instance PostgreSQL accessible. Configurez `SPRING_DATASOURCE_URL` dans `application-dev.yml` ou via des variables d'environnement.

### Frontend (Angular)

```bash
cd frontend

# Installer les dépendances
npm install

# Lancer le serveur de développement
# (avec proxy vers http://localhost:8080)
npm start

# Pour un build de production
npm run build --prod
```

Le frontend sera accessible sur `http://localhost:4200` (le proxy redirige les appels `/api/*` vers le backend).

---

## 📁 Structure du projet

```
airbnb-spa-booking/
├── backend/                        # Application Spring Boot (Java 21)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/airbnbspa/
│   │   │   │   ├── config/         # Configuration (sécurité, CORS, etc.)
│   │   │   │   ├── controller/     # Contrôleurs REST
│   │   │   │   ├── model/          # Entités JPA
│   │   │   │   ├── repository/     # Accès aux données (Spring Data JPA)
│   │   │   │   ├── service/        # Logique métier
│   │   │   │   └── dto/            # Objets de transfert de données
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       └── application-prod.yml
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                       # Application Angular
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/         # Composants Angular
│   │   │   ├── services/           # Services HTTP
│   │   │   ├── models/             # Interfaces TypeScript
│   │   │   └── guards/             # Gardes d'authentification
│   │   └── environments/
│   ├── angular.json
│   ├── package.json
│   └── Dockerfile
│
├── nginx/
│   └── default.conf                # Configuration Nginx
│
├── docker-compose.yml              # Orchestration Docker
├── .env.example                    # Exemple de variables d'environnement
├── .gitignore
├── api-tests.http                  # Tests d'API (VS Code REST Client)
└── README.md                       # Ce fichier
```

---

## 📡 Documentation de l'API

### Endpoints publics (aucune authentification requise)

| Méthode | Chemin | Description |
|---------|--------|-------------|
| `GET` | `/api/public/property` | Informations sur le gîte (description, adresse, règles) |
| `GET` | `/api/public/equipment` | Liste des équipements disponibles |
| `GET` | `/api/public/prices` | Grille tarifaire (prix par saison / type de séjour) |
| `GET` | `/api/public/availability` | Disponibilités sur une période (paramètres : `startDate`, `endDate`) |
| `POST` | `/api/public/booking-requests` | Créer une demande de réservation (anonyme) |
| `POST` | `/api/public/register` | Créer un compte utilisateur (email + mot de passe) |
| `POST` | `/api/public/login` | Connexion JWT (email + mot de passe → access token) |

### Endpoints utilisateur (JWT Bearer requis)

| Méthode | Chemin | Description |
|---------|--------|-------------|
| `GET` | `/api/user/profile` | Profil de l'utilisateur connecté |
| `GET` | `/api/user/bookings` | Liste des réservations de l'utilisateur connecté |
| `GET` | `/api/user/bookings/{id}` | Détail d'une réservation (propriétaire uniquement) |
| `GET` | `/api/user/bookings/{id}/calendar.ics` | Export iCalendar (`.ics`) de la réservation |

### Endpoints administrateur (JWT Bearer + rôle ADMIN requis)

| Méthode | Chemin | Description |
|---------|--------|-------------|
| `GET` | `/api/admin/dashboard` | Tableau de bord (statistiques, réservations récentes) |
| `GET` | `/api/admin/bookings` | Toutes les réservations |
| `GET` | `/api/admin/users` | Liste des utilisateurs |
| `GET` | `/api/admin/prices` | Gestion des prix |
| `GET` | `/api/admin/equipment` | Gestion des équipements |
| `GET` | `/api/admin/availability-blocks` | Blocs d'indisponibilité |
| `POST` | `/api/admin/availability-blocks/import-ics` | Import d'un fichier `.ics` (multipart `file`, `source`) |

### Export iCalendar (`.ics`)

Un utilisateur connecté peut télécharger un fichier **iCalendar** pour une de ses réservations, puis l'ouvrir dans Outlook, Google Calendar, Apple Calendar, etc.

- **API** : `GET /api/user/bookings/{id}/calendar.ics` (JWT Bearer requis, propriétaire uniquement)
- **Frontend** : bouton **Ajouter à mon agenda** sur la page détail d'une réservation (`/user/bookings/{id}`)
- **Contenu** : événement journée entière (`UID` stable `booking-{id}@airbnbspa.com`, titre, dates, statut)

Exemple :

```bash
curl -H "Authorization: Bearer <accessToken>" \
  -o reservation-1.ics \
  http://localhost:8080/api/user/bookings/1/calendar.ics
```

### Import iCalendar → blocs d'indisponibilité

Un **admin** peut importer un export calendrier (ex. Airbnb) pour bloquer les dates déjà réservées ailleurs. Les événements deviennent des `AvailabilityBlock` (pas des `Booking` clients).

- **API** : `POST /api/admin/availability-blocks/import-ics` (multipart : `file` + `source` optionnel, défaut `ICAL`)
- **Upsert** par `UID` iCal (`external_uid`) : réimporter le même fichier met à jour sans doublons
- **Dates** : `DTEND` iCal `VALUE=DATE` est exclusif → converti en `endDate` inclusive côté app

Exemple :

```bash
curl -H "Authorization: Bearer <accessToken>" \
  -F "file=@listing.ics" \
  -F "source=AIRBNB" \
  http://localhost:8080/api/admin/availability-blocks/import-ics
```

### Sync URL iCal (Airbnb / Booking)

Un admin peut enregistrer l’URL iCal du calendrier hosting. Le serveur télécharge périodiquement le `.ics` et met à jour les `AvailabilityBlock` (même logique d’upsert que l’import fichier).

- **API** :
  - `GET/POST /api/admin/calendar-feeds`
  - `POST /api/admin/calendar-feeds/{id}/sync` (sync manuelle)
  - `DELETE /api/admin/calendar-feeds/{id}`
- **UI** : carte **Sync URL calendrier** sur `/admin/calendar`
- **Job** : toutes les ~10 min (`app.calendar-sync.fixed-delay-ms`), désactivable via `CALENDAR_SYNC_ENABLED=false`
- **Sécurité** : seules les URL `airbnb.com` / `airbnb.fr` / `booking.com` sont acceptées

Exemple :

```bash
curl -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Airbnb","url":"https://www.airbnb.com/calendar/ical/....ics","source":"AIRBNB"}' \
  http://localhost:8080/api/admin/calendar-feeds

curl -X POST -H "Authorization: Bearer <accessToken>" \
  http://localhost:8080/api/admin/calendar-feeds/1/sync
```

---

## 🔐 Authentification

Le frontend utilise un **JWT access token** simple. L'identifiant de connexion est l'**email**.
HTTP Basic Auth reste disponible en fallback (tests / curl).

### Comment ça fonctionne (JWT)

1. `POST /api/public/login` avec `{ email, password }`.
2. Le serveur vérifie les identifiants et renvoie un `accessToken` (claims : `sub`, `uid`, `role`, `exp`).
3. Les requêtes protégées envoient `Authorization: Bearer <accessToken>`.
4. Le filtre JWT valide la signature et l'expiration, puis place l'utilisateur dans le contexte Spring Security.
5. Token invalide / expiré → `401 Unauthorized`.

Durée par défaut : **1 heure** (`JWT_EXPIRATION_MS`).

### Créer un compte (inscription)

N'importe qui peut créer un compte `USER` en libre-service via l'endpoint public `POST /api/public/register` :

```bash
curl -X POST http://localhost:8080/api/public/register \
  -H "Content-Type: application/json" \
  -d '{"email":"jean@example.com","password":"secret123","firstName":"Jean","lastName":"Dupont"}'
```

Le mot de passe doit contenir au moins 6 caractères. L'email doit être unique.

### Exemple de connexion JWT (admin)

```bash
# 1) Obtenir un access token
curl -s -X POST http://localhost:8080/api/public/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@airbnbspa.com","password":"admin123"}'

# 2) Appeler un endpoint protégé
curl -H "Authorization: Bearer <accessToken>" http://localhost:8080/api/admin/dashboard
```

Fallback Basic Auth (toujours supporté) :

```bash
curl -u admin@airbnbspa.com:admin123 http://localhost:8080/api/admin/dashboard
```

### Rôles

| Rôle | Compte démo (email / mot de passe) | Accès |
|------|------------------------------------|-------|
| `ADMIN` | `admin@airbnbspa.com` / `admin123` | Tous les endpoints admin + utilisateur + publics |
| `USER` | `john@example.com` / `password123` | Endpoints utilisateur + publics uniquement |

> **ℹ️ Note :** L'email de l'administrateur est `admin@airbnbspa.com`. Le mot de passe correspond à la variable `ADMIN_PASSWORD` (par défaut `admin123`) définie au premier démarrage.

> **⚠️ ATTENTION :** Changez impérativement les mots de passe par défaut avant toute mise en production.

---

## ⚠️ Limitations de HTTP Basic Auth

**Ce projet utilise HTTP Basic Authentication pour des raisons pédagogiques.** Bien que fonctionnel, ce mécanisme présente plusieurs limitations importantes pour un environnement de production :

### 1. Absence de chiffrement

HTTP Basic Auth se contente d'encoder les identifiants en **Base64**, ce n'est PAS du chiffrement. Le Base64 est un simple encodage réversible instantanément. Un attaquant qui intercepte le trafic réseau peut décoder les identifiants en quelques secondes.

```
email:password  ──encodage Base64──►  (chaîne encodée)  ──décodage──►  email:password
```

### 2. HTTPS est OBLIGATOIRE en production

Sans HTTPS, les identifiants circulent en clair sur le réseau. L'utilisation de HTTPS (TLS) chiffre l'intégralité de la communication, protégeant les identifiants Basic Auth contre l'interception.

### 3. Identifiants envoyés à chaque requête

Contrairement aux systèmes basés sur des tokens (JWT, OAuth2), les identifiants complets sont envoyés dans chaque en-tête HTTP. Cela :
- Augmente la surface d'attaque (les identifiants sont visibles dans les logs, les proxys, etc.)
- Oblige le serveur à vérifier le mot de passe à chaque appel (impact performance)
- Empêche de révoquer sélectivement une session sans changer le mot de passe

### 4. Pas d'expiration de session intégrée

Une fois que l'utilisateur a fourni ses identifiants, le navigateur les renvoie automatiquement jusqu'à la fermeture de l'onglet (ou indéfiniment s'ils sont enregistrés). Il n'y a pas de mécanisme natif pour :
- Forcer une reconnexion périodique
- Déconnecter un utilisateur à distance
- Gérer des durées de session différentes par rôle

### 5. Vulnérabilité aux attaques CSRF

Étant donné que les navigateurs envoient automatiquement les identifiants Basic Auth avec chaque requête vers un domaine, l'application est potentiellement vulnérable aux attaques CSRF (Cross-Site Request Forgery) si des mesures de protection ne sont pas mises en place.

### Améliorations recommandées pour la production

| Mécanisme | Description | Complexité |
|-----------|-------------|------------|
| **OAuth2** | Protocole standard d'autorisation déléguée | Moyenne |
| **JWT (JSON Web Tokens)** | Tokens signés contenant les claims utilisateur | Faible |
| **OpenID Connect** | Couche d'identité au-dessus d'OAuth2 | Moyenne |
| **Keycloak** | Serveur d'authentification complet (open source) | Élevée |
| **Spring Session** | Gestion de sessions serveur avec expiration | Faible |
| **Refresh Tokens** | Tokens courts + token de rafraîchissement longue durée | Moyenne |

### Pourquoi ce projet utilise Basic Auth

1. **Simplicité** — Le mécanisme est trivial à comprendre et à implémenter côté frontend comme backend.
2. **Pédagogie** — Ce projet est conçu pour apprendre les bases de la sécurisation d'une API REST.
3. **Prototypage rapide** — Pour une phase de développement ou un MVP, Basic Auth évite la complexité d'un système OAuth2 complet.
4. **Compatibilité** — Fonctionne avec tous les clients HTTP sans bibliothèque supplémentaire.

> **En production, remplacez impérativement Basic Auth par OAuth2 + JWT avec HTTPS.**

---

## 🔧 Variables d'environnement

| Variable | Description | Valeur par défaut | Requise |
|----------|-------------|-------------------|---------|
| `POSTGRES_DB` | Nom de la base de données | `airbnb_spa` | Oui |
| `POSTGRES_USER` | Utilisateur PostgreSQL | `airbnb` | Oui |
| `POSTGRES_PASSWORD` | Mot de passe PostgreSQL | — | **Oui** |
| `SPRING_DATASOURCE_URL` | URL JDBC de connexion | `jdbc:postgresql://postgres:5432/airbnb_spa` | Oui |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur Spring DataSource | `airbnb` | Oui |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe Spring DataSource | — | **Oui** |
| `SPRING_PROFILES_ACTIVE` | Profil Spring actif | `dev` | Non |
| `ADMIN_USERNAME` | Identifiant admin par défaut | `admin` | Non |
| `ADMIN_PASSWORD` | Mot de passe admin par défaut | `admin123` | Non |
| `JWT_SECRET` | Secret de signature JWT (HS256, ≥ 32 car.) | valeur démo | **Oui en prod** |
| `JWT_EXPIRATION_MS` | Durée de vie du access token (ms) | `3600000` (1h) | Non |
| `SERVER_PORT` | Port du backend (interne) | `8080` | Non |

---

## 👤 Comptes de démonstration

| Rôle | Email (identifiant de connexion) | Mot de passe |
|------|----------------------------------|-------------|
| **Administrateur** | `admin@airbnbspa.com` | `admin123` |
| **Utilisateur** | `john@example.com` | `password123` |
| **Utilisateur** | `jane@example.com` | `password456` |

> Ces comptes sont créés automatiquement au premier démarrage de l'application via un seed dans la base de données. La connexion se fait avec l'**email** et le mot de passe. Changez les mots de passe dès que possible en production.

---

## 🧪 Tests

### Tests manuels avec VS Code REST Client

Le fichier `api-tests.http` contient tous les appels API nécessaires pour tester l'application :

1. Ouvrez `api-tests.http` dans VS Code.
2. Installez l'extension [REST Client](https://marketplace.visualstudio.com/items?itemName=humao.rest-client).
3. Lancez l'application (Docker Compose ou manuellement).
4. Cliquez sur **"Send Request"** au-dessus de chaque requête.

Les tests couvrent :
- ✅ Endpoints publics (propriété, équipements, prix, disponibilités, réservation)
- ✅ Endpoints utilisateur (profil, réservations)
- ✅ Endpoints administrateur (dashboard, réservations, utilisateurs, prix, équipements)
- ✅ Cas de sécurité (401 sans auth, 403 user→admin)

### Tests automatisés (backend)

```bash
cd backend
./mvnw test
```

---

## 🚢 Déploiement

### Production avec Docker Compose

```bash
# 1. Préparer les variables d'environnement
cp .env.example .env
# Éditez .env avec des valeurs sécurisées

# 2. Construire et démarrer
export SPRING_PROFILES_ACTIVE=prod
docker compose up --build -d

# 3. Configurer HTTPS avec un reverse proxy
# Ajoutez un fichier docker-compose.override.yml avec un service
# certbot / Let's Encrypt, ou placez Nginx derrière un autre proxy.
```

### Avec un orchestrateur (Kubernetes, Docker Swarm)

Adaptez le `docker-compose.yml` pour votre orchestrateur. Les conteneurs sont conçus pour être stateless (sauf PostgreSQL) et supportent le scaling horizontal du backend.

### Sauvegarde de la base de données

```bash
# Sauvegarder
docker exec airbnb-spa-db pg_dump -U airbnb airbnb_spa > backup_$(date +%Y%m%d).sql

# Restaurer
cat backup.sql | docker exec -i airbnb-spa-db psql -U airbnb airbnb_spa
```

---

## 📄 Licence

Ce projet est fourni à titre éducatif. Vous êtes libre de l'utiliser, le modifier et le distribuer selon vos besoins.

---

*Projet réalisé avec ❤️ — Spring Boot + Angular + PostgreSQL*
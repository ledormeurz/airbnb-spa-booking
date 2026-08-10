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
    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │ Frontend │◄──►│ Backend  │◄──►│ Postgres │◄──►│ pgAdmin  │
    │  :80     │    │  :8080   │    │  :5432   │    │  :5050   │
    └──────────┘    └──────────┘    └──────────┘    └──────────┘
         ▲                                               ▲
         │ port 80                                       │ port 5050
    ┌────┴───────────────────────────────────────────────┴────┐
    │                         Host                             │
    └──────────────────────────────────────────────────────────┘
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
# pgAdmin : http://localhost:5050  (email/mot de passe : voir .env)
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
│   └── default.conf.template       # Nginx (templated : BACKEND_HOST)
│
├── pgadmin/                        # pgAdmin (Docker local + Fly.io)
│   ├── Dockerfile
│   ├── entrypoint-wrapper.sh
│   └── fly.toml
│
├── docker-compose.yml              # Orchestration Docker (postgres, backend, frontend, pgadmin)
├── fly.frontend.toml               # Config Fly.io du frontend
├── .env.example                    # Exemple de variables d'environnement
├── .gitignore
├── api-tests.http                  # Tests d'API (VS Code REST Client)
└── README.md                       # Ce fichier
```

> Le backend a aussi `backend/fly.toml` pour le déploiement Fly.io.

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

### Endpoints utilisateur (authentification Basic requise)

| Méthode | Chemin | Description |
|---------|--------|-------------|
| `GET` | `/api/user/profile` | Profil de l'utilisateur connecté |
| `GET` | `/api/user/bookings` | Liste des réservations de l'utilisateur connecté |

### Endpoints administrateur (authentification Basic + rôle ADMIN requis)

| Méthode | Chemin | Description |
|---------|--------|-------------|
| `GET` | `/api/admin/dashboard` | Tableau de bord (statistiques, réservations récentes) |
| `GET` | `/api/admin/bookings` | Toutes les réservations |
| `GET` | `/api/admin/users` | Liste des utilisateurs |
| `GET` | `/api/admin/prices` | Gestion des prix |
| `GET` | `/api/admin/equipment` | Gestion des équipements |
| `GET` | `/api/admin/availability-blocks` | Blocs d'indisponibilité |

---

## 🔐 Authentification

Ce projet utilise **HTTP Basic Authentication**.

### Comment ça fonctionne

1. Le client envoie ses identifiants (`username:password`) encodés en **Base64** dans l'en-tête HTTP `Authorization`.
2. Le serveur décode les identifiants et vérifie leur validité.
3. Si valides, la requête est traitée avec le rôle de l'utilisateur (`USER` ou `ADMIN`).
4. Si invalides, le serveur répond avec un statut `401 Unauthorized`.

### Exemple

```bash
# Encoder les identifiants
echo -n "admin:admin123" | base64
# Résultat : YWRtaW46YWRtaW4xMjM=

# Envoyer la requête
curl -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" http://localhost:8080/api/admin/dashboard
```

Ou plus simplement :

```bash
curl -u admin:admin123 http://localhost:8080/api/admin/dashboard
```

### Rôles

| Rôle | Compte démo | Accès |
|------|------------|-------|
| `ADMIN` | `admin` / `admin123` | Tous les endpoints admin + utilisateur + publics |
| `USER` | `user1` / `password123` | Endpoints utilisateur + publics uniquement |

> **⚠️ ATTENTION :** Changez impérativement les mots de passe par défaut avant toute mise en production.

---

## ⚠️ Limitations de HTTP Basic Auth

**Ce projet utilise HTTP Basic Authentication pour des raisons pédagogiques.** Bien que fonctionnel, ce mécanisme présente plusieurs limitations importantes pour un environnement de production :

### 1. Absence de chiffrement

HTTP Basic Auth se contente d'encoder les identifiants en **Base64**, ce n'est PAS du chiffrement. Le Base64 est un simple encodage réversible instantanément. Un attaquant qui intercepte le trafic réseau peut décoder les identifiants en quelques secondes.

```
username:password  ──encodage Base64──►  YWRtaW46YWRtaW4xMjM=  ──décodage──►  username:password
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
| `SERVER_PORT` | Port du backend (interne) | `8080` | Non |
| `PGADMIN_DEFAULT_EMAIL` | Email de connexion pgAdmin | `admin@airbnb-spa.local` | Non |
| `PGADMIN_DEFAULT_PASSWORD` | Mot de passe pgAdmin | `admin` | Non |
| `PGADMIN_PORT` | Port hôte pgAdmin (local) | `5050` | Non |

---

## 👤 Comptes de démonstration

| Rôle | Identifiant | Mot de passe |
|------|-------------|-------------|
| **Administrateur** | `admin` | `admin123` |
| **Utilisateur** | `user1` | `password123` |

> Ces comptes sont créés automatiquement au premier démarrage de l'application via un seed dans la base de données. Changez les mots de passe dès que possible en production.

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

### Déploiement sur Fly.io (test — 1 instance, région France `cdg`)

Architecture déployée :

```
Internet → airbnb-spa-frontend.fly.dev (Nginx + Angular)
              └─ /api/* → airbnb-spa-backend.internal:8080
                              └─ jdbc → airbnb-spa-db.internal:5432

Optionnel : airbnb-spa-pgadmin.fly.dev → airbnb-spa-db.internal:5432
```

| App | Config | Rôle |
|-----|--------|------|
| `airbnb-spa-db` | Fly Postgres | Base PostgreSQL (privé) |
| `airbnb-spa-backend` | `backend/fly.toml` | API Spring Boot |
| `airbnb-spa-frontend` | `fly.frontend.toml` | SPA + reverse proxy `/api` |
| `airbnb-spa-pgadmin` | `pgadmin/fly.toml` | UI admin Postgres (optionnel) |

#### Prérequis

- Compte [Fly.io](https://fly.io) + CLI [`flyctl`](https://fly.io/docs/hands-on/install-flyctl/)
- `fly auth login`

```bash
fly version
fly orgs list
```

> **Important :** toujours utiliser `--ha=false` pour rester sur **1 machine** (sinon Fly en crée 2).  
> Postgres a besoin d’**au moins 1024 Mo de RAM** (256 Mo provoque des crashs / `connection attempt failed`).

#### 1. Créer PostgreSQL (Paris)

```bash
fly postgres create --name airbnb-spa-db --region cdg \
  --initial-cluster-size 1 --vm-size shared-cpu-1x \
  --volume-size 1 --vm-memory 1024
```

Sauvegardez le mot de passe affiché (une seule fois).

```bash
fly postgres connect -a airbnb-spa-db
```

Puis dans `psql` :

```sql
CREATE DATABASE airbnb_spa;
\q
```

#### 2. Backend

```bash
fly apps create airbnb-spa-backend --org personal

fly secrets set \
  SPRING_DATASOURCE_URL="jdbc:postgresql://airbnb-spa-db.internal:5432/airbnb_spa" \
  SPRING_DATASOURCE_USERNAME="postgres" \
  SPRING_DATASOURCE_PASSWORD="YOUR_DB_PASSWORD" \
  SPRING_PROFILES_ACTIVE="prod" \
  ADMIN_USERNAME="admin" \
  ADMIN_PASSWORD="YOUR_STRONG_ADMIN_PASSWORD" \
  -a airbnb-spa-backend

cd backend
fly deploy --ha=false
cd ..
```

Vérifications :

```bash
fly status -a airbnb-spa-backend
fly logs -a airbnb-spa-backend
curl https://airbnb-spa-backend.fly.dev/actuator/health
```

#### 3. Frontend

Déployer **depuis la racine du repo** (le Dockerfile copie `frontend/` + `nginx/`) :

```bash
fly apps create airbnb-spa-frontend --org personal
fly deploy -c fly.frontend.toml --ha=false
```

Vérifications :

```bash
fly status -a airbnb-spa-frontend
curl -I https://airbnb-spa-frontend.fly.dev/
curl -I https://airbnb-spa-frontend.fly.dev/api/public/property
```

Nginx proxyfie `/api` vers `BACKEND_HOST` (`airbnb-spa-backend.internal` sur Fly, `backend` en local).

#### 4. pgAdmin (optionnel)

```bash
fly apps create airbnb-spa-pgadmin --org personal
fly volumes create pgadmin_data --region cdg --size 1 -a airbnb-spa-pgadmin

fly secrets set \
  PGADMIN_DEFAULT_EMAIL="you@example.com" \
  PGADMIN_DEFAULT_PASSWORD="YOUR_STRONG_PGADMIN_PASSWORD" \
  POSTGRES_HOST="airbnb-spa-db.internal" \
  POSTGRES_DB="airbnb_spa" \
  POSTGRES_USER="postgres" \
  -a airbnb-spa-pgadmin

cd pgadmin
fly deploy --ha=false
cd ..
```

Ouvrir https://airbnb-spa-pgadmin.fly.dev — le serveur Postgres est pré-enregistré ; utiliser le mot de passe DB à la connexion.

> Les secrets `PGADMIN_DEFAULT_*` ne s’appliquent qu’au **premier** démarrage (volume vide).

#### URLs

| Service | URL |
|---------|-----|
| Application | https://airbnb-spa-frontend.fly.dev |
| API | https://airbnb-spa-backend.fly.dev |
| Health | https://airbnb-spa-backend.fly.dev/actuator/health |
| pgAdmin | https://airbnb-spa-pgadmin.fly.dev |

#### Commandes utiles (après déploiement)

```bash
# Redeploy après changements de code
cd backend && fly deploy --ha=false && cd ..
fly deploy -c fly.frontend.toml --ha=false
cd pgadmin && fly deploy --ha=false && cd ..

# Forcer 1 seule machine
fly scale count 1 -a airbnb-spa-backend
fly scale count 1 -a airbnb-spa-frontend
fly scale count 1 -a airbnb-spa-pgadmin

# Logs / SSH
fly logs -a airbnb-spa-backend
fly logs -a airbnb-spa-frontend
fly ssh console -a airbnb-spa-backend

# Postgres — statut / RAM (min. 1024 Mo recommandé)
fly status -a airbnb-spa-db
fly scale show -a airbnb-spa-db
fly machine update <MACHINE_ID> -a airbnb-spa-db --vm-memory 1024 -y

# Mettre pgAdmin en pause pour économiser
fly scale count 0 -a airbnb-spa-pgadmin
fly scale count 1 -a airbnb-spa-pgadmin

# Lister les apps
fly apps list
```

#### Secrets Fly à connaître

| Secret / env | App | Description |
|--------------|-----|-------------|
| `SPRING_DATASOURCE_URL` | backend | `jdbc:postgresql://airbnb-spa-db.internal:5432/airbnb_spa` |
| `SPRING_DATASOURCE_USERNAME` | backend | Souvent `postgres` (valeur affichée à la création) |
| `SPRING_DATASOURCE_PASSWORD` | backend | Mot de passe Postgres |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | backend | Compte admin applicatif |
| `BACKEND_HOST` | frontend | Défini dans `fly.frontend.toml` → `airbnb-spa-backend.internal` |
| `POSTGRES_HOST` | pgAdmin | `airbnb-spa-db.internal` |
| `PGADMIN_DEFAULT_EMAIL` / `PGADMIN_DEFAULT_PASSWORD` | pgAdmin | Login UI |

#### Points d’attention

1. **HTTPS** est fourni par Fly — nécessaire avec HTTP Basic Auth.
2. Changez les mots de passe admin / pgAdmin avant un usage réel.
3. Backend et Postgres doivent être dans la **même org** Fly (réseau `.internal`).
4. Si vous renommez les apps, mettez à jour `BACKEND_HOST`, `POSTGRES_HOST` et les `app = "..."` dans les `fly.toml`.
5. `--org personal` : adaptez avec le nom renvoyé par `fly orgs list`.

### Sauvegarde de la base de données

**Docker Compose (local) :**

```bash
# Sauvegarder
docker exec airbnb-spa-db pg_dump -U airbnb airbnb_spa > backup_$(date +%Y%m%d).sql

# Restaurer
cat backup.sql | docker exec -i airbnb-spa-db psql -U airbnb airbnb_spa
```

**Fly.io :**

```bash
# Sauvegarder (depuis une machine ayant accès au réseau privé, ou via fly postgres connect)
fly postgres connect -a airbnb-spa-db -d airbnb_spa
# Puis dans psql : \copy / commandes dump selon besoin

# Alternative : ouvrir un tunnel puis pg_dump en local
fly proxy 5432 -a airbnb-spa-db
# dans un autre terminal :
# pg_dump -h localhost -U postgres -d airbnb_spa > backup.sql
```

---

## 📄 Licence

Ce projet est fourni à titre éducatif. Vous êtes libre de l'utiliser, le modifier et le distribuer selon vos besoins.

---

*Projet réalisé avec ❤️ — Spring Boot + Angular + PostgreSQL*
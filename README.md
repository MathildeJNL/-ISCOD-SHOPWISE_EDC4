# ShopWise API

ShopWise est une API REST pour gérer un catalogue de produits, enregistrer des ventes et proposer des recommandations basées sur les produits vendus ensemble.

## Fonctionnalités

- créer, consulter, modifier et supprimer des produits ;
- enregistrer une vente contenant plusieurs produits ;
- calculer automatiquement le total d’une vente ;
- consulter la liste et le détail des ventes ;
- authentifier les utilisateurs avec un token Bearer ;
- limiter certaines actions selon le rôle de l’utilisateur ;
- proposer des produits souvent achetés ensemble ;
- retourner des erreurs JSON au même format.

## Technologies

- Java 21 ;
- Spring Boot 4 ;
- Spring MVC ;
- Spring Data JPA ;
- Spring Security ;
- H2 pour le lancement local ;
- PostgreSQL avec Docker ;
- JUnit, Mockito et MockMvc pour les tests ;
- JaCoCo pour la couverture des tests.

## Lancer le projet en local

Prérequis : Java 21 et Maven 3.9 ou une version plus récente.

```bash
cd app_eval
mvn spring-boot:run
```

L’API est disponible sur `http://localhost:8080`.

La base H2 est créée en mémoire au démarrage et quatre produits de démonstration sont ajoutés automatiquement.

### Comptes de test

| Utilisateur | Mot de passe | Rôle |
|---|---|---|
| `admin` | `admin123` | Administrateur |
| `merchant` | `merchant123` | Commerçant |

Ces mots de passe peuvent être remplacés avec les variables d’environnement suivantes :

```text
SHOPWISE_ADMIN_PASSWORD
SHOPWISE_MERCHANT_PASSWORD
```

## Utiliser l’API

### 1. Se connecter

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"merchant","password":"merchant123"}'
```

La réponse contient un token :

```json
{
  "token": "TOKEN_RETOURNE_PAR_API",
  "tokenType": "Bearer",
  "expiresAt": "2026-07-13T08:00:00Z",
  "username": "merchant",
  "roles": ["ROLE_MERCHANT"]
}
```

Le token doit ensuite être envoyé dans l’en-tête `Authorization`.

### 2. Consulter les produits

```bash
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer TOKEN_RETOURNE_PAR_API"
```

### 3. Enregistrer une vente

```bash
curl -X POST http://localhost:8080/api/sales \
  -H "Authorization: Bearer TOKEN_RETOURNE_PAR_API" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]}'
```

Exemple de réponse :

```json
{
  "id": 1,
  "soldAt": "2026-07-12T20:00:00",
  "createdBy": "merchant",
  "total": 2899.97,
  "lines": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 2,
      "unitPrice": 999.99,
      "lineTotal": 1999.98
    },
    {
      "productId": 2,
      "productName": "Samsung Galaxy S24",
      "quantity": 1,
      "unitPrice": 899.99,
      "lineTotal": 899.99
    }
  ]
}
```

### 4. Obtenir des recommandations

```bash
curl "http://localhost:8080/api/recommendations?productId=1&limit=5" \
  -H "Authorization: Bearer TOKEN_RETOURNE_PAR_API"
```

## Endpoints

| Méthode | URL | Description | Rôle |
|---|---|---|---|
| POST | `/api/auth/login` | Se connecter | Public |
| GET | `/api/products` | Lister les produits | Commerçant ou administrateur |
| GET | `/api/products/{id}` | Consulter un produit | Commerçant ou administrateur |
| POST | `/api/products` | Créer un produit | Administrateur |
| PUT | `/api/products/{id}` | Modifier un produit | Administrateur |
| DELETE | `/api/products/{id}` | Supprimer un produit | Administrateur |
| POST | `/api/sales` | Enregistrer une vente | Commerçant ou administrateur |
| GET | `/api/sales` | Lister les ventes | Commerçant ou administrateur |
| GET | `/api/sales/{id}` | Consulter une vente | Commerçant ou administrateur |
| GET | `/api/recommendations` | Obtenir des recommandations | Commerçant ou administrateur |

## Organisation du code

L’application reste organisée en couches simples :

```mermaid
flowchart LR
    Client --> Controller
    Controller --> Service
    Service --> Repository
    Repository --> Database[(Base de données)]
    Security[Filtre de sécurité] --> Controller
```

- les contrôleurs reçoivent les requêtes HTTP ;
- les services contiennent les calculs et les règles de l’application ;
- les repositories communiquent avec la base de données ;
- les DTO définissent les données reçues et retournées par l’API ;
- les entités représentent les tables de la base de données.

Le code est séparé en dossiers `sale`, `recommendation` et `security` pour retrouver facilement chaque fonctionnalité.

## Base de données

```mermaid
erDiagram
    PRODUCTS ||--o{ SALE_LINES : concerne
    SALES ||--|{ SALE_LINES : contient

    PRODUCTS {
        bigint id PK
        varchar name
        varchar description
        decimal price
        timestamp created_at
        timestamp updated_at
    }

    SALES {
        bigint id PK
        decimal total
        timestamp sold_at
        varchar created_by
    }

    SALE_LINES {
        bigint id PK
        bigint sale_id FK
        bigint product_id FK
        varchar product_name
        int quantity
        decimal unit_price
        decimal line_total
    }
```

Les fichiers de base de données sont disponibles dans le dossier `bdd` :

- `bdd/script.sql` contient le script PostgreSQL ;
- `bdd/schema-base-de-donnees.pdf` contient le schéma au format PDF.

Le nom et le prix du produit sont copiés dans la ligne de vente. Ainsi, une ancienne vente garde ses informations même si le produit est modifié plus tard.

## Calcul des recommandations

Le programme regarde les ventes déjà enregistrées et repère les produits présents dans les mêmes paniers.

Pour chaque produit, il construit une liste des ventes dans lesquelles il apparaît. Il compare ensuite ces listes avec une similarité cosinus. Deux produits obtiennent un score élevé lorsqu’ils apparaissent souvent dans les mêmes ventes.

Si le produit n’a encore jamais été vendu, l’API propose les produits les plus vendus. Si aucune vente n’existe, elle retourne simplement des produits du catalogue.

La logique est placée derrière l’interface `RecommendationEngine`. Une autre méthode de recommandation pourra donc être ajoutée plus tard sans modifier le contrôleur.

## Sécurité

L’utilisateur se connecte avec son nom et son mot de passe. Après une connexion réussie, l’application crée un token aléatoire valable huit heures.

- une requête sans token valide retourne `401` ;
- un commerçant qui tente de modifier le catalogue reçoit `403` ;
- les mots de passe sont hachés avec BCrypt ;
- la console H2 est désactivée ;
- le prix et le total d’une vente sont toujours recalculés par le serveur.

Les tokens sont conservés en mémoire. Ils sont donc supprimés à chaque redémarrage de l’application, ce qui convient à cette version du projet.

## Format des erreurs

```json
{
  "code": "NOT_FOUND",
  "message": "Produit 20 introuvable",
  "timestamp": "2026-07-12T20:00:00Z",
  "path": "/api/products/20",
  "details": {}
}
```

L’API utilise notamment les codes HTTP `400`, `401`, `403`, `404`, `409` et `500`.

## Tests

Pour exécuter les tests et générer le rapport de couverture :

```bash
cd app_eval
mvn verify
```

Les tests couvrent :

- le catalogue existant ;
- le calcul du total d’une vente ;
- le regroupement des produits identiques ;
- les produits inexistants ;
- la connexion et les tokens ;
- les réponses `401`, `403`, `400` et `404` ;
- le calcul des recommandations ;
- le démarrage de l’application Spring.

Le rapport HTML est généré dans `couverture/backend/index.html`.

## Docker

Copier le fichier d’exemple puis choisir des mots de passe :

```bash
cp .env.example .env
docker compose up --build
```

Docker démarre PostgreSQL et l’API sur le port `8080`.

## Arborescence

```text
.
├── README.md
├── app_eval/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── bdd/
│   ├── schema-base-de-donnees.pdf
│   └── script.sql
├── couverture/backend/
├── docker-compose.yml
└── .env.example
```

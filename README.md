# 🍽️ Restaurant Review Platform

A web-based platform for discovering local restaurants, reading authentic user reviews, and sharing your own dining experiences. This intermediate-level project is built using **Spring Boot**, **Elasticsearch**, and a **Keycloak**.

---

## 🚀 Features

- 🔍 **Advanced Search** with Elasticsearch (fuzzy & geospatial search)
- 🗺️ Interactive maps for restaurant locations
- 📝 Submit and edit reviews with ratings and photos
- 📷 Upload & retrieve photos via REST API
- 👥 Keycloak-based authentication
- 📦 RESTful API for restaurants, reviews, and photos
- 🛠️ Dockerized setup for Elasticsearch and Keycloak

---

## 🧱 Tech Stack

| Layer       | Technology                                      |
|-------------|-------------------------------------------------|
| Backend     | Java 21, Spring Boot, Spring Security, OAuth2   |
| Search      | Elasticsearch                                   |
| Auth        | Keycloak (via Docker)                           |
| Deployment  | Docker, Docker Compose                          |

---

## ⚙️ Prerequisites

| Tool         | Version        | Notes                        |
|--------------|----------------|------------------------------|
| Java         | 21 or higher   | Backend development          |
| Docker       | Latest         | Required for Keycloak & ES   |
| Maven        | Bundled        | Dependency management        |
| IDE          | IntelliJ recommended | or any preferred IDE    |

---

## 🛠️ Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/ahmadnusa/restaurant-review-platform.git
   cd restaurant-review-platform

2. **Start Docker services**

   ```bash
   docker-compose up -d

3. **Run Backend**

   ```bash
   mvn spring-boot:run


5. **Access Services**
 
   - 📡 Backend API: http://localhost:8080  
   - 🔐 Keycloak Admin Console: http://localhost:9090  
   - 📊 Kibana (Elasticsearch): http://localhost:5601

---

## 🧩 Domain Model Overview

The Restaurant Review Platform consists of several key domain entities and their relationships:

### 👤 User
- Basic identification: `id`, `username`, `givenName`, `familyName`
- Can create restaurants (as an owner)
- Can write reviews

### 🍽️ Restaurant
- Core details: `name`, `cuisineType`, `contactInformation`
- Associated data:
  - `address` (with geolocation)
  - `operatingHours` (per day)
  - `photos`
  - `reviews`
  - `createdBy` (User)
- Geospatial location (`GeoPoint`) for mapping and search
- `averageRating` calculated from all reviews

### ✍️ Review
- `content` (text)
- `rating` (1–5 stars)
- `datePosted` and `lastEdited` timestamps
- `writtenBy` (User)
- `photos` (optional)
- Editable only within 48 hours of posting

### 🖼️ Photo
- `url` (location of uploaded image)
- `uploadDate` (timestamp)
- Can be associated with Restaurants or Reviews

### 🗺️ Address
- Fields: `streetNumber`, `streetName`, `unit`, `city`, `state`, `postalCode`, `country`
- `GeoPoint` for geospatial search and map view

### ⏰ OperatingHours
- One `TimeRange` per day (Monday to Sunday)
- Each `TimeRange` has `openTime` and `closeTime`

### 🔗 Relationships
- A `User` can create multiple `Restaurants`
- A `Restaurant` has one `owner` (User)
- A `User` can write multiple `Reviews`
- A `Review` belongs to one `Restaurant` and one `User`
- A `Restaurant` contains:
  - one `Address`
  - one `OperatingHours`
  - many `Reviews`
  - many `Photos`

---

## 📡 REST API Overview

The platform offers a RESTful API to manage restaurants, reviews, and photos.

### 📍 Restaurants

- `GET /restaurants`  
  Search restaurants using:
  - `q`: search query
  - `latitude`, `longitude`: geolocation filter
  - `radius`: distance in km
  - `cuisineType`: filter by cuisine
  - `minRating`: minimum rating (1–5)
  - `page`, `size`: pagination

- `POST /restaurants` *(auth required)*  
  Create a new restaurant

- `GET /restaurants/{restaurantId}`  
  Retrieve details of a single restaurant

- `PUT /restaurants/{restaurantId}` *(auth required)*  
  Update restaurant details

- `DELETE /restaurants/{restaurantId}` *(auth required)*  
  Delete a restaurant (owner only)

---

### 📝 Reviews

- `GET /restaurants/{restaurantId}/reviews`  
  Get paginated reviews for a restaurant  
  - Sort options: `date,desc`, `date,asc`, `rating,desc`, `rating,asc`  
  - Pagination: `page`, `size`

- `POST /restaurants/{restaurantId}/reviews` *(auth required)*  
  Submit a review  
  - One review per user per restaurant

- `PUT /restaurants/{restaurantId}/reviews/{reviewId}` *(auth required)*  
  Update a review  
  - Can only be edited within 48 hours

- `DELETE /restaurants/{restaurantId}/reviews/{reviewId}` *(auth required)*  
  Delete a review (author only)

---

### 📷 Photos

- `POST /photos` *(auth required)*  
  Upload a photo  
  - Content-Type: `multipart/form-data`  
  - Fields: `file`, optional `caption`

---

### 🔁 Pagination Metadata

All paginated responses include metadata:

```json
{
  "page": 1,
  "size": 20,
  "totalElements": 142,
  "totalPages": 8
}
```

🔐 All POST, PUT, and DELETE endpoints require JWT authentication via Keycloak. Be sure to log in to obtain a valid token.

---

> ⚠️ Jika mengalami error saat menjalankan `docker-compose`, pastikan:
> - Docker Desktop aktif
> - Port 9200, 5601, atau 9090 tidak sedang digunakan oleh aplikasi lain
> - Anda sudah menggunakan versi Docker dan Node.js yang direkomendasikan

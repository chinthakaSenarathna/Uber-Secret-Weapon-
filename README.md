# 🚀 Uber-like Real-Time Ride Matching System

This project demonstrates how ride-hailing platforms (like Uber) match riders with nearby drivers using **microservices**, **event-driven architecture**, and **real-time geospatial queries**.

---

## 🎥 Demo
👉 Add your demo video / GIF here

---

## 🏗️ Architecture Overview

### Services

| Service            | Port  | Responsibility |
|-------------------|-------|----------------|
| location-service  | 8082  | Tracks real-time driver locations using Redis Geospatial |
| ride-service      | 8083  | Manages ride lifecycle and publishes Kafka events |
| matching-service  | 8084  | Consumes ride events and assigns the best driver |

---

## ⚡ System Flow

Driver → Location Service → Redis (GEOADD)

Rider → Ride Service → Kafka (ride.requested)
↓
Matching Service (consumer)
↓
Location Service (Feign call)
↓
Driver Scoring Algorithm
↓
Kafka (ride.matched)
↓
Ride Service (update ride)

---

## 🛠️ Tech Stack

- **Spring Boot** - Microservices framework  
- **Apache Kafka** - Event-driven communication  
- **Redis** - Geospatial indexing (GEOADD, GEOSEARCH)  
- **OpenFeign** - Synchronous service-to-service communication  
- **MySQL** - Data persistence  
- **Docker Compose** - Infrastructure setup  

---

## 💡 Key Concepts

- Event-driven architecture using Kafka (Producer/Consumer pattern)  
- Asynchronous communication between services  
- Synchronous inter-service calls using OpenFeign  
- Redis Geospatial indexing for real-time proximity search  
- Driver ranking using weighted scoring (distance + rating)  
- Ride state machine: REQUESTED → MATCHING → ACCEPTED → STARTED → COMPLETED

---

## 🚀 How To Run

### Step 1: Start Infrastructure

``` bash
docker-compose up -d
```

- This starts:

Redis
MySQL
Zookeeper
Kafka

⏳ Wait ~30 seconds for Kafka to fully initialize.

### Step 2: Start Services

``` bash
cd location-service
mvn spring-boot:run

cd ride-service
mvn spring-boot:run

cd matching-service
mvn spring-boot:run
```

**🧪 End-to-End Testing**

1. Add Driver Locations -> http://localhost:8082/api/v1/locations/drivers/update

{
    "driverId": "driver:5",
    "latitude": 12.9630,
    "longitude": 77.6200
}

2. Find Nearby Drivers -> http://localhost:8082/api/v1/locations/drivers/nearby?latitude=12.9761&longitude=77.5946&radius=2.0

3. Request a Ride -> http://localhost:8083/api/v1/rides/request

{
    "riderId": "rider:2",
    "pickupLatitude": 12.9500,
    "pickupLongitude": 77.6045,
    "pickupAddress": "RS Road, Bangalore",
    "dropLatitude": 12.9351,
    "dropLongitude": 77.6247,
    "dropAddress": "ML mall, Bangalore"
}

4. Check Ride Status -> http://localhost:8083/api/v1/rides/{rideId}

5. Get List of Rides of Rider -> http://localhost:8083/api/v1/rides/rider/{riderId}

6. Start Ride -> http://localhost:8083/api/v1/rides/3105e6d2-5c9c-4181-af92-ac267677d4ab/start

7. Complete Ride -> http://localhost:8083/api/v1/rides/3105e6d2-5c9c-4181-af92-ac267677d4ab/complete

8. Cancel Ride -> http://localhost:8083/api/v1/rides/3105e6d2-5c9c-4181-af92-ac267677d4ab/cancel

9. Remove Driver -> http://localhost:8082/api/v1/locations/drivers/{driverId}

🔍 Redis Verification

``` bash
docker exec -it redis-geo redis-cli
```

View Drivers
``` bash
ZRANGE drivers:locations 0 -1
```

Get Driver Position
``` bash
GEOPOS drivers:locations "driver:1"
```

Distance Between Drivers
``` bash
GEODIST drivers:locations "driver:1" "driver:2" km
```

## Key Concepts Covered

- Redis Geospatial (GEOADD, GEORADIUS)
- Kafka event-driven architecture (Producer/Consumer)
- Ride state machine (REQUESTED → MATCHING → ACCEPTED → STARTED → COMPLETED)
- Driver scoring algorithm (distance + rating weighted score)
- Service-to-service REST communication (Matching → Location Service)
- Docker Compose for infrastructure setup

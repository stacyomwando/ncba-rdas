# RDAS — Reference Data Aggregation Service

Single REST/JSON API that acts as the source of truth for all country, currency, language and geographical reference data across NCBA Loop channels.

---

## Beginning

```bash
mvn clean package
mvn spring-boot:run
```

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Health check:** http://localhost:8080/actuator/health

---

## Architecture

```
Attached image architecture-diagram.png
```

**Key design decision:** The WSDL exposes `FullCountryInfoAllCountries` which returns every country with all fields in a single SOAP call. RDAS uses this exclusively for cache warming — meaning after startup, the SOAP service is called just 4 times per day regardless of traffic.

---

## API Reference

Base URL: `http://localhost:8080/api/v1`

### GET /countries
Search and filter countries.


`search` | Partial country name or ISO code |
`continent` | Filter by continent code (e.g. AF, EU) |
`currency` | Filter by currency ISO code (e.g. KES, USD) |
`language` | Filter by language name |
`page` | Page number, default 0 |
`size` | Page size, default 20 |
`sortBy` | name \| isoCode \| capital \| currency |
`sortDir` | asc \| desc |

**Eg:**
```
GET /api/v1/countries?continent=AF&page=0&size=10&sortBy=name
```

### GET /countries/{isoCode}
Full details for one country. Example: `GET /api/v1/countries/KE`

### GET /countries/currency/{currencyCode}
All countries sharing a currency. Example: `GET /api/v1/countries/currency/USD`

### GET /continents
All continents.

### GET /currencies
All currencies.

### GET /languages
All languages.

---

## Caching Strategy 

**How SOAP traffic is reduced:**
Without RDAS every channel made individual SOAP calls. With RDAS, all calls are served from Caffeine in-memory cache. SOAP is only called at startup (4 calls) and once per day at midnight — regardless of how many users hit the API.

**What is cached:** All countries (full detail), all continents, all currencies, all languages.

**TTL: 24 hours** — Reference data changes at most a few times a year.

**Refresh strategy:** `@Scheduled(cron = "0 0 0 * * *")` runs silently at midnight to refresh in the background. Users are never blocked. If the refresh fails, stale data is served and an error is logged.

---

## Resilience 

**When SOAP is down for 6 hours:**

- If cache is warm (normal after startup): **zero user impact**. SOAP outage is invisible.
- Circuit breaker (Resilience4j) opens after 3 failures — stops hammering the dead service
- Retry with backoff handles transient blips
- If cache expires while SOAP is down: returns last known data with degraded flag, or 503 if no data at all

**Monitoring:** `GET /actuator/health` shows circuit breaker state. `GET /actuator/caches` shows cache stats. Alert when circuit breaker opens or cache age exceeds 25 hours.

---

## Deploying on Kubernetes

### Step 1 — Build and push Docker image
```bash
mvn clean package -DskipTests
docker build -t your-registry/ncba/rdas:1.0.0 .
docker push your-registry/ncba/rdas:1.0.0
```
Update the image name in `k8s/deployment.yaml`.

### Step 2 — Deploy
```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
```

### Step 3 — Verify
```bash
kubectl get pods -n rdas
kubectl logs -n rdas deployment/rdas
kubectl port-forward -n rdas svc/rdas-service 8080:80
curl http://localhost:8080/actuator/health
```

### Rolling update
```bash
kubectl set image deployment/rdas rdas=your-registry/ncba/rdas:1.0.1 -n rdas
kubectl rollout status deployment/rdas -n rdas
```

### Scale
```bash
kubectl scale deployment rdas --replicas=4 -n rdas
```

---

## Kubernetes Troubleshooting

### Pod won't start
```bash
kubectl describe pod <pod-name> -n rdas   # check Events section
kubectl logs <pod-name> -n rdas           # application logs
kubectl logs <pod-name> -n rdas --previous # crash logs
```
Common causes: `OOMKilled` → increase memory limit | `ImagePullBackOff` → check image name/registry

### App returns 503
```bash
kubectl logs -n rdas deployment/rdas --follow   # watch for SOAP errors
curl http://localhost:8080/actuator/health       # check circuit breaker state
```

### Check cache status
```bash
curl http://localhost:8080/actuator/caches
curl http://localhost:8080/actuator/health
```

### Rollback
```bash
kubectl rollout undo deployment/rdas -n rdas
```

### Force cache refresh (without restarting)
```bash
kubectl rollout restart deployment/rdas -n rdas
```

### Useful commands
```bash
kubectl get all -n rdas
kubectl logs -n rdas -l app=rdas --follow
kubectl exec -it <pod-name> -n rdas -- /bin/sh
kubectl describe configmap rdas-config -n rdas
```

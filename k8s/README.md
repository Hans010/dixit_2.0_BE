# Local Kubernetes deployment

Deploys the Dixit backend plus a Loki + Grafana Alloy + Grafana logging stack,
all in the `dixit` namespace.

## 1. Build the app image

```bash
docker build -t dixit-backend:local .
```

## 2. Load the image into your local cluster

Local clusters don't pull from Docker Hub by default for images you've built
locally — you need to load the image directly.

**kind:**
```bash
kind load docker-image dixit-backend:local
```

**minikube:**
```bash
minikube image load dixit-backend:local
```

**k3s (with containerd):**
```bash
docker save dixit-backend:local | sudo k3s ctr images import -
```

## 3. Apply the manifests

```bash
kubectl apply -f k8s/
```

This creates, in order: the `dixit` namespace, the backend Deployment/Service,
Loki (config + Deployment/Service/PVC), Alloy (RBAC + config + Deployment),
and Grafana (datasource config + Deployment/Service/PVC).

Check everything came up:

```bash
kubectl get pods -n dixit
```

## 4. Access the app and Grafana

Nothing is exposed outside the cluster by default (all Services are
`ClusterIP`) — use port-forwarding:

```bash
# Dixit backend API
kubectl port-forward -n dixit svc/dixit-backend 8080:8080

# Grafana
kubectl port-forward -n dixit svc/grafana 3000:3000
```

Open http://localhost:3000 — Grafana is provisioned with Loki as the default
datasource already, and anonymous access is enabled as Admin (local/dev only,
see the note in `41-grafana.yaml` if you ever expose this beyond your machine).

Go to **Explore** (compass icon) → select the **Loki** datasource → query:

```logql
{namespace="dixit", app="dixit-backend"}
```

To filter to just structured game-action logs:

```logql
{namespace="dixit", app="dixit-backend"} | action != ""
```

Or filter by a specific action or game:

```logql
{namespace="dixit", app="dixit-backend"} | action="GAME_STARTED"
{namespace="dixit", app="dixit-backend"} | gameId="<uuid>"
```

(`action`, `gameId`, `phase`, `roundNumber` are attached as Loki structured
metadata by Alloy — see `31-alloy-config.yaml` — so they're filterable without
re-parsing JSON on every query.)

## 5. Generate some logs

With the backend port-forwarded, drive a game through the API
(`POST /player/add/{name}` a few times, then `/game/start`, `/game/clue`,
`/game/submit`, `/game/vote`, `/game/score`) and watch the log lines land in
Grafana within a few seconds.

## Updating the app

After code changes:

```bash
docker build -t dixit-backend:local .
kind load docker-image dixit-backend:local   # or minikube/k3s equivalent
kubectl rollout restart deployment/dixit-backend -n dixit
```

## Cleaning up

```bash
kubectl delete namespace dixit
```

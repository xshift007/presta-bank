Param(
    [string]$DOCKER_USER = "xsh1ft",   # Tu usuario de Docker Hub
    [string]$VERSION = "1.0",          # Versión/tag de la imagen
    [string]$NAMESPACE = "default"     # Namespace de Kubernetes
)

# Establecer que el script se detenga si hay errores
$ErrorActionPreference = "Stop"

# Definir los directorios de los microservicios
$services = @(
    "config-server",
    "eureka-server",
    "gateway-server",
    "m1-credit-simulation-service",
    "m2-user-registration-service",
    "m3-loan-application-service",
     "m4-loan-evaluation-service"
)

# Función para compilar un microservicio
function Build-Service($service) {
    Write-Host "Compilando $service con Maven..."
    Set-Location .\$service
    cmd /c "mvn clean install -DskipTests"
    Set-Location ..
}

# Compilar cada microservicio
foreach ($service in $services) {
    Build-Service $service
}

# Volver al directorio raíz
Set-Location .

Write-Host "Construyendo y subiendo imágenes Docker..."
foreach ($service in $services) {
    Write-Host "Construyendo imagen para $service..."
    docker build -t "${DOCKER_USER}/${service}:${VERSION}" .\$service
    Write-Host "Subiendo imagen ${DOCKER_USER}/${service}:${VERSION}..."
    docker push "${DOCKER_USER}/${service}:${VERSION}"
}

Write-Host "Aplicando ConfigMaps y Secrets..."
kubectl apply -f deployment/mysql-config-map.yaml -n $NAMESPACE
kubectl apply -f deployment/mysql-secrets.yaml -n $NAMESPACE

Write-Host "Desplegando Config Server..."
kubectl apply -f deployment/config-server-deployment-service.yaml -n $NAMESPACE

Write-Host "Desplegando Eureka Server..."
kubectl apply -f deployment/eureka-server-deployment-service.yaml -n $NAMESPACE

Write-Host "Desplegando Gateway Server..."
kubectl apply -f deployment/gateway-server-deployment-service.yaml -n $NAMESPACE

Write-Host "Desplegando Bases de Datos (M2 y M3)..."
kubectl apply -f deployment/m2-db-deployment-service.yaml -n $NAMESPACE
kubectl apply -f deployment/m3-db-deployment-service.yaml -n $NAMESPACE

Write-Host "Desplegando Microservicios..."
kubectl apply -f deployment/m1-credit-simulation-service-deployment-service.yaml -n $NAMESPACE
kubectl apply -f deployment/m2-user-registration-service-deployment-service.yaml -n $NAMESPACE
kubectl apply -f deployment/m3-loan-application-service-deployment-service.yaml -n $NAMESPACE
kubectl apply -f deployment/m4-loan-evaluation-service-deployment-service.yaml -n $NAMESPACE


Write-Host "Esperando que los pods estén disponibles..."

# Lista de deployments para esperar su disponibilidad
$deployments = @(
    "config-server-deployment",
    "eureka-server-deployment",
    "gateway-server-deployment",
    "m1-credit-simulation-service-deployment",
    "m2-user-registration-service-deployment",
    "m3-loan-application-service-deployment",
    "m4-loan-evaluation-service-deployment"
)

foreach ($deployment in $deployments) {
    try {
        Write-Host "Esperando que $deployment esté disponible..."
        kubectl wait --for=condition=Available deployment/$deployment -n $NAMESPACE --timeout=180s
        Write-Host "Deployment $deployment está disponible."
    } catch {
        Write-Host "Error: Timed out esperando que $deployment esté disponible."
    }
}

Write-Host "Estado final de Pods:"
kubectl get pods -o wide -n $NAMESPACE

Write-Host "Estado final de Services:"
kubectl get svc -n $NAMESPACE
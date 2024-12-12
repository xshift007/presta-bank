# deploy_kubernets.ps1

Param(
    [string]$DOCKER_USER = "xsh1ft",       # Tu usuario de Docker Hub
    [string]$VERSION = "1.0",              # Versión/tag de la imagen
    [string]$NAMESPACE = "default",        # Namespace de Kubernetes
    [string]$KUBE_CONTEXT = "minikube"      # Contexto de Kubernetes (ejemplo: "minikube")
)

# Establecer que el script se detenga si hay errores
$ErrorActionPreference = "Stop"

# Función para verificar si un comando existe
function Command-Exists($command) {
    return Get-Command $command -ErrorAction SilentlyContinue -ErrorVariable _
}

# Verificar si kubectl está instalado
if (-not (Command-Exists kubectl)) {
    Write-Error "kubectl no está instalado o no está en el PATH."
    exit 1
}

# Seleccionar el contexto de Kubernetes
Write-Host "Seleccionando el contexto de Kubernetes: $KUBE_CONTEXT"
kubectl config use-context $KUBE_CONTEXT

# Verificar si el contexto fue seleccionado correctamente
$currentContext = kubectl config current-context
if ($currentContext -ne $KUBE_CONTEXT) {
    Write-Error "No se pudo seleccionar el contexto de Kubernetes: $KUBE_CONTEXT"
    exit 1
} else {
    Write-Host "Contexto de Kubernetes seleccionado: $currentContext"
}

# Verificar si el namespace existe, si no, crearlo
$namespaceExists = kubectl get namespace $NAMESPACE -o jsonpath='{.metadata.name}' -n $NAMESPACE 2>$null
if (-not $namespaceExists) {
    Write-Host "Namespace '$NAMESPACE' no existe. Creándolo..."
    kubectl create namespace $NAMESPACE
} else {
    Write-Host "Namespace '$NAMESPACE' ya existe."
}

# Definir los microservicios
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
    if (Test-Path ".\mvnw.cmd") {
        # Usar Maven Wrapper en Windows
        .\mvnw.cmd clean install -DskipTests
    } elseif (Test-Path "./mvnw") {
        # Usar Maven Wrapper en Unix/Linux
        ./mvnw clean install -DskipTests
    } else {
        # Usar Maven instalado globalmente
        mvn clean install -DskipTests
    }
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

Write-Host "Esperando que los deployments estén disponibles..."

# Lista de deployments para esperar su disponibilidad
$deployments = @(
    "config-server-deployment",
    "eureka-server-deployment",
    "gateway-server-deployment",
    "m2-db-deployment",
    "m3-db-deployment",
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
        Write-Warning "Error: Timed out esperando que $deployment esté disponible."
    }
}

Write-Host "Estado final de Pods:"
kubectl get pods -o wide -n $NAMESPACE

Write-Host "Estado final de Services:"
kubectl get svc -n $NAMESPACE

Write-Host "Despliegue en Kubernetes completado."

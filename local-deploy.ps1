# local-deploy.ps1

<#
.SYNOPSIS
    Script para desplegar localmente todos los microservicios de la aplicación.

.DESCRIPTION
    Este script compila e inicia cada microservicio en su propio directorio,
    asegurando que las configuraciones locales estén correctamente aplicadas.
    También maneja la configuración de bases de datos locales si se especifica.

.PARAMETER DB_HOST_M2
    Host de la base de datos para el servicio M2 (User Registration).

.PARAMETER DB_PORT_M2
    Puerto de la base de datos para el servicio M2.

.PARAMETER DB_DBNAME_M2
    Nombre de la base de datos para el servicio M2.

.PARAMETER DB_USER_M2
    Usuario de la base de datos para el servicio M2.

.PARAMETER DB_PASSWORD_M2
    Contraseña de la base de datos para el servicio M2.

.PARAMETER DB_HOST_M3
    Host de la base de datos para el servicio M3 (Loan Application).

.PARAMETER DB_PORT_M3
    Puerto de la base de datos para el servicio M3.

.PARAMETER DB_DBNAME_M3
    Nombre de la base de datos para el servicio M3.

.PARAMETER DB_USER_M3
    Usuario de la base de datos para el servicio M3.

.PARAMETER DB_PASSWORD_M3
    Contraseña de la base de datos para el servicio M3.

.PARAMETER USE_LOCAL_DB
    Indica si se debe usar una base de datos local en lugar de Docker Compose.

.EXAMPLE
    .\local-deploy.ps1 -USE_LOCAL_DB "true"
#>

Param(
    [string]$DB_HOST_M2 = "localhost",
    [string]$DB_PORT_M2 = "3307",
    [string]$DB_DBNAME_M2 = "m2db",
    [string]$DB_USER_M2 = "user",
    [string]$DB_PASSWORD_M2 = "password",
    [string]$DB_HOST_M3 = "localhost",
    [string]$DB_PORT_M3 = "3308",
    [string]$DB_DBNAME_M3 = "m3db",
    [string]$DB_USER_M3 = "user",
    [string]$DB_PASSWORD_M3 = "password",
    [string]$USE_LOCAL_DB = "true" # Para usar las BDs locales en lugar de Docker Compose
)

# Obtener la ruta del directorio del script
$baseDir = $PSScriptRoot

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

# Define el orden de inicio
$startupOrder = @(
    "config-server",
    "eureka-server",
    "gateway-server",
    "m1-credit-simulation-service",
    "m2-user-registration-service",
    "m3-loan-application-service",
    "m4-loan-evaluation-service"
)

# Detener procesos Java específicos (evita detener todos los procesos Java)
Write-Host "Deteniendo procesos Java relacionados con la aplicación..."
Get-Process java -ErrorAction SilentlyContinue | Where-Object {
    $_.Path -like "*presta-bank*" -or $_.Path -like "*mvn*"
} | ForEach-Object {
    Write-Host "Deteniendo proceso $($_.ProcessName) (ID: $($_.Id))..."
    Stop-Process -Id $_.Id -Force
}
Write-Host "Procesos Java relacionados detenidos."

# Detener y remover contenedores Docker específicos
Write-Host "Deteniendo y eliminando contenedores Docker previos..."
docker stop mysql-m2 2>$null
docker stop mysql-m3 2>$null
docker rm mysql-m2 2>$null
docker rm mysql-m3 2>$null
Write-Host "Contenedores Docker detenidos y eliminados."

# Funcion para levantar las BD con Docker Compose
function Start-DBs(){
    Write-Host "Iniciando MySQL con Docker Compose..."
    $dockerComposePath = Join-Path $baseDir "docker-compose.yml"
    if(Test-Path $dockerComposePath){
        docker-compose -f $dockerComposePath up -d
        Write-Host "Docker Compose ejecutado."
    }
    else{
        Write-Error "docker-compose.yml no encontrado en $baseDir. Asegúrate de tenerlo en el mismo directorio del script."
    }
}

# Funcion para modificar los application.properties con la config de la DB local
function Configure-LocalDBs(){
    if ($USE_LOCAL_DB -eq "true"){
        # M2 Config
        $m2propsPath = Join-Path $baseDir "m2-user-registration-service\src\main\resources\application.properties"
        Write-Host "Configurando BD para M2 en $m2propsPath ..."
        (Get-Content $m2propsPath) | ForEach-Object {
            if ($_ -match "^spring.datasource.url=") {
                "spring.datasource.url=jdbc:mysql://${DB_HOST_M2}:${DB_PORT_M2}/${DB_DBNAME_M2}?useSSL=false&allowPublicKeyRetrieval=true"
            } elseif ($_ -match "^spring.datasource.username=") {
                "spring.datasource.username=${DB_USER_M2}"
            } elseif ($_ -match "^spring.datasource.password=") {
                "spring.datasource.password=${DB_PASSWORD_M2}"
            }
            else {
                $_
            }
        } | Set-Content $m2propsPath

        # M3 Config
        $m3propsPath = Join-Path $baseDir "m3-loan-application-service\src\main\resources\application.properties"
        Write-Host "Configurando BD para M3 en $m3propsPath ..."
        (Get-Content $m3propsPath) | ForEach-Object {
            if ($_ -match "^spring.datasource.url=") {
                "spring.datasource.url=jdbc:mysql://${DB_HOST_M3}:${DB_PORT_M3}/${DB_DBNAME_M3}?useSSL=false&allowPublicKeyRetrieval=true"
            } elseif ($_ -match "^spring.datasource.username=") {
                "spring.datasource.username=${DB_USER_M3}"
            } elseif ($_ -match "^spring.datasource.password=") {
                "spring.datasource.password=${DB_PASSWORD_M3}"
            }
            else {
                $_
            }
        } | Set-Content $m3propsPath

        Write-Host "Configuraciones de BD locales aplicadas."
    }
}

# Función para compilar un microservicio
function Build-Service($service) {
    Write-Host "Compilando $service con Maven..."
    $servicePath = Join-Path $baseDir $service
    if(Test-Path (Join-Path $servicePath "pom.xml")){
        Push-Location $servicePath
        cmd /c "mvn clean install -DskipTests"
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Error al compilar $service. Revisa los errores de Maven."
            Pop-Location
            exit 1
        }
        Pop-Location
    }
    else{
        Write-Host "No se encontró pom.xml en $servicePath. Saltando compilación."
    }
}

# Funcion para levantar un microservicio
function Start-ServiceInstance {
    param (
        [string]$serviceName,
        [string]$workingDirectory,
        [int]$sleepSeconds = 15  # Tiempo de espera antes de iniciar el siguiente servicio
    )

    Write-Host "Iniciando $serviceName..."
    $fullPath = Join-Path $baseDir $workingDirectory

    if (-Not (Test-Path $fullPath)) {
        Write-Error "Directorio no encontrado: $fullPath. Verifica la ruta."
        return
    }

    # Iniciar el servicio en una nueva ventana de PowerShell
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run" -WorkingDirectory $fullPath

    # Espera para asegurar que el servicio tenga tiempo de iniciar
    Start-Sleep -Seconds $sleepSeconds
}

# Compilar cada microservicio
foreach ($service in $services) {
    Build-Service $service
}

# Iniciar DB local o Docker Compose
if($USE_LOCAL_DB -eq "false"){
    Start-DBs
}
Configure-LocalDBs

# Iniciar microservicios en el orden definido
foreach ($service in $startupOrder) {
    switch ($service) {
        "config-server" {
            Start-ServiceInstance -serviceName "Config Server" -workingDirectory "config-server" -sleepSeconds 20
        }
        "eureka-server" {
            Start-ServiceInstance -serviceName "Eureka Server" -workingDirectory "eureka-server" -sleepSeconds 15
        }
        "gateway-server" {
            Start-ServiceInstance -serviceName "Gateway Server" -workingDirectory "gateway-server" -sleepSeconds 15
        }
        "m1-credit-simulation-service" {
            Start-ServiceInstance -serviceName "M1 Credit Simulation Service" -workingDirectory "m1-credit-simulation-service" -sleepSeconds 10
        }
        "m2-user-registration-service" {
            Start-ServiceInstance -serviceName "M2 User Registration Service" -workingDirectory "m2-user-registration-service" -sleepSeconds 10
        }
        "m3-loan-application-service" {
            Start-ServiceInstance -serviceName "M3 Loan Application Service" -workingDirectory "m3-loan-application-service" -sleepSeconds 10
        }
        "m4-loan-evaluation-service" {
            Start-ServiceInstance -serviceName "M4 Loan Evaluation Service" -workingDirectory "m4-loan-evaluation-service" -sleepSeconds 10
        }
        default {
            Write-Host "Servicio desconocido: $service. Saltando inicio."
        }
    }
}

Write-Host "Todos los servicios han sido iniciados."
Write-Host "Puedes verificar el estado de los servicios accediendo a los siguientes endpoints:"
Write-Host "Eureka Server: http://localhost:8761"
Write-Host "Gateway Server: http://localhost:8080"
Write-Host "Endpoints de servicios a través del Gateway:"
Write-Host " - Credit Simulation Service: http://localhost:8080/credit-simulation/status"
Write-Host " - User Registration Service: http://localhost:8080/user-registration/status"
Write-Host " - Loan Application Service: http://localhost:8080/loan-application/status"
Write-Host " - Loan Evaluation Service: http://localhost:8080/loan-evaluation/status"

# Parametros opcionales para la BD (en caso de querer usar una bd local)
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
    [string]$USE_LOCAL_DB = "true" # Para usar las BDs locales en lugar de las del Docker Compose
)

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

# Detener todos los procesos de spring-boot previamente ejecutados
Write-Host "Deteniendo procesos de Spring Boot previos..."
Get-Process | Where-Object {$_.ProcessName -like "java*"} | ForEach-Object {
    Write-Host "Deteniendo proceso $($_.ProcessName) (ID: $($_.Id))..."
    Stop-Process -Id $_.Id -Force
}

# Detener y remover contenedores Docker
Write-Host "Deteniendo y eliminando contenedores Docker previos..."
docker stop mysql-m2 2>$null
docker stop mysql-m3 2>$null
docker rm mysql-m2 2>$null
docker rm mysql-m3 2>$null

# Funcion para levantar las BD con Docker Compose
function Start-DBs(){
    Write-Host "Iniciando MySQL con Docker Compose..."
    if(Test-Path "docker-compose.yml"){
        docker-compose up -d
    }else{
        Write-Error "docker-compose.yml no encontrado. Asegúrate de tenerlo en el mismo directorio del script"
    }
}

# Funcion para modificar los application.properties con la config de la DB local
function Configure-LocalDBs(){
    if ($USE_LOCAL_DB -eq "true"){
        # M2 Config
        $m2propsPath = "m2-user-registration-service/src/main/resources/application.properties"
        Write-Host "Configurando BD para M2 en $($m2propsPath) ..."
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
        $m3propsPath = "m3-loan-application-service/src/main/resources/application.properties"
        Write-Host "Configurando BD para M3 en $($m3propsPath) ..."
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
    }
}

# Función para compilar un microservicio
function Build-Service($service) {
    Write-Host "Compilando $service con Maven..."
    Set-Location .\$service
    cmd /c "mvn clean install -DskipTests"
    Set-Location ..
}

# Funcion para levantar un microservicio
function Start-Service($service) {
    Write-Host "Iniciando $service ..."
    Set-Location .\$service
    cmd /c "mvn spring-boot:run"
    Set-Location ..
}

# Compilar cada microservicio
foreach ($service in $services) {
    Build-Service $service
}

# Volver al directorio raíz
Set-Location .

# Iniciar DB local o Docker Compose
if($USE_LOCAL_DB -eq "false"){
    Start-DBs
}
Configure-LocalDBs

# Iniciar microservicios
foreach ($service in $startupOrder) {
    Start-Service $service
}
Write-Host "Todos los servicios se han iniciado. Verifícalos en tu navegador."

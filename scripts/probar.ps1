param([string]$Configuracion = 'config/pruebas.properties', [string]$RutaJdk = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $RutaJdk) { $RutaJdk = 'C:\Program Files\Android\Android Studio\jbr' }
New-Item -ItemType Directory -Path target/test-classes -Force | Out-Null
$fuentes = Get-ChildItem src/test/java -Recurse -Filter *.java
$argumentos = @('-encoding','UTF-8','--release','17','-cp','target/classes',
    '-d','target/test-classes') + $fuentes.FullName
& "$RutaJdk\bin\javac.exe" @argumentos
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron compilar las pruebas.' }
& "$RutaJdk\bin\java.exe" -cp 'target/classes;target/test-classes;lib/mysql-connector-j-9.7.0.jar' lacasona.PruebasIntegracion $Configuracion
if ($LASTEXITCODE -ne 0) { throw 'Hay pruebas fallidas. Revisa el reporte.' }

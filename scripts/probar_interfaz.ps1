param([string]$Configuracion = 'config/pruebas.properties', [string]$RutaJdk = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $RutaJdk) { $RutaJdk = 'C:\Program Files\Android\Android Studio\jbr' }
if (-not (Test-Path target/test-classes/lacasona/PruebasInterfaz.class)) {
    throw 'Primero ejecuta scripts/probar.ps1 para compilar las pruebas.'
}
& "$RutaJdk\bin\java.exe" -cp 'target/classes;target/test-classes;lib/mysql-connector-j-9.7.0.jar' lacasona.PruebasInterfaz $Configuracion
if ($LASTEXITCODE -ne 0) { throw 'Revisa los resultados de las pruebas del formulario.' }

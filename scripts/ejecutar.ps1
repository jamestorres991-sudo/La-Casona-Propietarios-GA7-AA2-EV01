param([string]$RutaJdk = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $RutaJdk) { $RutaJdk = 'C:\Program Files\Android\Android Studio\jbr' }
$archivoJar = 'dist/propietarios.jar'
if (Test-Path target/propietarios.jar) { $archivoJar = 'target/propietarios.jar' }
if (-not (Test-Path $archivoJar)) { throw 'Primero ejecuta scripts/compilar.ps1.' }
& "$RutaJdk\bin\java.exe" -cp "$archivoJar;lib/mysql-connector-j-9.7.0.jar" lacasona.Aplicacion

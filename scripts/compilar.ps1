param([string]$RutaJdk = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $RutaJdk) {
    $RutaJdk = 'C:\Program Files\Android\Android Studio\jbr'
}
if (-not (Test-Path "$RutaJdk\bin\javac.exe")) {
    throw 'Indica la carpeta del JDK: .\scripts\compilar.ps1 -RutaJdk "C:\ruta\jdk"'
}
New-Item -ItemType Directory -Path target/classes -Force | Out-Null
$fuentes = Get-ChildItem src/main/java -Recurse -Filter *.java
$argumentos = @('-encoding', 'UTF-8', '--release', '17', '-d', 'target/classes')
$argumentos += $fuentes.FullName
& "$RutaJdk\bin\javac.exe" @argumentos
if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar el proyecto.' }
& "$RutaJdk\bin\jar.exe" "-J-Djava.io.tmpdir=$((Resolve-Path target).Path)" --create --file target/propietarios.jar --main-class lacasona.Aplicacion -C target/classes .
if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear el archivo JAR.' }
Write-Host 'Proyecto compilado. Ya puedes ejecutar el módulo.'


New-Item -ItemType Directory -Path dist -Force | Out-Null
Copy-Item -LiteralPath target/propietarios.jar -Destination dist/propietarios.jar -Force

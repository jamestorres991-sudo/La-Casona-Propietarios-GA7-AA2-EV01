param([string]$RutaJdk = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$raizInicio = Split-Path $PSScriptRoot -Parent
if (-not $RutaJdk) { $RutaJdk = 'C:\Program Files\Android\Android Studio\jbr' }
& (Join-Path $PSScriptRoot 'compilar.ps1') -RutaJdk $RutaJdk
$rutaPrueba = Join-Path $raizInicio ('target/inicio-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
New-Item -ItemType Directory -Path $rutaPrueba -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $raizInicio 'sql') -Destination $rutaPrueba -Recurse
$rutaClases = Join-Path $raizInicio 'target/pruebas_inicio'
New-Item -ItemType Directory -Path $rutaClases -Force | Out-Null
$rutaJar = Join-Path $raizInicio 'dist/propietarios.jar'
$rutaJdbc = Join-Path $raizInicio 'lib/mysql-connector-j-9.7.0.jar'
& "$RutaJdk\bin\javac.exe" -encoding UTF-8 --release 17 -cp "$rutaJar;$rutaJdbc" -d $rutaClases (Join-Path $raizInicio 'src/test/java/lacasona/PruebasInicioAutomatico.java')
if ($LASTEXITCODE -ne 0) { throw 'No se pudo preparar la prueba de inicio.' }
& "$RutaJdk\bin\java.exe" '-Djava.awt.headless=true' -cp "$rutaClases;$rutaJar;$rutaJdbc" lacasona.PruebasInicioAutomatico $rutaPrueba
if ($LASTEXITCODE -ne 0) { throw 'La prueba del inicio no fue aprobada.' }

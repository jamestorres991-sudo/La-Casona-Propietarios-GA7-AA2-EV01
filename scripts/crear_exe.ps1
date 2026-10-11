$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
& (Join-Path $PSScriptRoot 'compilar.ps1')
$compilador = Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
if (-not (Test-Path $compilador)) { throw 'Se necesita .NET Framework para crear el ejecutable.' }
& $compilador /nologo /target:winexe /r:System.Windows.Forms.dll "/out:$proyecto\La Casona.exe" "$proyecto\lanzador\LaCasona.cs"
if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear el ejecutable.' }
Write-Host 'Ya puedes abrir La Casona.exe con doble clic.'


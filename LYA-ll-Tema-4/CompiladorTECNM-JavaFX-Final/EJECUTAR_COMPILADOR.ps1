Set-Location $PSScriptRoot
$mavenLocal = 'C:\apache-maven-3.9.16\bin\mvn.cmd'

if (Test-Path $mavenLocal) {
    & $mavenLocal clean javafx:run
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn clean javafx:run
} else {
    Write-Host 'No se encontro Maven. Agregue Maven al PATH o revise C:\apache-maven-3.9.16\bin\mvn.cmd' -ForegroundColor Red
}

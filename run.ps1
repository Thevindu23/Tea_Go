param([int]$Port = 8080, [switch]$NoBuild)
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$runtimePath = Join-Path $projectRoot '.runtime'
New-Item -ItemType Directory -Force -Path $runtimePath | Out-Null
if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) {
    throw "Port $Port is in use. Stop the existing TeaGo instance with .\stop.ps1 or choose another -Port."
}
if (!$NoBuild) {
    $repositoryPath = Join-Path $projectRoot '.m2\repository'
    & mvn "-Dmaven.repo.local=$repositoryPath" -f (Join-Path $projectRoot 'teago\pom.xml') package -B -ntp
    if ($LASTEXITCODE -ne 0) { throw 'Build failed; TeaGo was not started.' }
}
$jarPath = Join-Path $projectRoot 'teago\target\teago-0.0.1-SNAPSHOT.jar'
if (!(Test-Path -LiteralPath $jarPath)) { throw 'Application jar is missing. Run without -NoBuild first.' }
if (!$env:TEAGO_DB_URL) { $env:TEAGO_DB_URL = 'jdbc:mysql://localhost:3306/teago_local?createDatabaseIfNotExist=true' }
$javaCommand = (Get-Command java -ErrorAction Stop).Source
$started = Start-Process -FilePath $javaCommand -ArgumentList @('-jar', ('"' + $jarPath + '"'), '--server.address=127.0.0.1', "--server.port=$Port") -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $runtimePath 'app.log') -RedirectStandardError (Join-Path $runtimePath 'app-error.log') -PassThru
for ($attempt = 0; $attempt -lt 40; $attempt++) {
    Start-Sleep -Milliseconds 500
    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($listener) {
        $listener.OwningProcess | Set-Content (Join-Path $runtimePath 'app.pid')
        Write-Output "TeaGo is running at http://localhost:$Port (PID $($listener.OwningProcess))."
        return
    }
}
throw "TeaGo did not become ready. See $runtimePath\app.log and app-error.log."

$ErrorActionPreference = 'Stop'
$pidPath = Join-Path $PSScriptRoot '.runtime\app.pid'
if (!(Test-Path -LiteralPath $pidPath)) { Write-Output 'No recorded TeaGo process.'; return }
$teagoProcessId = [int](Get-Content -LiteralPath $pidPath)
$teagoProcess = Get-CimInstance Win32_Process -Filter "ProcessId = $teagoProcessId"
if (!$teagoProcess) { Write-Output 'TeaGo is already stopped.'; return }
if ($teagoProcess.Name -notmatch '^java(w)?\.exe$' -or $teagoProcess.CommandLine -notlike '*teago-0.0.1-SNAPSHOT.jar*') {
    throw 'The recorded PID no longer belongs to TeaGo; it was not stopped.'
}
Stop-Process -Id $teagoProcessId
Remove-Item -LiteralPath $pidPath
Write-Output 'TeaGo stopped.'

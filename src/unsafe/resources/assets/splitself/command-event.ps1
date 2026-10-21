$OutputEncoding = [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
$browserNames = @{
    chrome = 'Google Chrome'; msedge = 'Microsoft Edge'; firefox = 'Mozilla Firefox'
    brave = 'Brave'; opera = 'Opera'; vivaldi = 'Vivaldi'; browser = 'Yandex Browser'
}
$running = @(Get-Process -ErrorAction SilentlyContinue | ForEach-Object {
    if ($browserNames.ContainsKey($_.ProcessName)) { $browserNames[$_.ProcessName] }
} | Sort-Object -Unique)
if ($running.Count -eq 0) { $running = @('Не обнаружен') }
Write-Host ('Браузер: ' + ($running -join ', '))
Write-Host ('Имя пользователя: ' + $env:SPLITSELF_COMMAND_USER)
try {
    $profiles = @(Get-NetConnectionProfile -ErrorAction Stop)
    $online = @($profiles | Where-Object {
        $_.IPv4Connectivity -eq 'Internet' -or $_.IPv6Connectivity -eq 'Internet'
    }).Count -gt 0
    if ($online) { Write-Host 'Интернет: подключён' }
    else { Write-Host 'Интернет: подключение не подтверждено Windows' }
} catch { Write-Host 'Интернет: статус недоступен' }
Write-Host 'Запущенная игра: Minecraft 1.20.1 (Split Self)'
Write-Host ''
if ((Get-UICulture).TwoLetterISOLanguageName -eq 'ru') {
    Write-Host 'Я помогу тебе вспомнить'
} else {
    Write-Host 'I will help you remember'
}

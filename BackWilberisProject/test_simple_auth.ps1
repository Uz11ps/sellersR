# Простой тест авторизации
Write-Host "=== Тест авторизации ==="

# Авторизация
$body = @{
    email = "vvlad1001@gmail.com"
    password = "123"
} | ConvertTo-Json

Write-Host "1. Авторизуемся..."
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body
$token = $response.user.token
Write-Host "Токен получен: $($token.Substring(0, 20))..."

# Заголовки
$headers = @{
    'Authorization' = "Bearer $token"
    'Content-Type' = 'application/json'
}

# Тест user-info (работает)
Write-Host "`n2. Тест user-info..."
try {
    $userInfo = Invoke-RestMethod -Uri "http://localhost:8080/api/analytics/user-info" -Method GET -Headers $headers
    Write-Host "✅ user-info работает: $($userInfo.data.email)"
} catch {
    Write-Host "❌ user-info ошибка: $($_.Exception.Message)"
}

# Тест analytics/data (не работает)
Write-Host "`n3. Тест analytics/data..."
try {
    $analyticsData = Invoke-RestMethod -Uri "http://localhost:8080/api/analytics/data" -Method GET -Headers $headers
    Write-Host "✅ analytics/data работает"
} catch {
    Write-Host "❌ analytics/data ошибка: $($_.Exception.Message)"
}

# Тест отдельных эндпоинтов
$endpoints = @(
    "http://localhost:8080/api/analytics/financial-report?days=7",
    "http://localhost:8080/api/analytics/stocks-report?days=7",
    "http://localhost:8080/api/analytics/orders-report?days=7"
)

Write-Host "`n4. Тест отдельных эндпоинтов..."
foreach ($endpoint in $endpoints) {
    $name = $endpoint.Split('/')[-1].Split('?')[0]
    try {
        $result = Invoke-RestMethod -Uri $endpoint -Method POST -Headers $headers
        Write-Host "✅ $name работает"
    } catch {
        Write-Host "❌ $name ошибка: $($_.Exception.Message)"
    }
}

Write-Host "`n=== Тест завершен ===" 
 
 
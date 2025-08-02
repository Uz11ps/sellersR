Write-Host "Compiling project..." -ForegroundColor Green
mvn clean compile
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "Compilation failed!" -ForegroundColor Red
}
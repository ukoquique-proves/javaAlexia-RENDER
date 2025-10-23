@echo off
REM Script para ejecutar la aplicación Alexia en Windows
REM Detiene cualquier instancia previa, elimina webhook de Telegram y lanza una nueva

echo.
echo ========================================
echo   Alexia - Asistente Automatizado
echo ========================================
echo.

echo [1/4] Buscando instancias previas...

REM Buscar procesos de Maven
tasklist /FI "IMAGENAME eq java.exe" /FI "WINDOWTITLE eq *spring-boot*" 2>NUL | find /I "java.exe" >NUL

if %ERRORLEVEL% EQU 0 (
    echo [!] Encontradas instancias en ejecucion
    echo [2/4] Deteniendo instancias previas...
    
    REM Detener procesos de Maven/Spring Boot
    for /f "tokens=2" %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FI "WINDOWTITLE eq *spring-boot*" ^| find /I "java.exe"') do (
        taskkill /PID %%a /F >NUL 2>&1
    )
    
    REM Esperar un momento
    timeout /t 3 /nobreak >NUL
    echo [OK] Instancias previas detenidas
) else (
    echo [OK] No hay instancias previas en ejecucion
)

echo.
echo [3/4] Eliminando webhook de Telegram (si existe)...

REM Cargar token de Telegram desde .env
set TELEGRAM_BOT_TOKEN=
for /f "tokens=1,2 delims==" %%a in ('type .env ^| findstr /v "^#" ^| findstr "TELEGRAM_BOT_TOKEN"') do (
    set TELEGRAM_BOT_TOKEN=%%b
)

REM Eliminar webhook de Telegram
if defined TELEGRAM_BOT_TOKEN (
    curl -s "https://api.telegram.org/bot%TELEGRAM_BOT_TOKEN%/deleteWebhook" >NUL 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo [OK] Webhook eliminado correctamente
    ) else (
        echo [!] No se pudo eliminar el webhook (puede que no exista^)
    )
) else (
    echo [!] TELEGRAM_BOT_TOKEN no encontrado en .env
)

echo.
echo [4/4] Iniciando aplicacion Alexia...
echo ========================================
echo.

REM Ejecutar la aplicación
call mvn spring-boot:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] La aplicacion no pudo iniciarse
    echo Verifica que Maven este instalado y configurado correctamente
    pause
    exit /b 1
)

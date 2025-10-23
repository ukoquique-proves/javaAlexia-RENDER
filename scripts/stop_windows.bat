@echo off
REM Script para detener completamente la aplicación Alexia en Windows
REM Detiene todos los procesos relacionados de forma segura

echo.
echo ========================================
echo   Deteniendo Alexia
echo ========================================
echo.

echo [1/3] Buscando procesos de Maven/Spring Boot...

REM Buscar procesos de Maven/Spring Boot
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I "java.exe" >NUL

if %ERRORLEVEL% EQU 0 (
    echo [!] Encontrados procesos Java en ejecucion
    echo [2/3] Deteniendo procesos Java...
    
    REM Detener todos los procesos Java (Maven/Spring Boot)
    for /f "tokens=2" %%a in ('tasklist /FI "IMAGENAME eq java.exe" ^| find /I "java.exe"') do (
        taskkill /PID %%a /F >NUL 2>&1
    )
    
    REM Esperar un momento
    timeout /t 2 /nobreak >NUL
    echo [OK] Procesos Java detenidos
) else (
    echo [OK] No hay procesos Java en ejecucion
)

echo.
echo [3/3] Verificacion final...

REM Verificar que no queden procesos
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I "java.exe" >NUL

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo [OK] Aplicacion Alexia detenida
    echo ========================================
    echo.
) else (
    echo.
    echo [!] ADVERTENCIA: Aun quedan procesos Java en ejecucion
    echo.
    echo Procesos activos:
    tasklist /FI "IMAGENAME eq java.exe"
    echo.
    echo Para forzar el cierre, ejecuta:
    echo   taskkill /F /IM java.exe
    echo.
)

pause

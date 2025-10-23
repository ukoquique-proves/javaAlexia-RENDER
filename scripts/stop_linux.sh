#!/bin/bash

# Script para detener completamente la aplicación Alexia
# Detiene todos los procesos relacionados de forma segura y limpia conexiones

echo "🛑 Deteniendo aplicación Alexia..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "🔍 Buscando procesos de Maven/Spring Boot..."
SPRING_PIDS=$(pgrep -f "spring-boot:run")

if [ -n "$SPRING_PIDS" ]; then
    echo "⚠️  Encontrados procesos Maven/Spring Boot (PIDs: $SPRING_PIDS)"
    echo "🛑 Deteniendo procesos Maven/Spring Boot (SIGTERM)..."
    pkill -15 -f "spring-boot:run"
    sleep 5
    
    # Verificar si se detuvieron
    REMAINING=$(pgrep -f "spring-boot:run")
    if [ -n "$REMAINING" ]; then
        echo "⚠️  Algunos procesos no respondieron, forzando cierre (SIGKILL)..."
        pkill -9 -f "spring-boot:run"
        sleep 2
    fi
    echo "✓ Procesos Maven/Spring Boot detenidos"
else
    echo "✓ No hay procesos Maven/Spring Boot en ejecución"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✓ Aplicación Alexia detenida."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"



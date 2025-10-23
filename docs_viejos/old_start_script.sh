#!/bin/bash

# Script para ejecutar la aplicación Alexia (Versión simplificada para sistemas sensibles)
# Detiene cualquier instancia previa y lanza una nueva

echo "🛑 Intentando detener cualquier instancia previa de la aplicación..."

# Envía una señal de terminación suave a cualquier proceso relacionado con la app.
# Esto es más seguro y menos propenso a causar cuelgues.
pkill -f "spring-boot:run" 2>/dev/null

# Una pausa muy corta para permitir que el sistema procese la señal.
sleep 2

echo "✓ Comprobación finalizada."

echo ""
echo "🚀 Iniciando aplicación Alexia..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "ℹ️  Cargando variables de entorno desde .env..."

# Cargar variables de entorno desde .env
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✓ Variables de entorno cargadas"
else
    echo "⚠️  Archivo .env no encontrado"
fi

echo ""
echo "ℹ️  El webhook de Telegram se eliminará automáticamente al iniciar el bot"
echo "   desde la interfaz web (Dashboard → Telegram → Iniciar Bot)"
echo ""

# Ejecutar la aplicación
mvn spring-boot:run -Dspring-boot.run.profiles=dev

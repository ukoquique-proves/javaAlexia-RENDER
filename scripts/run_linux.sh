#!/bin/bash

# Script para ejecutar la aplicación Alexia
# Detiene cualquier instancia previa y lanza una nueva
# 
# NOTA: El webhook de Telegram se elimina automáticamente en el código Java
#       al iniciar el bot, por lo que ya no es necesario hacerlo aquí.

echo "🔍 Buscando instancias previas de la aplicación..."

# Buscar y detener procesos de spring-boot:run
PIDS=$(pgrep -f "spring-boot:run")

if [ -n "$PIDS" ]; then
    echo "⚠️  Encontradas instancias en ejecución (PIDs: $PIDS)"
    echo "🛑 Deteniendo instancias previas..."
    pkill -f "spring-boot:run"
    sleep 3
    echo "✓ Instancias previas detenidas"
else
    echo "✓ No hay instancias previas en ejecución"
fi

# Verificar que no queden procesos
REMAINING=$(pgrep -f "spring-boot:run")
if [ -n "$REMAINING" ]; then
    echo "⚠️  Forzando cierre de procesos restantes..."
    pkill -9 -f "spring-boot:run"
    sleep 2
fi

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

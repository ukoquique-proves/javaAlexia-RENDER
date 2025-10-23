#!/bin/bash

# Script para ejecutar la migración de la base de datos para el Step 13 (RAG Search)
# Este script crea la tabla external_results_cache necesaria para el cache de resultados externos

set -e  # Salir inmediatamente si un comando falla

# Colores para la salida
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar que estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
  echo -e "${RED}Error: No se encontró pom.xml. Por favor ejecuta este script desde la raíz del proyecto.${NC}"
  exit 1
fi

# Verificar que el archivo de migración existe
if [ ! -f "database/step13_external_results_cache.sql" ]; then
  echo -e "${RED}Error: No se encontró el archivo de migración database/step13_external_results_cache.sql${NC}"
  exit 1
fi

# Cargar variables de entorno desde .env si existe
if [ -f ".env" ]; then
  echo -e "${YELLOW}Cargando variables de entorno desde .env${NC}"
  export $(cat .env | xargs)
fi

# Verificar que las variables de base de datos están configuradas
if [ -z "$SPRING_DATASOURCE_URL" ] || [ -z "$SPRING_DATASOURCE_USERNAME" ] || [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
  echo -e "${YELLOW}Advertencia: Las variables de entorno de base de datos no están configuradas.${NC}"
  echo -e "${YELLOW}Por favor configura SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME y SPRING_DATASOURCE_PASSWORD${NC}"
  echo -e "${YELLOW}o ingresa las credenciales manualmente a continuación:${NC}"
  
  read -p "URL de la base de datos (jdbc:postgresql://host:port/database): " DB_URL
  read -p "Usuario de la base de datos: " DB_USER
  read -s -p "Contraseña de la base de datos: " DB_PASS
  echo
  
  # Si no se ingresaron credenciales, usar las del .env o valores por defecto
  DB_URL=${DB_URL:-$SPRING_DATASOURCE_URL}
  DB_USER=${DB_USER:-$SPRING_DATASOURCE_USERNAME}
  DB_PASS=${DB_PASS:-$SPRING_DATASOURCE_PASSWORD}
  
  # Si aún no hay credenciales, salir
  if [ -z "$DB_URL" ] || [ -z "$DB_USER" ] || [ -z "$DB_PASS" ]; then
    echo -e "${RED}Error: No se pudieron obtener las credenciales de la base de datos.${NC}"
    exit 1
  fi
else
  DB_URL=$SPRING_DATASOURCE_URL
  DB_USER=$SPRING_DATASOURCE_USERNAME
  DB_PASS=$SPRING_DATASOURCE_PASSWORD
fi

# Extraer host, puerto, base de datos del URL
# Formato esperado: jdbc:postgresql://host:port/database
DB_HOST=$(echo $DB_URL | sed -E 's/jdbc:postgresql:\/\/([^:]+):([0-9]+)\/([^?]+).*/\1/')
DB_PORT=$(echo $DB_URL | sed -E 's/jdbc:postgresql:\/\/([^:]+):([0-9]+)\/([^?]+).*/\2/')
DB_NAME=$(echo $DB_URL | sed -E 's/jdbc:postgresql:\/\/([^:]+):([0-9]+)\/([^?]+).*/\3/')

# Si la extracción falló, intentar con formato simplificado
if [ -z "$DB_HOST" ] || [ -z "$DB_PORT" ] || [ -z "$DB_NAME" ]; then
  DB_HOST=$(echo $DB_URL | sed -E 's/jdbc:postgresql:\/\/([^\/]+)\/([^?]+).*/\1/')
  DB_PORT="5432"  # Puerto por defecto
  DB_NAME=$(echo $DB_URL | sed -E 's/jdbc:postgresql:\/\/([^\/]+)\/([^?]+).*/\2/')
fi

echo -e "${GREEN}Ejecutando migración para Step 13: RAG Search${NC}"
echo -e "${YELLOW}Base de datos: $DB_HOST:$DB_PORT/$DB_NAME${NC}"

# Ejecutar la migración usando psql
if command -v psql &> /dev/null; then
  echo -e "${YELLOW}Ejecutando migración con psql...${NC}"
  PGPASSWORD=$DB_PASS psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f database/step13_external_results_cache.sql
  
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Migración completada exitosamente${NC}"
    echo -e "${GREEN}La tabla external_results_cache ha sido creada.${NC}"
  else
    echo -e "${RED}❌ Error al ejecutar la migración${NC}"
    exit 1
  fi
else
  echo -e "${RED}Error: psql no está instalado o no está en el PATH${NC}"
  echo -e "${YELLOW}Por favor instala PostgreSQL client tools:${NC}"
  echo -e "${YELLOW}Ubuntu/Debian: sudo apt-get install postgresql-client${NC}"
  echo -e "${YELLOW}CentOS/RHEL: sudo yum install postgresql${NC}"
  echo -e "${YELLOW}macOS: brew install libpq${NC}"
  exit 1
fi

exit 0

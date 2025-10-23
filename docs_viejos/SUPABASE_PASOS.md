# Guía de Configuración de Supabase

Este documento contiene las instrucciones para crear todas las tablas necesarias en Supabase para el proyecto Alexia.

**Última actualización**: 2025-10-16  
**Estado del proyecto**: 6/10 pasos completados (60%)  
**Características actuales**: Bot con IA (Grok AI) + Dashboard profesional

## 🔧 Instrucciones Generales

1. **Accede a Supabase**: https://supabase.com
2. **Abre el SQL Editor** en tu proyecto
3. **Ejecuta los scripts** en el orden indicado

## 📦 Paso 2: Tabla de Prueba de Conexión

**Archivo**: `step2_connection_test.sql`

```sql
CREATE TABLE IF NOT EXISTS connection_test (
    id SERIAL PRIMARY KEY,
    message VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Propósito**: Verificar la conexión a la base de datos.

---

## 📱 Paso 3: Tabla de Mensajes de Telegram

** Archivo**: `step3_telegram_messages.sql`

```sql
CREATE TABLE IF NOT EXISTS telegram_messages (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    user_name VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    message_text TEXT,
    bot_response TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Propósito**: Almacenar todos los mensajes recibidos y respuestas del bot con IA.

---

## 🤖 Paso 5: Tabla de Comandos del Bot

** Archivo**: `step5_bot_commands.sql`

```sql
CREATE TABLE IF NOT EXISTS bot_commands (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    command VARCHAR(50) NOT NULL,
    user_name VARCHAR(255),
    first_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_bot_commands_chat_id ON bot_commands(chat_id);
CREATE INDEX IF NOT EXISTS idx_bot_commands_command ON bot_commands(command);
CREATE INDEX IF NOT EXISTS idx_bot_commands_created_at ON bot_commands(created_at DESC);
```

**Propósito**: Registrar todos los comandos ejecutados por los usuarios (/start, /help, /status).

---

## ✅ Verificación

Después de ejecutar cada script, verifica que las tablas se crearon correctamente:

```sql
-- Ver todas las tablas
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public';

-- Verificar estructura de una tabla específica
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'bot_commands';

-- Verificar datos
SELECT * FROM connection_test LIMIT 5;
SELECT * FROM telegram_messages LIMIT 5;
SELECT * FROM bot_commands LIMIT 5;
```

---

## 🎯 Comandos Disponibles del Bot

Una vez que todas las tablas estén creadas y la aplicación ejecutándose, podrás usar:

### `/start` - Mensaje de bienvenida
```
¡Bienvenido a Alexia! 🤖

Soy tu asistente automatizado con inteligencia artificial para encontrar Negocios, productos y servicios locales.

Usa /help para ver los comandos disponibles.
```

### `/help` - Lista de comandos
```
📋 Comandos disponibles:

/start - Mensaje de bienvenida
/help - Muestra esta ayuda
/status - Estado del bot

También puedes enviarme cualquier mensaje y te responderé con eco.
```

### `/status` - Estado del bot
```
✅ Bot activo y funcionando

📊 Estadísticas:
• Mensajes procesados: 42
• Comandos ejecutados: 15
• Última actualización: 16/10/2025 07:30:00
```

---

## 📊 Orden de Ejecución Recomendado

1. ✅ **Paso 2**: `step2_connection_test.sql` - Tabla de prueba
2. ✅ **Paso 3**: `step3_telegram_messages.sql` - Mensajes de Telegram
3. ✅ **Paso 5**: `step5_bot_commands.sql` - Comandos del bot

---

## 🔍 Consultas Útiles

### Ver estadísticas de comandos
```sql
SELECT command, COUNT(*) as total
FROM bot_commands
GROUP BY command
ORDER BY total DESC;
```

### Ver mensajes recientes con respuestas de IA
```sql
SELECT chat_id, user_name, message_text, bot_response, created_at
FROM telegram_messages
WHERE bot_response IS NOT NULL
ORDER BY created_at DESC
LIMIT 10;
```

### Ver comandos por usuario
```sql
SELECT user_name, command, COUNT(*) as total
FROM bot_commands
WHERE user_name IS NOT NULL
GROUP BY user_name, command
ORDER BY total DESC;
```

### Estadísticas de uso de IA
```sql
-- Mensajes que recibieron respuesta de IA vs eco
SELECT
    COUNT(CASE WHEN bot_response NOT LIKE 'Recibí tu mensaje:%' THEN 1 END) as respuestas_ia,
    COUNT(CASE WHEN bot_response LIKE 'Recibí tu mensaje:%' THEN 1 END) as respuestas_eco,
    COUNT(*) as total_mensajes
FROM telegram_messages;

---

## 📈 Progreso del Proyecto

| Paso | Estado | Fecha | Descripción |
|------|--------|-------|-------------|
| 1 | ✅ | 2025-10-14 | Proyecto Base y Dashboard Básico |
| 2 | ✅ | 2025-10-14 | Conexión a Supabase |
| UI | ✅ | 2025-10-14 | Dashboard Profesional Completo |
| 3 | ✅ | 2025-10-14 | Integración con Telegram |
| 4 | ✅ | 2025-10-16 | Dashboard con Logs de Telegram |
| 5 | ✅ | 2025-10-16 | Comandos Básicos del Bot |
| 6 | ✅ | 2025-10-16 | **Integración con Grok AI** ← **NUEVO** |
| 7 | ⏳ | Próximo | Dashboard de Conversaciones IA |
| 8 | ⏳ | Próximo | Integración con OpenAI (opcional) |
| 9 | ⏳ | Próximo | Búsqueda por categoría |
| 10 | ⏳ | Próximo | Dashboard con métricas |

**Progreso actual**: 6/10 pasos = **60% completado** 🎉

---

**Estado**: ✅ Completado hasta Paso 6  
**Última actualización**: 2025-10-16  
**Siguientes pasos**: Dashboard de conversaciones IA

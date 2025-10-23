# Paso 5: Comandos Básicos del Bot + Integración con Grok AI

## 📋 Tabla a Crear en Supabase

Antes de ejecutar la aplicación, debes crear la tabla `bot_commands` en Supabase.

### 🔧 Instrucciones

1. **Accede a Supabase**: https://supabase.com
2. **Abre el SQL Editor**
3. **Ejecuta el siguiente script**: `step5_bot_commands.sql`

### 📊 Estructura de la Tabla

```sql
CREATE TABLE IF NOT EXISTS bot_commands (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    command VARCHAR(50) NOT NULL,
    user_name VARCHAR(255),
    first_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);
```

### ✅ Verificación

Después de ejecutar el script, verifica que la tabla se creó correctamente:

```sql
SELECT * FROM bot_commands LIMIT 5;
```

### 🎯 Comandos Disponibles

Una vez que la tabla esté creada y la aplicación ejecutándose, podrás usar:

- `/start` - Mensaje de bienvenida
- `/help` - Lista de comandos disponibles
- `/status` - Estado del bot con estadísticas

### 🤖 Funcionalidad con IA

Además de los comandos básicos, el bot ahora responde con **inteligencia artificial** usando Grok AI:

- **Respuestas inteligentes** en español usando Grok AI (llama-3.1-8b-instant)
- **Historial de conversación** mantenido (hasta 20 mensajes)
- **Fallback automático** a respuesta eco si Grok falla
- **Tiempo de respuesta** optimizado (~1-3 segundos)

### 📝 Ejemplo de Uso

```
Usuario: /start
Bot: ¡Bienvenido a Alexia! 🤖

Usuario: Hola, ¿cómo estás?
Bot: Hola, ¿en qué puedo ayudarte? (respuesta de Grok AI)

Usuario: ¿Qué es la inteligencia artificial?
Bot: [Respuesta detallada e inteligente de Grok AI]

Usuario: /help
Bot: 📋 Comandos disponibles:
/start, /help, /status

Usuario: /status
Bot: ✅ Bot activo ✓ | Mensajes procesados: X | Comandos ejecutados: Y
```

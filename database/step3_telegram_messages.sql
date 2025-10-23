-- PASO 3: Tabla de mensajes de Telegram
-- Ejecutar este SQL en Supabase SQL Editor

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

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_telegram_messages_chat_id ON telegram_messages(chat_id);
CREATE INDEX IF NOT EXISTS idx_telegram_messages_created_at ON telegram_messages(created_at DESC);

-- Comentarios para documentación
COMMENT ON TABLE telegram_messages IS 'Almacena todos los mensajes recibidos del bot de Telegram';
COMMENT ON COLUMN telegram_messages.chat_id IS 'ID del chat de Telegram';
COMMENT ON COLUMN telegram_messages.user_name IS 'Nombre de usuario de Telegram (@username)';
COMMENT ON COLUMN telegram_messages.message_text IS 'Texto del mensaje enviado por el usuario';
COMMENT ON COLUMN telegram_messages.bot_response IS 'Respuesta generada por el bot';

-- Verificar que la tabla se creó correctamente
SELECT * FROM telegram_messages LIMIT 5;

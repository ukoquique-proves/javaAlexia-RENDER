-- Paso 5: Tabla de comandos del bot de Telegram
-- Esta tabla registra todos los comandos ejecutados por los usuarios

CREATE TABLE IF NOT EXISTS bot_commands (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    command VARCHAR(50) NOT NULL,
    user_name VARCHAR(255),
    first_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Índices para mejorar el rendimiento de consultas
CREATE INDEX IF NOT EXISTS idx_bot_commands_chat_id ON bot_commands(chat_id);
CREATE INDEX IF NOT EXISTS idx_bot_commands_command ON bot_commands(command);
CREATE INDEX IF NOT EXISTS idx_bot_commands_created_at ON bot_commands(created_at DESC);

-- Comentarios para documentación
COMMENT ON TABLE bot_commands IS 'Registro de comandos ejecutados en el bot de Telegram';
COMMENT ON COLUMN bot_commands.id IS 'Identificador único del comando';
COMMENT ON COLUMN bot_commands.chat_id IS 'ID del chat donde se ejecutó el comando';
COMMENT ON COLUMN bot_commands.command IS 'Comando ejecutado (ej: /start, /help, /status)';
COMMENT ON COLUMN bot_commands.user_name IS 'Username del usuario que ejecutó el comando';
COMMENT ON COLUMN bot_commands.first_name IS 'Nombre del usuario';
COMMENT ON COLUMN bot_commands.created_at IS 'Fecha y hora de ejecución del comando';

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS email VARCHAR(150);

UPDATE usuario
SET email = LOWER(TRIM(login)) || '@legacy.local'
WHERE email IS NULL OR TRIM(COALESCE(email, '')) = '';

ALTER TABLE usuario ALTER COLUMN email SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_email ON usuario (email);

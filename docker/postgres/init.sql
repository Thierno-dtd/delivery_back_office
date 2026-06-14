-- Créer la base si elle n'existe pas déjà
-- (PostgreSQL la crée via POSTGRES_DB, ce fichier sert pour les extensions)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- Timezone
SET timezone = 'Africa/Libreville';
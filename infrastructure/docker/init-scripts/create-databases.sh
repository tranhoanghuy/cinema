#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE identity_db;
    CREATE DATABASE movie_db;
    CREATE DATABASE cinema_db;
    CREATE DATABASE showtime_db;
    CREATE DATABASE booking_db;
    CREATE DATABASE payment_db;
    CREATE DATABASE promotion_db;
    CREATE DATABASE ticket_db;
    CREATE DATABASE notif_db;
    CREATE DATABASE chat_db;
    CREATE DATABASE keycloak_db;
EOSQL

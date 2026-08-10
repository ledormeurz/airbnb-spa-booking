#!/bin/sh
set -eu

HOST="${POSTGRES_HOST:-postgres}"
PORT="${POSTGRES_PORT:-5432}"
DB="${POSTGRES_DB:-airbnb_spa}"
DB_USER="${POSTGRES_USER:-airbnb}"

# Pre-register the Postgres server (password still entered in the UI / saved in volume).
cat > /pgadmin4/servers.json <<EOF
{
  "Servers": {
    "1": {
      "Name": "Airbnb Spa PostgreSQL",
      "Group": "Servers",
      "Host": "${HOST}",
      "Port": ${PORT},
      "MaintenanceDB": "${DB}",
      "Username": "${DB_USER}",
      "SSLMode": "prefer",
      "Favorite": true
    }
  }
}
EOF

exec /entrypoint.sh "$@"

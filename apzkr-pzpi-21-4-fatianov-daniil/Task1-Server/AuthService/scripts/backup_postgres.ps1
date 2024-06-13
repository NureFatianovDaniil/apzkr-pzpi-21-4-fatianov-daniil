# Змінні
$PGUSER = "postgres"
$PGPASSWORD = "pstgrsql_admin_fia"
$PGHOST = "localhost"
$PGPORT = "5432"
$PGDATABASE = "Drone-delivery-user-database"

# Дата і час для назви файлу
$BACKUP_DATE = Get-Date -Format "yyyyMMdd_HHmmss"

# Директорія
$BACKUP_DIR = "D:\Files\Nure\3_2\Software architecture\Project\apz-pzpi-21-4-fatianov-daniil\Task2\AuthService\backups"

# Назва файлу бекапу
$BACKUP_FILE = "$BACKUP_DIR\postgresql_backup_$BACKUP_DATE.sql"

# Повний шлях до pg_dump
$pg_dump_path = "D:\Setup\PostgreSQL\bin\pg_dump.exe"

# Бекап
$env:PGPASSWORD = $PGPASSWORD
$pg_dump_command = "& `"$pg_dump_path`" -U $PGUSER -h $PGHOST -p $PGPORT -d $PGDATABASE -F c -b -v -f `"$BACKUP_FILE`""

# Виконання команди
Invoke-Expression $pg_dump_command
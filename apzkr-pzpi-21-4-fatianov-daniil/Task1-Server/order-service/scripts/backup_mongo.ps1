# Змінні
$MONGOUSER = "rootuser"
$MONGOPASSWORD = "rootpass"
$MONGOHOST = "localhost"
$MONGOPORT = "27017"
$MONGODATABASE = "ddp_orders"
$MONGOAUTHDB = "admin"
$CONTAINER_NAME = "mongodb"  # Ім'я контейнера MongoDB

# Дата та час для назви файлу
$BACKUP_DATE = Get-Date -Format "yyyyMMdd_HHmmss"

# Директорія
$BACKUP_DIR = "D:\Files\Nure\3_2\Software architecture\Project\apz-pzpi-21-4-fatianov-daniil\Task2\order-service\backups"

# Назва файлу
$BACKUP_FILE = "$BACKUP_DIR\mongodb_backup_$BACKUP_DATE.gz"

# Рядок підключення до MongoDB
$mongo_connection_string = "mongodb://$MONGOUSER:$MONGOPASSWORD@$MONGOHOST:$MONGOPORT/$MONGODATABASE?authSource=$MONGOAUTHDB"

# Команда для виконання бекапу всередині контейнера
$mongodump_command = "mongodump --uri=$mongo_connection_string --archive=$BACKUP_FILE --gzip"

# Виконання команди всередині контейнера
$full_command = "docker exec $CONTAINER_NAME /bin/bash -c `"$mongodump_command`""
Invoke-Expression $full_command

# Перевірка виконання
if ($LastExitCode -eq 0) {
  Write-Output "MongoDB backup completed successfully."
} else {
  Write-Output "MongoDB backup failed."
}
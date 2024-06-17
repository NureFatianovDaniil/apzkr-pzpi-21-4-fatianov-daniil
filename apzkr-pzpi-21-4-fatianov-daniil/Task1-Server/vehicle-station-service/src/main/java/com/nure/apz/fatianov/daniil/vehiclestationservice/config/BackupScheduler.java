package com.nure.apz.fatianov.daniil.vehiclestationservice.config;

import com.nure.apz.fatianov.daniil.vehiclestationservice.service.PostgresBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class BackupScheduler {
    @Autowired
    private PostgresBackupService postgresBackupService;

    @Scheduled(cron = "0 16 5 * * *")
    public void schedulePostgresBackup() {
        postgresBackupService.backup();
    }
}

package com.nure.apz.fatianov.daniil.orderservice.config;

import com.nure.apz.fatianov.daniil.orderservice.service.MongoDBBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class BackupSheduler {
    @Autowired
    private MongoDBBackupService mongoBackupService;

    @Scheduled(cron = "0 0 6 * * *")
    public void scheduleMongoBackup() {
        mongoBackupService.backup();
    }
}

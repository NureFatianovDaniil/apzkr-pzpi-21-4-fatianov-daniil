package com.nure.apz.fatianov.daniil.vehiclestationservice.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class PostgresBackupService {
    private final String backupScript = "D:\\Files\\Nure\\3_2\\Software architecture\\Project\\apz-pzpi-21-4-fatianov-daniil\\Task2\\vehicle-station-service\\scripts\\backup_postgres.ps1";

    @Scheduled(cron = "0 0 6 * * *")
    public void backup() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", backupScript);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("PostgreSQL backup script executed successfully.");
            } else {
                System.err.println("PostgreSQL backup script execution failed.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

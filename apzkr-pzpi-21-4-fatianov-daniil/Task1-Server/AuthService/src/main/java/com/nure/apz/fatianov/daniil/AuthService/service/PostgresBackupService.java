package com.nure.apz.fatianov.daniil.AuthService.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class PostgresBackupService {
    private final String backupScript = "D:\\Files\\Nure\\3_2\\Software architecture\\Project\\apz-pzpi-21-4-fatianov-daniil\\Task2\\AuthService\\scripts\\backup_postgres.ps1";

    public void backup() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", backupScript);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            System.out.println("Output:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("Error:");
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("PostgreSQL backup script executed successfully.");
            } else {
                System.err.println("PostgreSQL backup script execution failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

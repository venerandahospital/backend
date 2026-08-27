package org.example.backup.services.payloads;

public class BackupActionResultDTO {
    public boolean success;
    public String message;
    public String lastBackupAt;
    public String lastBackupStatus;
    public String localFilePath;
    public String dropboxPath;
}

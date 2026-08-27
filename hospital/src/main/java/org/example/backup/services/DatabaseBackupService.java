package org.example.backup.services;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DatabaseBackupService {

    @ConfigProperty(name = "quarkus.datasource.username")
    String dbUser;

    @ConfigProperty(name = "quarkus.datasource.password")
    String dbPassword;

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String jdbcUrl;

    @ConfigProperty(name = "backup.mysqldump-path", defaultValue = "mysqldump")
    String mysqldumpPath;

    @ConfigProperty(name = "backup.mysql-path", defaultValue = "mysql")
    String mysqlPath;

    public String extractDatabaseName() {
        // jdbc:mysql://localhost:3306/vena
        int slash = jdbcUrl.lastIndexOf('/');
        if (slash < 0 || slash == jdbcUrl.length() - 1) {
            return "vena";
        }
        String tail = jdbcUrl.substring(slash + 1);
        int q = tail.indexOf('?');
        return q >= 0 ? tail.substring(0, q) : tail;
    }

    public Path createDump(Path targetFile) throws Exception {
        Files.createDirectories(targetFile.getParent());
        String dbName = extractDatabaseName();

        ProcessBuilder pb = new ProcessBuilder(
                mysqldumpPath,
                "-h", "localhost",
                "-u", dbUser,
                "--password=" + dbPassword,
                "--single-transaction",
                "--routines",
                "--triggers",
                dbName
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (var in = process.getInputStream()) {
            Files.copy(in, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        int code = process.waitFor();
        if (code != 0) {
            throw new IllegalStateException("mysqldump failed with exit code " + code);
        }
        if (!Files.exists(targetFile) || Files.size(targetFile) == 0) {
            throw new IllegalStateException("Backup file was not created or is empty");
        }
        return targetFile;
    }

    public void restoreFromDump(Path dumpFile) throws Exception {
        if (!Files.exists(dumpFile)) {
            throw new IllegalStateException("Backup file not found: " + dumpFile);
        }

        Path sqlFile = dumpFile;
        Path extracted = null;
        String name = dumpFile.getFileName().toString().toLowerCase();
        if (name.endsWith(".zip")) {
            extracted = extractSqlFromZip(dumpFile);
            sqlFile = extracted;
        }

        try {
            restoreSqlFile(sqlFile);
        } finally {
            if (extracted != null) {
                Files.deleteIfExists(extracted);
            }
        }
    }

    private void restoreSqlFile(Path sqlFile) throws Exception {
        String dbName = extractDatabaseName();

        ProcessBuilder pb = new ProcessBuilder(
                mysqlPath,
                "-h", "localhost",
                "-u", dbUser,
                "--password=" + dbPassword,
                dbName
        );
        pb.redirectInput(sqlFile.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder err = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                err.append(line).append('\n');
            }
        }

        int code = process.waitFor();
        if (code != 0) {
            throw new IllegalStateException("mysql restore failed: " + err);
        }
    }

    /** Unzip and return backup.sql if present, otherwise the first .sql entry. */
    private Path extractSqlFromZip(Path zipFile) throws Exception {
        Path preferred = null;
        Path firstSql = null;

        try (java.util.zip.ZipInputStream zis =
                     new java.util.zip.ZipInputStream(Files.newInputStream(zipFile))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                String base = entryName.substring(entryName.lastIndexOf('/') + 1).toLowerCase();
                if (!base.endsWith(".sql")) {
                    continue;
                }
                Path target = Files.createTempFile("kmc-entry-", ".sql");
                Files.copy(zis, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                if ("backup.sql".equals(base)) {
                    if (preferred != null) {
                        Files.deleteIfExists(preferred);
                    }
                    preferred = target;
                } else if (firstSql == null) {
                    firstSql = target;
                } else {
                    Files.deleteIfExists(target);
                }
            }
        }

        Path chosen = preferred != null ? preferred : firstSql;
        if (chosen == null) {
            throw new IllegalStateException("No .sql file found inside zip: " + zipFile);
        }
        if (preferred != null && firstSql != null && !preferred.equals(firstSql)) {
            Files.deleteIfExists(firstSql);
        }
        if (Files.size(chosen) == 0) {
            Files.deleteIfExists(chosen);
            throw new IllegalStateException("Extracted SQL dump is empty");
        }
        return chosen;
    }

    public Path buildLatestLocalPath(Path facilityDir) {
        return facilityDir.resolve("kmc.db");
    }

    /** Remove older timestamped dumps; only the single latest file is kept. */
    public void removeStaleLocalBackups(Path facilityDir) throws Exception {
        if (!Files.isDirectory(facilityDir)) {
            return;
        }
        Path latest = buildLatestLocalPath(facilityDir);
        try (Stream<Path> entries = Files.list(facilityDir)) {
            entries.filter(Files::isRegularFile)
                    .filter(p -> !p.equals(latest))
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.startsWith("kmc-") || name.endsWith(".sql");
                    })
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception ignored) {
                            // best-effort cleanup
                        }
                    });
        }
    }
}

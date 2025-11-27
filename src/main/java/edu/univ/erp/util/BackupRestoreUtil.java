package edu.univ.erp.util;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class BackupRestoreUtil {

    private static final String USERNAME = "root";
    private static final String PASSWORD = "Ishank@17";
    private static final String DB_NAME  = "erp_db";

    /**
     * Detect MySQL executable across Windows, Mac, Linux.
     */


    private static String mysqlBin(String exe) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {

            String[] guess = {
                    "C:/xampp/mysql/bin/" + exe + ".exe",
                    "C:/Program Files/MySQL/MySQL Server 8.0/bin/" + exe + ".exe",
                    exe + ".exe"
            };

            for (String g : guess) {
                if (new File(g).exists()) return g;
            }
            return exe + ".exe";

        } else {
            // macOS / Linux locations
            String[] guess = {
                    "/usr/local/mysql/bin/" + exe,
                    "/usr/local/bin/" + exe,
                    "/opt/homebrew/bin/" + exe,
                    "/usr/bin/" + exe,
                    exe
            };

            for (String g : guess) {
                if (new File(g).exists()) return g;
            }

            return exe;
        }
    }

    /**
     * Perform DB BACKUP → write SQL dump into output file.
     */
    public static void backupDatabase(File outputFile) throws Exception {

        String dumpExe = mysqlBin("mysqldump");

        ProcessBuilder pb = new ProcessBuilder(
                dumpExe,
                "-u" + USERNAME,
                "--password=" + PASSWORD,
                "--databases",
                DB_NAME
        );

        pb.redirectErrorStream(true);

        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
             FileWriter fw = new FileWriter(outputFile)) {

            String line;
            while ((line = br.readLine()) != null) {
                fw.write(line + "\n");
            }
        }

        if (p.waitFor() != 0) {
            throw new Exception("mysqldump failed — invalid path or password.");
        }
    }

    /**
     * Perform DB RESTORE → read SQL file & feed to MySQL CLI.
     */
    public static void restoreDatabase(File inputFile) throws Exception {

        String mysqlExe = mysqlBin("mysql");

        ProcessBuilder pb = new ProcessBuilder(
                mysqlExe,
                "-u" + USERNAME,
                "--password=" + PASSWORD,
                DB_NAME
        );

        pb.redirectErrorStream(true);

        Process p = pb.start();

        // Write SQL file into MySQL input stream
        try (OutputStream os = p.getOutputStream();
             FileInputStream fis = new FileInputStream(inputFile)) {

            fis.transferTo(os);
            os.flush();
        }

        // Read output log
        StringBuilder log = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                log.append(line).append("\n");
            }
        }

        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new Exception("mysql restore failed.\nDetails:\n" + log);
        }
    }
}

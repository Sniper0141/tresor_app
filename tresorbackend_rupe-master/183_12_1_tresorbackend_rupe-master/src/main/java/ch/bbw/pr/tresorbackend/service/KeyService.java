package ch.bbw.pr.tresorbackend.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Service
public class KeyService {
    public String getPrivateKey() throws FileNotFoundException {
        var secretFile = new File("C:\\Development\\Schule\\Modul_183\\secrets\\tresor_app_master_key.txt");

        Scanner scanner;
        scanner = new Scanner(secretFile);

        StringBuilder result = new StringBuilder();
        while (scanner.hasNextLine()) {
            var line = scanner.nextLine();
            result.append(line).append("\n");
        }

        return result.toString();
    }

    public String getPublicKey() throws FileNotFoundException {
        var secretFile = new File("C:\\Development\\Schule\\Modul_183\\secrets\\tresor_app_public_key.txt");

        Scanner scanner;
        scanner = new Scanner(secretFile);

        StringBuilder result = new StringBuilder();
        while (scanner.hasNextLine()) {
            var line = scanner.nextLine();
            result.append(line).append("\n");
        }

        return result.toString();
    }
}

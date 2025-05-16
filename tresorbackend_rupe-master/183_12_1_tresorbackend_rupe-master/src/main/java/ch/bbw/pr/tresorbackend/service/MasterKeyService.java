package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

@Service
public class MasterKeyService {


    public String getMasterKey() throws FileNotFoundException {
        var secretFile = new File("C:\\Development\\Schule\\Modul_183\\secrets\\tresor_app\\tresor_app_master_key.txt");

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

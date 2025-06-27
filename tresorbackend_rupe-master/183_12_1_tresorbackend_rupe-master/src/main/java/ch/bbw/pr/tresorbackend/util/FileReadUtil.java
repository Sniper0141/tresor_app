package ch.bbw.pr.tresorbackend.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileReadUtil {
    public static String readFile(String path) throws FileNotFoundException {
        var secretFile = new File(path);
        Scanner scanner = new Scanner(secretFile);

        StringBuilder result = new StringBuilder();
        while (scanner.hasNextLine()) {
            var line = scanner.nextLine();
            result.append(line).append("\n");
        }

        return result.toString();
    }
}

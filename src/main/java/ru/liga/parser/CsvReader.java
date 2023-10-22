package ru.liga.parser;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public class CsvReader {

    public List<String> readAllLines(String csvPath) {
        try {
            return Files.readAllLines(new File(getClass().getClassLoader().getResource(csvPath).toURI()).toPath());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
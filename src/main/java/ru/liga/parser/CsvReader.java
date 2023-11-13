package ru.liga.parser;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@Log4j2
public class CsvReader {

    /**
     * Читает CSV файл.
     *
     * @param csvPath Путь к CSV файлу.
     * @return Список строк, представляющих данные из файла.
     */
    public List<String> readAllLines(String csvPath) {
        try {
            return Files.readAllLines(new File(getClass().getClassLoader().getResource(csvPath).toURI()).toPath());
        } catch (Exception e) {
            log.error("Ошибка при чтении файла: {}. {}", csvPath, e.getMessage());
            return Collections.emptyList();
        }
    }
}
package hu.rool.roolcsvtranslator.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import hu.rool.roolcsvtranslator.model.Translation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TranslatorIOService {

    public List<Translation> loadCsv(String filePath) {
        List<Translation> translations = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String[] header = reader.readNext();
            if (header == null || header.length < 2) {
                log.error("CSV file is empty or does not have the expected header: {}", filePath);
                return translations;
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 2) {
                    log.warn("Skipping malformed line in CSV file: {}", String.join(",", line));
                } else {
                    Translation translation = Translation.builder()
                            .originalHungarianText(line[0])
                            .machineTranslation(line[1])
                            .build();

                    translations.add(translation);
                }
            }
            log.info("Successfully loaded CSV file: {}", filePath);
        } catch (IOException | CsvValidationException e) {
            log.error("Error reading CSV file: {}", filePath, e);
        }

        return translations;
    }

    public void saveCsv(String filePath, List<Translation> translations) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{"Lefordítandó szöveg", "Leforított szöveg", "ChatGPT Fordítás"});

            for (Translation translation : translations) {
                writer.writeNext(new String[]{
                        translation.getOriginalHungarianText(),
                        translation.getMachineTranslation(),
                        translation.getChatGptTranslation() != null ? translation.getChatGptTranslation() : ""
                });
            }
            log.info("Successfully saved CSV file: {}", filePath);
        } catch (IOException e) {
            log.error("Error saving CSV file: {}", filePath, e);
        }
    }
}
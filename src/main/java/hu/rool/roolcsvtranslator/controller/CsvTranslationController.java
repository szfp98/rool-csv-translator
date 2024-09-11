package hu.rool.roolcsvtranslator.controller;

import hu.rool.roolcsvtranslator.model.Translation;
import hu.rool.roolcsvtranslator.service.TranslationService;
import hu.rool.roolcsvtranslator.service.TranslatorIOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvTranslationController implements CommandLineRunner {

    private final TranslatorIOService translatorIOService;
    private final TranslationService translationService;

    @Override
    public void run(String... args) {
        if (args.length < 1 || !StringUtils.hasText(args[0])) {
            log.error("CSV file path must be provided as a command line argument.");
            System.exit(1);
        }

        String inputFilePath = args[0];
        String outputFilePath = getOutputFilePath(inputFilePath);

        log.info("Starting translation process for file: {}", inputFilePath);

        try {
            List<Translation> translations = translatorIOService.loadCsv(inputFilePath);

            if (translations.isEmpty()) {
                log.warn("No data loaded from CSV file: {}", inputFilePath);
                return;
            }

            List<Translation> translatedTexts = translationService.translateWithChatGpt(translations);

            translatorIOService.saveCsv(outputFilePath, translatedTexts);

            log.info("Translation process completed. Results saved to: {}", outputFilePath);
        } catch (Exception e) {
            log.error("An error occurred during the translation process.", e);
        }
    }

   private String getOutputFilePath(String inputFilePath) {
        int lastDotIndex = inputFilePath.lastIndexOf('.');
        String baseName = (lastDotIndex == -1) ? inputFilePath : inputFilePath.substring(0, lastDotIndex);
        String extension = (lastDotIndex == -1) ? "" : inputFilePath.substring(lastDotIndex);
        return baseName + "-translated" + extension;
    }
}
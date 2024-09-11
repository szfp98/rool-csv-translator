package hu.rool.roolcsvtranslator.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hu.rool.roolcsvtranslator.model.Translation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final WebClient webClient;
    private final Gson gson = new Gson();

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    public List<Translation> translateWithChatGpt(List<Translation> translations) {
        for (Translation translation : translations) {
            if (translation.getOriginalHungarianText() != null && !translation.getOriginalHungarianText().isEmpty()) {
                try {
                    String chatGptTranslation = translateText(translation.getOriginalHungarianText());
                    translation = Translation.builder()
                            .originalHungarianText(translation.getOriginalHungarianText())
                            .machineTranslation(translation.getMachineTranslation())
                            .chatGptTranslation(chatGptTranslation)
                            .build();
                    log.info("Translation successful for text: {}", translation.getOriginalHungarianText());
                } catch (Exception e) {
                    log.error("Error translating text: {}", translation.getOriginalHungarianText(), e);
                }
            }
        }
        return translations;
    }

    private String translateText(String text) {
        try {
            Mono<String> response = webClient.post()
                    .uri(openAiApiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(constructRequestBody(text))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> Mono.error(new RuntimeException("API error: " + clientResponse.statusCode())))
                    .bodyToMono(String.class);

            String result = response.block();
            log.info("Received response from OpenAI API: {}", result);
            return parseResponse(Objects.requireNonNull(result));
        } catch (WebClientResponseException e) {
            log.error("API response error: Status code: {}, Response body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "API Error";
        } catch (Exception e) {
            log.error("Failed to translate text using ChatGPT: {}", text, e);
            return "Translation Error";
        }
    }

    private String constructRequestBody(String text) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4");

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "Translate this to English: " + text);
        messages.add(message);

        requestBody.add("messages", messages);

        return gson.toJson(requestBody);
    }

    private String parseResponse(String response) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (!choices.isEmpty()) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                String translation = message.get("content").getAsString();
                log.info("Parsed translation from response: {}", translation);
                return translation;
            } else {
                log.warn("No choices found in response: {}", response);
                return "No translation found";
            }
        } catch (Exception e) {
            log.error("Error parsing response: {}", response, e);
            return "Parsing Error";
        }
    }
}
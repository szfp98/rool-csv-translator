package hu.rool.roolcsvtranslator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Translation {
    private String originalHungarianText;
    private String machineTranslation;
    private String chatGptTranslation;
}
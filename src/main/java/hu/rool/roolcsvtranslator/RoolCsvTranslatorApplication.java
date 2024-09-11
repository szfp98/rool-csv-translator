package hu.rool.roolcsvtranslator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoolCsvTranslatorApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RoolCsvTranslatorApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run("kissist_20240730_175522_v2.csv");
    }

}

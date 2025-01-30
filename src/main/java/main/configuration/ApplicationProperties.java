package main.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;


import java.util.List;
import java.util.Map;


@Setter
@Getter
@Configuration
@ConfigurationPropertiesScan
@ConfigurationProperties
public class ApplicationProperties {

    private List<Map<String, String>> sites;
    private String userAgent;
}

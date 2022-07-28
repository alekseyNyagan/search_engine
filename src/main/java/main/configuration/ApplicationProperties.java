package main.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;


import java.util.List;
import java.util.Map;


@Configuration
@ConfigurationPropertiesScan
@ConfigurationProperties
public class ApplicationProperties {

    private List<Map<String, String>> sites;
    private String userAgent;

    public List<Map<String, String>> getSites() {
        return sites;
    }

    public void setSites(List<Map<String, String>> sites) {
        this.sites = sites;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}

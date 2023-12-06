package main.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(
        title = "Search Engine Api",
        description = "Search Engine", version = "1.0.0",
        contact = @Contact(name = "Aleksey")
))
public class OpenApiConfig {
}

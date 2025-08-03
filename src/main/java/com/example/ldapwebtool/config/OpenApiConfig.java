package com.example.ldapwebtool.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LDAP REST API")
                        .description("A Spring Boot REST API for performing LDAP operations via HTTP requests. " +
                                   "This tool acts as a proxy between HTTP clients and LDAP servers, supporting both JSON and LDIF formats.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("LDAP Web Tool")
                                .email("admin@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8090").description("Development Server"),
                        new Server().url("https://your-domain.com").description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("HTTP Basic Authentication using LDAP DN and password")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}

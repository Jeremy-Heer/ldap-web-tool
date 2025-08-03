package com.example.ldapwebtool.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for dual HTTP/HTTPS support.
 * This configuration is activated when using the 'dual' profile.
 * It configures HTTPS as the primary connector and adds an HTTP connector.
 */
@Configuration
@Profile("dual")
public class DualProtocolConfig {

    @Value("${ldapwebtool.http.port:8090}")
    private int httpPort;

    /**
     * Configures the servlet container to support both HTTP and HTTPS.
     * HTTPS runs on the main server port (configured in application properties).
     * HTTP runs on the additional port specified by ldapwebtool.http.port.
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createHttpConnector());
        return tomcat;
    }

    /**
     * Creates an HTTP connector for the additional port.
     */
    private Connector createHttpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        return connector;
    }
}

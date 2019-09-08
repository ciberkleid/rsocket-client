package com.example.rsocketclient;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rsocket.sample.server")
@Getter
@Setter
public class ConfigProperties {

    private String transport = "tcp";
    private String host = "localhost";
    private Integer port = 7000;

}

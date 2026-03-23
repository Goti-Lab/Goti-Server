package com.goti.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goti.mesh")
public record MeshProperties(boolean enabled) {
}

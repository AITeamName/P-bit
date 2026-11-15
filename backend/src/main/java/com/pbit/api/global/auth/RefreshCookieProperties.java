package com.pbit.api.global.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

// app.refresh-cookie.*  로컬(http)에서만 secure=false
@ConfigurationProperties(prefix = "app.refresh-cookie")
public record RefreshCookieProperties(boolean secure) {
}

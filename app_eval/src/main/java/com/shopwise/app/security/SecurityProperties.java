package com.shopwise.app.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shopwise.security")
public class SecurityProperties {
    private String adminUsername = "admin";
    private String adminPassword = "admin123";
    private String merchantUsername = "merchant";
    private String merchantPassword = "merchant123";
    private Duration tokenTtl = Duration.ofHours(8);

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    public String getMerchantUsername() { return merchantUsername; }
    public void setMerchantUsername(String merchantUsername) { this.merchantUsername = merchantUsername; }
    public String getMerchantPassword() { return merchantPassword; }
    public void setMerchantPassword(String merchantPassword) { this.merchantPassword = merchantPassword; }
    public Duration getTokenTtl() { return tokenTtl; }
    public void setTokenTtl(Duration tokenTtl) { this.tokenTtl = tokenTtl; }
}

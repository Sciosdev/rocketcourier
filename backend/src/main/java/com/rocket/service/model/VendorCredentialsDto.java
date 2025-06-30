package com.rocket.service.model;

public class VendorCredentialsDto {

    private String shopifyApiKey;
    private String shopifyAccessToken;
    private String shopifyStoreUrl;

    public String getShopifyApiKey() {
        return shopifyApiKey;
    }

    public void setShopifyApiKey(String shopifyApiKey) {
        this.shopifyApiKey = shopifyApiKey;
    }

    public String getShopifyAccessToken() {
        return shopifyAccessToken;
    }

    public void setShopifyAccessToken(String shopifyAccessToken) {
        this.shopifyAccessToken = shopifyAccessToken;
    }

    public String getShopifyStoreUrl() {
        return shopifyStoreUrl;
    }

    public void setShopifyStoreUrl(String shopifyStoreUrl) {
        this.shopifyStoreUrl = shopifyStoreUrl;
    }
}

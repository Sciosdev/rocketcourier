package com.rocket.service.model;

public class VendorCredentialsDto {

    private String shopifyAccessToken;
    private String shopifyStoreUrl;
    private String shopifyApiVersion;

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

    public String getShopifyApiVersion() {
        return shopifyApiVersion;
    }

    public void setShopifyApiVersion(String shopifyApiVersion) {
        this.shopifyApiVersion = shopifyApiVersion;
    }
}

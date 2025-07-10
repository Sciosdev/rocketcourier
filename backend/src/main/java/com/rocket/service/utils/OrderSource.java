package com.rocket.service.utils;

public enum OrderSource {
    SHOPIFY("SHOPIFY"),
    MERCADO_LIBRE("MERCADO_LIBRE");

    private final String value;

    OrderSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

package com.rocket.service.exception;

public class ShopifyApiException extends RuntimeException {
    public ShopifyApiException(String message) {
        super(message);
    }

    public ShopifyApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.rocket.service.model;

public class ShopifyFulfillmentData {
    private String fulfillmentOrderId;
    private String lineItemId;
    private Integer quantity;

    public ShopifyFulfillmentData() {}

    public ShopifyFulfillmentData(String fulfillmentOrderId, String lineItemId, Integer quantity) {
        this.fulfillmentOrderId = fulfillmentOrderId;
        this.lineItemId = lineItemId;
        this.quantity = quantity;
    }

    public String getFulfillmentOrderId() {
        return fulfillmentOrderId;
    }

    public void setFulfillmentOrderId(String fulfillmentOrderId) {
        this.fulfillmentOrderId = fulfillmentOrderId;
    }

    public String getLineItemId() {
        return lineItemId;
    }

    public void setLineItemId(String lineItemId) {
        this.lineItemId = lineItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

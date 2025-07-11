package com.rocket.service.entity;

import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonFormat; // Added import
import com.rocket.service.utils.OrderSource;

public class OrderDto {
	private String id;
	@BsonId
	private ObjectId orderKey;
	private String name; // Validado por registroService.validacion()
	// @Email // Puede permitir cadena vacía, @NotEmpty no. Se valida en registroService si es necesario.
	private String email;
	@NotEmpty(message = "vendor es un campo requerido")
	private String vendor; // Mantenemos esta, ya que 'user' se usa para esto.
	// @NotEmpty(message = "risk_level es un campo requerido") // Shopify puede no enviarlo, mapeado a "N/A"
	private String risk_level;
	// @NotEmpty(message = "source es un campo requerido") // Mapeado a "SHOPIFY"
	private String source;
	@NotEmpty(message = "financial_status es un campo requerido")
	private String financial_status; // Crítico para la lógica de guardado
	// @NotEmpty(message = "accepts_marketing es un campo requerido") // Puede ser 'no' o no venir
	private String accepts_marketing;
	// @NotEmpty(message = "currency es un campo requerido") // Shopify debería enviarlo
	private String currency;
	private double subtotal;	
	// @NotEmpty es incorrecto para double. Si se requiere que no sea cero, usar @Min(0) o validación lógica.
	private double shipping;
	// @NotEmpty(message = "shipping_method es un campo requerido") // Puede ser vacío si no hay shipping lines
        private String shipping_method;

        // Shopify fulfillment metadata
        private String fulfillmentOrderId;
        private String fulfillmentLineItemId;
        private Integer fulfillmentLineItemQty;
        private String fulfillmentId; // id devuelto al crear el fulfillment

	@NotNull(message = "created_at Es un campo requerido")
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
	private Date created_at;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getRisk_level() {
		return risk_level;
	}
	public void setRisk_level(String risk_level) {
		this.risk_level = risk_level;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getFinancial_status() {
		return financial_status;
	}
	public void setFinancial_status(String financial_status) {
		this.financial_status = financial_status;
	}
	public String getAccepts_marketing() {
		return accepts_marketing;
	}
	public void setAccepts_marketing(String accepts_marketing) {
		this.accepts_marketing = accepts_marketing;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public double getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}
	public double getShipping() {
		return shipping;
	}
	public void setShipping(double shipping) {
		this.shipping = shipping;
	}
	public String getShipping_method() {
		return shipping_method;
	}
        public void setShipping_method(String shipping_method) {
                this.shipping_method = shipping_method;
        }

        public String getFulfillmentOrderId() {
                return fulfillmentOrderId;
        }

        public void setFulfillmentOrderId(String fulfillmentOrderId) {
                this.fulfillmentOrderId = fulfillmentOrderId;
        }

        public String getFulfillmentLineItemId() {
                return fulfillmentLineItemId;
        }

        public void setFulfillmentLineItemId(String fulfillmentLineItemId) {
                this.fulfillmentLineItemId = fulfillmentLineItemId;
        }

        public Integer getFulfillmentLineItemQty() {
                return fulfillmentLineItemQty;
        }

        public void setFulfillmentLineItemQty(Integer fulfillmentLineItemQty) {
                this.fulfillmentLineItemQty = fulfillmentLineItemQty;
        }

        public String getFulfillmentId() {
                return fulfillmentId;
        }

        public void setFulfillmentId(String fulfillmentId) {
                this.fulfillmentId = fulfillmentId;
        }
        public Date getCreated_at() {
                return created_at;
        }
	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}
	public ObjectId getOrderKey() {
		return orderKey;
	}
        public void setOrderKey(ObjectId orderKey) {
                this.orderKey = orderKey;
        }

        public boolean isShopifyOrder() {
                return OrderSource.SHOPIFY.getValue().equalsIgnoreCase(this.source);
        }
}

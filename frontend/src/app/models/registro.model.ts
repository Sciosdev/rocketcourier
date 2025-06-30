export interface Registro {
    id:               number;
    rowNumber:        string;
    idCarga:          number;
    idEstatus:        number;
    order:            Order;
    billing_address:  Address;
    shipping_address: Address;
    payment:          Payment;
    extra:            Extra;
    scheduled:        Scheduled;
}

export interface Address {
    name:          string;
    street:        string;
    address1:      string;
    address2:      string;
    city:          string;
    zip:           string;
    province:      string;
    province_name: string;
    country:       string;
    phone:         string;
}

export interface Extra {
    fulfillment_status:          string;
    taxes:                       number;
    discount_amount:             string;
    lineitem_quantity:           string;
    lineitem_name:               string;
    lineitem_price:              string;
    lineitem_sku:                string;
    lineitem_requires_shipping:  string;
    lineitem_taxable:            string;
    lineitem_fulfillment_status: string;
    lineitem_discount:           string;
    note_attributes:             string;
    refunded_amount:             string;
}

export interface Order {
    id:                string;
    orderKey:          OrderKey;
    name:              string;
    email:             string;
    vendor:            string;
    risk_level:        string;
    source:            string;
    financial_status:  string;
    accepts_marketing: string;
    currency:          string;
    subtotal:          number;
    shipping:          number;
    shipping_method:   string;
    created_at:        Date;
}

export interface OrderKey {
    timestamp:    number;
    date:         Date;
}

export interface Payment {
    paid_at:           string;
    total:             number;
    payment_method:    string;
    payment_reference: string;
}

export interface Scheduled {
    scheduledDate?:     string;
    idVendor?:         string;
}

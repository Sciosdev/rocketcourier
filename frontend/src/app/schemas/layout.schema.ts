export const layout_map = [{
    "Id": {
        "column_id": "id",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Name": {
        "column_id": "name",
        "type": "order",
        "required": true,
        "nullable": false
    },
    "Email": {
        "column_id": "email",
        "type": "order",
        "required": true,
        "nullable": true
    },
    "Financial Status": {
        "column_id": "financial_status",
        "type": "order",
        "required": true,
        "nullable": false
    },
    "Paid at": {
        "column_id": "paid_at",
        "type": "payment",
        "required": false,
        "nullable": true
    },
    "Fulfillment Status": {
        "column_id": "fulfillment_status",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Fulfilled at": {
        "column_id": "fulfilled_at",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Accepts Marketing": {
        "column_id": "accepts_marketing",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Currency": {
        "column_id": "currency",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Subtotal": {
        "column_id": "subtotal",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Shipping": {
        "column_id": "shipping",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Taxes": {
        "column_id": "taxes",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Total": {
        "column_id": "total",
        "type": "payment",
        "required": false,
        "nullable": true
    },
    "Discount Code": {
        "column_id": "discount_code",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Discount Amount": {
        "column_id": "discount_amount",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Shipping Method": {
        "column_id": "shipping_method",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Created at": {
        "column_id": "created_at",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Lineitem quantity": {
        "column_id": "lineitem_quantity",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Lineitem name": {
        "column_id": "lineitem_name",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Lineitem price": {
        "column_id": "lineitem_price",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Lineitem compare at price": {
        "column_id": "lineitem_compare_at_price",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Lineitem sku": {
        "column_id": "lineitem_sku",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Lineitem requires shipping": {
        "column_id": "lineitem_requires_shipping",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Lineitem taxable": {
        "column_id": "lineitem_taxable",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Lineitem fulfillment status": {
        "column_id": "lineitem_fulfillment_status",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Billing Name": {
        "column_id": "name",
        "type": "billing_address",
        "required": true,
        "nullable": true
    },
    "Billing Street": {
        "column_id": "street",
        "type": "billing_address",
        "required": true,
        "nullable": true
    },
    "Billing Address1": {
        "column_id": "address1",
        "type": "billing_address",
        "required": true,
        "nullable": true
    },
    "Billing Address2": {
        "column_id": "address2",
        "type": "billing_address",
        "required": true,
        "nullable": true
    },
    "Billing Company": {
        "column_id": "company",
        "type": "billing_address",
        "required": true,
        "nullable": true
    },
    "Billing City": {
        "column_id": "city",
        "type": "billing_address",
        "required": true,
        "nullable": true
    },
    "Billing Zip": {
        "column_id": "zip",
        "type": "billing_address",
        "required": false,
        "nullable": true
    },
    "Billing Province": {
        "column_id": "province",
        "type": "billing_address",
        "required": true,
        "nullable": true
    },
    "Billing Country": {
        "column_id": "country",
        "type": "billing_address",
        "required": true,
        "nullable": true
    },
    "Billing Phone": {
        "column_id": "phone",
        "type": "billing_address",
        "required": true,
        "nullable": true
    },
    "Shipping Name": {
        "column_id": "name",
        "type": "shipping_address",
        "required": true,
        "nullable": false
    },
    "Shipping Street": {
        "column_id": "street",
        "type": "shipping_address",
        "required": true,
        "nullable": false
    },
    "Shipping Address1": {
        "column_id": "address1",
        "type": "shipping_address",
        "required": true,
        "nullable": false
    },
    "Shipping Address2": {
        "column_id": "address2",
        "type": "shipping_address",
        "required": true,
        "nullable": true
    },
    "Shipping Company": {
        "column_id": "company",
        "type": "shipping_address",
        "required": true,
        "nullable": true
    },
    "Shipping City": {
        "column_id": "city",
        "type": "shipping_address",
        "required": true,
        "nullable": false
    },
    "Shipping Zip": {
        "column_id": "zip",
        "type": "shipping_address",
        "required": false,
        "nullable": true
    },
    "Shipping Province": {
        "column_id": "province",
        "type": "shipping_address",
        "required": true,
        "nullable": false
    },
    "Shipping Country": {
        "column_id": "country",
        "type": "shipping_address",
        "required": true,
        "nullable": false
    },
    "Shipping Phone": {
        "column_id": "phone",
        "type": "shipping_address",
        "required": true,
        "nullable": true
    },
    "Notes": {
        "column_id": "notes",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Note Attributes": {
        "column_id": "note_attributes",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Cancelled at": {
        "column_id": "cancelled_at",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Payment Method": {
        "column_id": "payment_method",
        "type": "payment",
        "required": false,
        "nullable": true
    },
    "Payment Reference": {
        "column_id": "payment_reference",
        "type": "payment",
        "required": false,
        "nullable": true
    },
    "Refunded Amount": {
        "column_id": "refunded_amount",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Vendor": {
        "column_id": "vendor",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Tags": {
        "column_id": "tags",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Risk Level": {
        "column_id": "risk_level",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Source": {
        "column_id": "source",
        "type": "order",
        "required": false,
        "nullable": true
    },
    "Lineitem discount": {
        "column_id": "lineitem_discount",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 1 Name": {
        "column_id": "tax_1_name",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 1 Value": {
        "column_id": "tax_1_value",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 2 Name": {
        "column_id": "tax_2_name",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 2 Value": {
        "column_id": "tax_2_value",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 3 Name": {
        "column_id": "tax_3_name",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 3 Value": {
        "column_id": "tax_3_value",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 4 Name": {
        "column_id": "tax_4_name",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 4 Value": {
        "column_id": "tax_4_value",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 5 Name": {
        "column_id": "tax_5_name",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Tax 5 Value": {
        "column_id": "tax_5_value",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Phone": {
        "column_id": "phone",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Receipt Number": {
        "column_id": "receipt_number",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Duties": {
        "column_id": "duties",
        "type": "extra",
        "required": false,
        "nullable": true
    },
    "Billing Province Name": {
        "column_id": "province_name",
        "type": "billing_address",
        "required": false,
        "nullable": true
    },
    "Shipping Province Name": {
        "column_id": "province_name",
        "type": "shipping_address",
        "required": false,
        "nullable": true
    },
    "RUT Destinatario": {
        "column_id": "rut_destinatario",
        "type": "extra",
        "required": false,
        "nullable": true
    }
},
{
  "# de venta": {
      "column_id": "name",
      "type": "order",
      "required": false,
      "nullable": true
  },
  "Comprador": {
      "column_id": "name",
      "type": "shipping_address",
      "required": false,
      "nullable": true
  },
  "Domicilio": {
      "column_id": "address1",
      "type": "shipping_address",
      "required": false,
      "nullable": true
  },
  "Comuna": {
      "column_id": "city",
      "type": "shipping_address",
      "required": false,
      "nullable": true
  },
  "Código postal": {
      "column_id": "zip",
      "type": "shipping_address",
      "required": false,
      "nullable": true
  },
  "Estado": {
      "column_id": "province",
      "type": "shipping_address",
      "required": false,
      "nullable": true
  },
  "País": {
      "column_id": "country",
      "type": "shipping_address",
      "required": false,
      "nullable": true
  }
}
]

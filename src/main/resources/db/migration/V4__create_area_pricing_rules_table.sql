CREATE TABLE area_pricing_rules (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    printing_cost_per_square_meter NUMERIC(12, 2) NOT NULL,
    labor_cost NUMERIC(12, 2) NOT NULL DEFAULT 0,
    waste_percentage NUMERIC(5, 2) NOT NULL DEFAULT 0,
    margin_percentage NUMERIC(5, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_area_pricing_rule_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);

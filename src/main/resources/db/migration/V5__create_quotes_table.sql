CREATE TABLE quotes (
    id BIGSERIAL PRIMARY KEY,

    product_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,

    product_name VARCHAR(150) NOT NULL,
    material_name VARCHAR(150) NOT NULL,

    quantity INTEGER NOT NULL,

    width NUMERIC(12, 4) NOT NULL,
    height NUMERIC(12, 4) NOT NULL,

    unit_area NUMERIC(12, 4) NOT NULL,
    total_area NUMERIC(12, 4) NOT NULL,

    material_cost NUMERIC(12, 2) NOT NULL,
    printing_cost NUMERIC(12, 2) NOT NULL,
    finishing_cost NUMERIC(12, 2) NOT NULL,
    waste_cost NUMERIC(12, 2) NOT NULL,
    labor_cost NUMERIC(12, 2) NOT NULL,

    total_cost NUMERIC(12, 2) NOT NULL,

    margin_percentage NUMERIC(5, 2) NOT NULL,

    suggested_price NUMERIC(12, 2) NOT NULL,
    final_price NUMERIC(12, 2) NOT NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_quote_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT fk_quote_material
        FOREIGN KEY (material_id)
        REFERENCES materials(id)
);

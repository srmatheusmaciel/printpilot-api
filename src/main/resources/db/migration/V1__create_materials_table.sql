CREATE TABLE materials (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    unit_measure VARCHAR(50) NOT NULL,
    cost NUMERIC(12, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

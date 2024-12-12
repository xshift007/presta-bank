-- Crear la tabla credit_simulation
CREATE TABLE IF NOT EXISTS credit_simulation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    income DOUBLE,
    requested_amount DOUBLE,
    months INT,
    total_payment DOUBLE,
    monthly_payment DOUBLE
);

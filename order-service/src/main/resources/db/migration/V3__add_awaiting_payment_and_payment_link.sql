ALTER TABLE orders DROP CONSTRAINT chk_orders_status;
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'AWAITING_PAYMENT', 'PAID', 'CANCELLED'));

ALTER TABLE orders
    ADD COLUMN payment_link VARCHAR(2048);

USE moeats;

CREATE TABLE IF NOT EXISTS payment_transaction (
    payment_transaction_idx BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_idx INT NOT NULL,
    payment_share_idx INT NULL,
    member_idx INT NOT NULL,

    attempt_no INT NOT NULL DEFAULT 1,

    provider VARCHAR(20) NOT NULL DEFAULT 'TOSS',
    transaction_type VARCHAR(20) NOT NULL,
    merchant_order_id VARCHAR(64) NOT NULL,
    provider_payment_key VARCHAR(200) NULL,
    provider_method VARCHAR(30) NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'KRW',

    request_amount INT NOT NULL,
    approved_amount INT NULL,
    cancelled_amount INT NOT NULL DEFAULT 0,

    transaction_status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(300) NOT NULL,

    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    expired_at TIMESTAMP NULL,
    failed_at TIMESTAMP NULL,

    fail_code VARCHAR(100) NULL,
    fail_message VARCHAR(500) NULL,
    raw_response LONGTEXT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_payment_transaction_order_id UNIQUE (merchant_order_id),
    CONSTRAINT uk_payment_transaction_payment_key UNIQUE (provider_payment_key),
    CONSTRAINT uk_payment_transaction_share_attempt UNIQUE (payment_share_idx, attempt_no),

    CONSTRAINT chk_payment_transaction_type
        CHECK (transaction_type IN ('REPRESENTATIVE', 'INDIVIDUAL')),

    CONSTRAINT chk_payment_transaction_status
        CHECK (transaction_status IN (
            'READY',
            'IN_PROGRESS',
            'DONE',
            'FAILED',
            'CANCEL_PENDING',
            'CANCELLED',
            'EXPIRED'
        )),

    CONSTRAINT chk_payment_transaction_share_link
        CHECK (
            (transaction_type = 'REPRESENTATIVE' AND payment_share_idx IS NULL)
            OR
            (transaction_type = 'INDIVIDUAL' AND payment_share_idx IS NOT NULL)
        ),

    CONSTRAINT fk_payment_transaction_payment
        FOREIGN KEY (payment_idx) REFERENCES payment(payment_idx),

    CONSTRAINT fk_payment_transaction_payment_share
        FOREIGN KEY (payment_share_idx) REFERENCES payment_share(payment_share_idx),

    CONSTRAINT fk_payment_transaction_member
        FOREIGN KEY (member_idx) REFERENCES member(member_idx)
) ENGINE=InnoDB;

CREATE INDEX idx_payment_transaction_payment_status
    ON payment_transaction (payment_idx, transaction_status, created_at);

CREATE INDEX idx_payment_transaction_share_status
    ON payment_transaction (payment_share_idx, transaction_status, created_at);

CREATE INDEX idx_payment_transaction_member_status
    ON payment_transaction (member_idx, transaction_status, created_at);

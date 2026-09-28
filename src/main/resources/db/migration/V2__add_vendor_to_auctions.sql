ALTER TABLE auctions
    ALTER COLUMN buyer_id DROP NOT NULL;

ALTER TABLE auctions
    ADD COLUMN vendor_id BIGINT;

ALTER TABLE auctions
    ADD CONSTRAINT fk_auctions_vendor
        FOREIGN KEY (vendor_id)
            REFERENCES users(id);
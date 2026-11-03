CREATE TABLE shipments (

                           id UUID PRIMARY KEY,

                           tracking_number VARCHAR(40) NOT NULL,

                           recipient_name VARCHAR(120) NOT NULL,

                           origin VARCHAR(120) NOT NULL,

                           destination VARCHAR(120) NOT NULL,

                           status VARCHAR(32) NOT NULL DEFAULT 'CREATED',

                           version BIGINT NOT NULL DEFAULT 0,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT uq_shipments_tracking_number
                               UNIQUE (tracking_number),

                           CONSTRAINT chk_shipments_status
                               CHECK (status IN (
                                                 'CREATED',
                                                 'PICKED_UP',
                                                 'IN_TRANSIT',
                                                 'OUT_FOR_DELIVERY',
                                                 'DELIVERED'
                                   ))
);
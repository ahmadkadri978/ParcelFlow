CREATE TABLE shipment_events (

                                 id UUID PRIMARY KEY,

                                 shipment_id UUID NOT NULL,

                                 event_type VARCHAR(20) NOT NULL,

                                 previous_status VARCHAR(32),

                                 new_status VARCHAR(32) NOT NULL,

                                 sequence_number BIGINT NOT NULL,

                                 occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_shipment_events_shipment
                                     FOREIGN KEY (shipment_id)
                                         REFERENCES shipments(id),

                                 CONSTRAINT uq_shipment_events_sequence
                                     UNIQUE (shipment_id, sequence_number),

                                 CONSTRAINT chk_shipment_events_type
                                     CHECK (event_type IN (
                                                           'BASELINE',
                                                           'CREATED',
                                                           'STATUS_CHANGED'
                                         )),

                                 CONSTRAINT chk_shipment_events_status
                                     CHECK (new_status IN (
                                                           'CREATED',
                                                           'PICKED_UP',
                                                           'IN_TRANSIT',
                                                           'OUT_FOR_DELIVERY',
                                                           'DELIVERED'
                                         )),

                                 CONSTRAINT chk_shipment_events_previous_status
                                     CHECK (
                                         previous_status IS NULL
                                             OR previous_status IN (
                                                                    'CREATED',
                                                                    'PICKED_UP',
                                                                    'IN_TRANSIT',
                                                                    'OUT_FOR_DELIVERY',
                                                                    'DELIVERED'
                                             )
                                         ),

                                 CONSTRAINT chk_shipment_events_sequence
                                     CHECK (sequence_number >= 0),

                                 CONSTRAINT chk_shipment_events_previous_type
                                     CHECK (
                                         (event_type = 'STATUS_CHANGED'
                                             AND previous_status IS NOT NULL)
                                             OR
                                         (event_type IN ('BASELINE', 'CREATED')
                                             AND previous_status IS NULL)
                                         )
);

-- Preserve the current state of shipments created before V2.
-- Their earlier transitions cannot be reconstructed.

INSERT INTO shipment_events (
    id,
    shipment_id,
    event_type,
    previous_status,
    new_status,
    sequence_number,
    occurred_at
)
SELECT
    gen_random_uuid(),
    id,
    'BASELINE',
    NULL,
    status,
    version,
    updated_at
FROM shipments;
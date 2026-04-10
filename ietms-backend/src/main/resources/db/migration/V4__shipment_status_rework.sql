-- shipment: added NEW, TO_LOAD, TO_DROP
ALTER TABLE shipment
    DROP CONSTRAINT shipment_status_check;

ALTER TABLE shipment
    ADD CONSTRAINT shipment_status_check
        CHECK (status IN ('NEW', 'PLANNED', 'TO_LOAD', 'LOADED', 'TO_DROP', 'DROPPED', 'CANCELED'));

-- shipment_timestamp: TO_LOAD і TO_DROP don't write timestamp
ALTER TABLE shipment_timestamp
    DROP CONSTRAINT shipment_timestamp_status_check;

ALTER TABLE shipment_timestamp
    ADD CONSTRAINT shipment_timestamp_status_check
        CHECK (status IN ('NEW', 'PLANNED', 'LOADED', 'DROPPED', 'CANCELED'));

-- migration of existing data:
-- old PLANNED without transportOrder → NEW (initial state)
-- old PLANNED with transportOrder → remain PLANNED (semantically correct)
UPDATE shipment
SET status = 'NEW'
WHERE status = 'PLANNED'
  AND transport_order IS NULL;

-- accordingly timestamp: PLANNED → NEW for those shipments
UPDATE shipment_timestamp st
SET status = 'NEW'
FROM shipment s
WHERE st.shipment_id = s.id
  AND st.status = 'PLANNED'
  AND s.status = 'NEW';
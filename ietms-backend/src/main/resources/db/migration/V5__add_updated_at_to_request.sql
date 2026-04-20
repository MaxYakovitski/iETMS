-- request: add updated_at to track last modification time for sorting
ALTER TABLE requests
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

-- backfill: use issue_date as initial value for existing records
UPDATE requests SET updated_at = issue_date;

ALTER TABLE requests
    ALTER COLUMN updated_at SET NOT NULL;
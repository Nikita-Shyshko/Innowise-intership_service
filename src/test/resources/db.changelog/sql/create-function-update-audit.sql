CREATE OR REPLACE FUNCTION update_audit()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    NEW.version = OLD.version + 1;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION set_initial_audit()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at = CURRENT_TIMESTAMP;
    NEW.updated_at = CURRENT_TIMESTAMP;
    NEW.version = 0;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
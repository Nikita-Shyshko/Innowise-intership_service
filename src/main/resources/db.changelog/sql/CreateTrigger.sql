DROP TRIGGER IF EXISTS trg_users_insert ON users;
CREATE TRIGGER trg_users_insert
    BEFORE INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_initial_audit();

DROP TRIGGER IF EXISTS trg_users_update ON users;
CREATE TRIGGER trg_users_update
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_audit();

DROP TRIGGER IF EXISTS trg_payment_cards_insert ON payment_cards;
CREATE TRIGGER trg_payment_cards_insert
    BEFORE INSERT ON payment_cards
    FOR EACH ROW
    EXECUTE FUNCTION set_initial_audit();

DROP TRIGGER IF EXISTS trg_payment_cards_update ON payment_cards;
CREATE TRIGGER trg_payment_cards_update
    BEFORE UPDATE ON payment_cards
    FOR EACH ROW
    EXECUTE FUNCTION update_audit();
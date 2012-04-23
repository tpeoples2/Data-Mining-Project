CREATE OR REPLACE PROCEDURE CreateAssociationRuleSet(support IN NUMBER, confidence IN NUMBER)
IS
    CURSOR set_cursor IS SELECT SETID FROM LARGESET;
    single_set set_cursor%ROWTYPE;

    CURSOR candidate_cursor IS SELECT ITEMID FROM TEMP_CANDIDATES;
    candidate_item candidate_cursor%ROWTYPE;

    no_of_trans NUMBER;
    no_of_hits_both NUMBER;
    no_of_hits_with_set NUMBER;
    temp_confidence NUMBER;
    temp_support NUMBER;
    duplication_check NUMBER;
BEGIN
    DELETE FROM ASSOCIATIONRULES;
    SELECT (COUNT(DISTINCT TRANSID)) INTO no_of_trans FROM TRANS;
    FOR single_set IN set_cursor LOOP
        DELETE FROM TEMP;
        DELETE FROM TEMP_CANDIDATES;
        INSERT INTO TEMP (SELECT ITEMID FROM LARGESET WHERE SETID = single_set.SETID);
        INSERT INTO TEMP_CANDIDATES (SELECT DISTINCT ITEMID FROM LARGESET MINUS SELECT * FROM TEMP);

        DELETE FROM TEMP_TRANS;
        INSERT INTO TEMP_TRANS (SELECT DISTINCT T1.TRANSID FROM TRANS T1 WHERE NOT EXISTS (SELECT * FROM TEMP MINUS SELECT ITEMID FROM TRANS T2 WHERE T2.TRANSID = T1.TRANSID));
        SELECT COUNT(*) INTO no_of_hits_with_set FROM TEMP_TRANS;
        FOR candidate_item IN candidate_cursor LOOP
            SELECT COUNT(*) INTO no_of_hits_both FROM TEMP_TRANS T1 WHERE NOT EXISTS (SELECT ITEMID FROM ITEMS WHERE ITEMID = candidate_item.ITEMID MINUS SELECT ITEMID FROM TEMP_TRANS, TRANS T2 WHERE T1.TRANSID = T2.TRANSID);

            temp_confidence := 100 * (no_of_hits_both / no_of_hits_with_set);
            temp_support := 100 * (no_of_hits_both / no_of_trans);
           
            SELECT COUNT(*) INTO duplication_check FROM ASSOCIATIONRULES WHERE SETID = single_set.SETID AND ITEMID = candidate_item.ITEMID;

            IF temp_confidence >= confidence AND temp_support >= support AND duplication_check = 0 THEN
                INSERT INTO ASSOCIATIONRULES VALUES (single_set.SETID, candidate_item.ITEMID, round(temp_support, 1), round(temp_confidence, 1));
            END IF;
        END LOOP;
    END LOOP;
END;
/


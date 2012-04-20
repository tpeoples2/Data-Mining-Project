CREATE OR REPLACE PROCEDURE FilterIntoLargeSet(support IN NUMBER)
AS
    CURSOR set_cursor IS SELECT DISTINCT SETID FROM CANDIDATES;
    single_set set_cursor%ROWTYPE;
    CURSOR item_cursor IS SELECT ITEMID FROM TEMP;
    single_item item_cursor%ROWTYPE;
    temp_counter NUMBER;
    single_support NUMBER;
    minus_count NUMBER;
    trans_id_count NUMBER;
    no_of_trans NUMBER;
    no_of_hits NUMBER;
BEGIN
    no_of_hits := 0;
    SELECT COUNT(DISTINCT SETID) + 1 INTO temp_counter FROM LARGESET;
    SELECT (COUNT(DISTINCT TRANSID)) INTO no_of_trans FROM trans;
    FOR single_set IN set_cursor LOOP
        no_of_hits := 0;
        INSERT INTO TEMP (SELECT ITEMID FROM CANDIDATES WHERE SETID = single_set.SETID);
        SELECT COUNT(DISTINCT TRANSID) INTO no_of_hits FROM TRANS T1 WHERE NOT EXISTS (SELECT * FROM TEMP MINUS SELECT ITEMID FROM TRANS WHERE TRANSID = T1.TRANSID);
        single_support := 100 * no_of_hits / no_of_trans;
        IF single_support >= support THEN
            FOR single_item IN item_cursor LOOP
                INSERT INTO LARGESET VALUES (temp_counter, single_item.ITEMID);
            END LOOP;
            temp_counter := temp_counter + 1;
        END IF;
        DELETE FROM TEMP;
    END LOOP;
END;
/

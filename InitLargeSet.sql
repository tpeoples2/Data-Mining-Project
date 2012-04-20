CREATE OR REPLACE PROCEDURE InitLargeSet(support IN NUMBER)
AS
    CURSOR item_cursor IS SELECT ITEMID
                          FROM TRANS
                          WHERE TRANS.ITEMID IN (SELECT DISTINCT ITEMID FROM CANDIDATES)
                          GROUP BY TRANS.ITEMID
                          HAVING COUNT(*) * 100 / (SELECT COUNT(DISTINCT TRANSID) FROM TRANS) >= support;
    single_item item_cursor%ROWTYPE;
    set_size NUMBER;
    fill_counter NUMBER;
    temp_counter NUMBER;
BEGIN
    fill_counter := 0;
    set_size := 1;
    temp_counter := 1;
    FOR single_item IN item_cursor LOOP
        IF fill_counter = set_size THEN
            temp_counter := temp_counter + 1;
            fill_counter := 0;
        END IF;
        INSERT INTO LARGESET VALUES (temp_counter, single_item.ITEMID);
        fill_counter := fill_counter + 1;
    END LOOP;
END;
/


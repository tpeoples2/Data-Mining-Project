SET SERVEROUTPUT ON;

CREATE OR REPLACE PROCEDURE CreateCandidateSet(set_size IN NUMBER)
IS
    CURSOR glue_cursor IS SELECT DISTINCT LSA.SETID AS IDA, LSB.SETID AS IDB
                          FROM LARGESET LSA, LARGESET LSB
                          WHERE LSA.SETID > LSB.SETID AND 
                                set_size = (SELECT COUNT(DISTINCT ITEMID)
                                            FROM LARGESET LSC
                                            WHERE LSC.SETID = LSA.SETID OR
                                                  LSC.SETID = LSB.SETID);
    setid_pair glue_cursor%ROWTYPE;

    CURSOR item_cursor IS SELECT ITEMID FROM TEMP;
    single_item item_cursor%ROWTYPE;

    temp_counter NUMBER;
    fill_counter NUMBER;
    duplicate_check NUMBER;
BEGIN
    fill_counter := 0;
    --SELECT COUNT(DISTINCT SETID) + 1 INTO temp_counter FROM CANDIDATES;
    temp_counter := 1;
    DELETE FROM CANDIDATES;
    FOR setid_pair IN glue_cursor LOOP
        INSERT INTO TEMP (SELECT DISTINCT ITEMID
                          FROM LARGESET LSA
                          WHERE LSA.SETID = setid_pair.IDA OR
                                LSA.SETID = setid_pair.IDB);
        SELECT COUNT(DISTINCT SETID) INTO duplicate_check FROM CANDIDATES C1 WHERE NOT EXISTS (SELECT * FROM TEMP MINUS SELECT ITEMID FROM CANDIDATES C2 WHERE C2.SETID = C1.SETID);
        IF duplicate_check = 0 THEN
            FOR single_item IN item_cursor LOOP
                IF fill_counter = set_size THEN
                    fill_counter := 0;
                    temp_counter := temp_counter + 1;
                END IF;
                INSERT INTO CANDIDATES VALUES (temp_counter, single_item.ITEMID);
                fill_counter := fill_counter + 1;
            END LOOP;
        END IF;
        DELETE FROM TEMP;
    END LOOP;
END;
/
 

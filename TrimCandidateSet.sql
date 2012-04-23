CREATE OR REPLACE PROCEDURE TrimCandidateSet(previous_size IN NUMBER)
AS 
    count_check NUMBER;
    CURSOR curs IS SELECT SETID FROM CANDIDATES;
    curs_row curs%ROWTYPE;
BEGIN
    FOR curs_row IN curs LOOP
        INSERT INTO TEMP (SELECT ITEMID 
                          FROM CANDIDATES 
                          WHERE SETID = curs_row.SETID);
        SELECT COUNT(*) INTO count_check FROM (SELECT LARGESET.SETID
                                               FROM TEMP, LARGESET
                                               WHERE TEMP.ITEMID = LARGESET.ITEMID
                                               GROUP BY LARGESET.SETID
                                               HAVING COUNT(*) = previous_size);
        IF count_check <> previous_size + 1 THEN
            DELETE FROM CANDIDATES WHERE SETID = curs_row.SETID; 
        END IF;
        DELETE FROM TEMP;
    END LOOP;
END;
/


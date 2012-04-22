CREATE TABLE CANDIDATES (
    SETID NUMBER,
    ITEMID NUMBER,
    PRIMARY KEY(SETID, ITEMID)
);

CREATE TABLE LARGESET (
    SETID NUMBER,
    ITEMID NUMBER,
    SUPPORT NUMBER,
    PRIMARY KEY(SETID, ITEMID)
);

CREATE TABLE TEMP (
    ITEMID NUMBER,
    PRIMARY KEY(ITEMID)
);

CREATE TABLE TEMP_CANDIDATES (
    ITEMID NUMBER,
    PRIMARY KEY(ITEMID)
);

CREATE TABLE TEMP_TRANS (
    TRANSID NUMBER,
    PRIMARY KEY(TRANSID)
);

CREATE TABLE ASSOCIATIONRULES (
    SETID NUMBER,
    ITEMID NUMBER,
    SUPPORT NUMBER,
    CONFIDENCE NUMBER,
    PRIMARY KEY(SETID, ITEMID)
);

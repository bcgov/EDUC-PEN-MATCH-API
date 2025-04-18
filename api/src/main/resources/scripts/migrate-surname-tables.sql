CREATE TABLE FOREIGN_SURNAME
as
SELECT
    sys_guid() AS FOREIGN_SURNAME_ID,
    TRIM(fs.SURNAME) AS SURNAME,
    TRIM(fs.ANCESTRY) AS ANCESTRY,
    fs.EFFECTIVE_DATE AS EFFECTIVE_DATE,
    fs.EXPIRY_DATE AS EXPIRY_DATE,
    fs.CREATE_DATE AS CREATE_DATE,
    fs.UPDATE_DATE AS UPDATE_DATE,
    TRIM(fs.CREATE_USER_NAME) AS CREATE_USER,
    TRIM(fs.UPDATE_USER_NAME) AS UPDATE_USER
FROM FOREIGN_SURNAMES fs;

CREATE TABLE NICKNAME
as
SELECT
    sys_guid() AS NICKNAME_ID,
    TRIM(nnm.NICKNAME1) AS NICKNAME_1,
    TRIM(nnm.NICKNAME2) AS NICKNAME_2,
    sysdate AS CREATE_DATE,
    sysdate AS UPDATE_DATE,
    'PEN_MATCH_MIGRATION' AS CREATE_USER,
    'PEN_MATCH_MIGRATION' AS UPDATE_USER
FROM NICKNAMES nnm;

CREATE TABLE FREQUENCY_SURNAME
as
SELECT
    sys_guid() AS FREQUENCY_SURNAME_ID,
    TRIM(sf.SURNAME) AS SURNAME,
    sf.SURNAME_FREQUENCY AS SURNAME_FREQUENCY,
    sysdate AS CREATE_DATE,
    sysdate AS UPDATE_DATE,
    'PEN_MATCH_MIGRATION' AS CREATE_USER,
    'PEN_MATCH_MIGRATION' AS UPDATE_USER
FROM SURNAME_FREQUENCY sf;

--Create match reason code table
CREATE TABLE MATCH_REASON_CODE
(
    MATCH_REASON_CODE VARCHAR2(10)           NOT NULL,
    LABEL             VARCHAR2(30),
    DESCRIPTION       VARCHAR2(255),
    DISPLAY_ORDER     NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE    DATE                   NOT NULL,
    EXPIRY_DATE       DATE                   NOT NULL,
    CREATE_USER       VARCHAR2(32)           NOT NULL,
    CREATE_DATE       DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER       VARCHAR2(32)           NOT NULL,
    UPDATE_DATE       DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT STUDENT_MATCH_REASON_CODE_PK PRIMARY KEY (MATCH_REASON_CODE)
);
COMMENT ON TABLE MATCH_REASON_CODE IS 'possible match reason code lists the standard codes for the reason why the two student records were marked as matches.';

-- Student grade codes
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('PENMATCH', 'Twinned by Matching PEN',
        'A PEN Request was matched to an existing Student record, so that Student record was twinned with the other other possible matches found by the PEN Match algorithm',
        1, to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('PENCREATE', 'Twinned by Creating PEN',
        'A PEN Request filled by creating a new Student record, so that new Student was twinned with the other other possible matches found by the PEN Match algorithm',
        2, to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('DEMERGE', 'Twinned after Demerge', 'Two previously merged Student records were demerged and twinned', 3,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('SPLIT', 'Twinned after Split', 'One Student record was split into two and the two were twinned', 4,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('AU', 'Legacy AU twinned by system',
        'Legacy AU code, meaning twinned by system, likely while completing a PEN Request', 5,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('SR', 'Slow Duplicate Report', 'Slow Duplicate Report', 6, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('DR', 'PEN Master Duplicate Report', 'PEN Master Duplicate Report', 7, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('MI', 'Ministry Identified', 'Ministry Identified', 8, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('SC', 'School Identified', 'School Identified', 9, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('DM', 'Twinned during de-merge', 'Twinned during de-merge', 10, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('IF', 'Iffy Twin', 'Iffy Twin', 11, to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'),
        'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('FR', 'Fast Duplicate Report', 'Fast Duplicate Report', 12, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('TX', 'Trax Identified', 'Trax Identified', 13, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('SD', 'SLD Duplicate Report', 'SLD Duplicate Report', 14, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('VT', 'Verified Twin', 'Verified Twin', 15, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG',
        to_date('2019-08-14', 'YYYY-MM-DD'));
INSERT INTO MATCH_REASON_CODE (MATCH_REASON_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE,
                               CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('DE', 'De-merged', 'De-merged', 16, to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'),
        'IDIR/MINYANG', to_date('2020-08-14', 'YYYY-MM-DD'), 'IDIR/MINYANG', to_date('2019-08-14', 'YYYY-MM-DD'));


--Create possible match table
CREATE TABLE POSSIBLE_MATCH
(
    POSSIBLE_MATCH_ID  RAW(16)              NOT NULL,
    STUDENT_ID         RAW(16)              NOT NULL,
    MATCHED_STUDENT_ID RAW(16)              NOT NULL,
    MATCH_REASON_CODE  VARCHAR2(10)         NOT NULL,
    CREATE_USER        VARCHAR2(32)         NOT NULL,
    CREATE_DATE        DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER        VARCHAR2(32)         NOT NULL,
    UPDATE_DATE        DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT POSSIBLE_MATCH_PK PRIMARY KEY (POSSIBLE_MATCH_ID)
);
COMMENT ON TABLE POSSIBLE_MATCH IS 'Contains the IDs of Student records that were matched together, which means that, although the records have some similarity, they are believed to be different Students. The relationship is symmetric. matching occurs as a result of users manually completing PEN Requests.';
-- Column Comments
COMMENT ON COLUMN POSSIBLE_MATCH.POSSIBLE_MATCH_ID IS 'Unique surrogate key for each record. GUID value must be provided during insert.';
COMMENT ON COLUMN POSSIBLE_MATCH.STUDENT_ID IS 'Soft Foreign key to the Student record that has this twin record.';
COMMENT ON COLUMN POSSIBLE_MATCH.MATCHED_STUDENT_ID IS 'Soft Foreign key to the Student record that was selected or created during PEN Request manual processing. It could be also found by the PEN Match system algorithm to be a possible match for the PEN Request.';
COMMENT ON COLUMN POSSIBLE_MATCH.MATCH_REASON_CODE IS 'Code specifying the reason why the two student records were marked as matches.';


ALTER TABLE POSSIBLE_MATCH
    ADD CONSTRAINT MATCH_REASON_CODE_FK FOREIGN KEY (MATCH_REASON_CODE) REFERENCES MATCH_REASON_CODE (MATCH_REASON_CODE);

ALTER TABLE POSSIBLE_MATCH
    ADD CONSTRAINT STUDENT_ID_MATCHED_STUDENT_ID_UK UNIQUE (STUDENT_ID, MATCHED_STUDENT_ID);

CREATE INDEX STUDENT_ID_IDX ON POSSIBLE_MATCH (STUDENT_ID);
CREATE INDEX MATCHED_STUDENT_ID_IDX ON POSSIBLE_MATCH (MATCHED_STUDENT_ID);
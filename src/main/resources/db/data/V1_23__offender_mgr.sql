INSERT INTO OFFENDER (OFFENDER_ID, FIRST_NAME, CRN, SECOND_NAME, SURNAME, DATE_OF_BIRTH_DATE, PARTITION_AREA_ID, SOFT_DELETED, GENDER_ID, FIRST_NAME_SOUNDEX, SURNAME_SOUNDEX, CREATED_DATETIME, LAST_UPDATED_DATETIME, CREATED_BY_USER_ID, LAST_UPDATED_USER_ID, LAST_UPDATED_USER_ID_DIVERSITY, PENDING_TRANSFER)
VALUES (40, 'KEN', 'CRN40', 'ROACH', 'BARLOW', to_date('19-JUL-65','DD-MON-RR'), 1, 0, 1, ' ', ' ', to_date('13-DEC-18','DD-MON-RR'), to_date('13-DEC-18','DD-MON-RR'), 1, 1, 2500040507, 0);

INSERT INTO OFFENDER_MANAGER (OFFENDER_MANAGER_ID, ALLOCATION_DATE, END_DATE, ALLOCATION_STAFF_ID, TEAM_ID, PARTITION_AREA_ID,OFFENDER_ID,
                              SOFT_DELETED,ROW_VERSION,ALLOCATION_REASON_ID,PROVIDER_EMPLOYEE_ID, OFFENDER_TRANSFER_ID, PROVIDER_TEAM_ID,
                              TRAINING_SESSION_ID, TRUST_PROVIDER_FLAG,STAFF_EMPLOYEE_ID,PROBATION_AREA_ID,TRUST_PROVIDER_TEAM_ID,ACTIVE_FLAG,
                              CREATED_BY_USER_ID, CREATED_DATETIME,LAST_UPDATED_USER_ID,LAST_UPDATED_DATETIME)
VALUES (40, to_date('04-MAY-18','DD-MON-RR'), null, 17, 100, 1, 40, 0, 1, 1, null, null, null, 1, 0, 11, 11, 1, 1, 1, to_date('13-DEC-18','DD-MON-RR'), 1, to_date('13-DEC-18','DD-MON-RR'));

INSERT INTO PRISON_OFFENDER_MANAGER (PRISON_OFFENDER_MANAGER_ID,OFFENDER_ID,PROBATION_AREA_ID,ALLOCATION_TEAM_ID, ALLOCATION_STAFF_ID, ALLOCATION_REASON_ID, ALLOCATION_DATE, ACTIVE_FLAG, SOFT_DELETED, CREATED_DATETIME, CREATED_BY_USER_ID,LAST_UPDATED_DATETIME,LAST_UPDATED_USER_ID)
VALUES (12, 40, 12, 101, 101, 1, to_date('01-JAN-19','DD-MON-RR'), 1, 0, to_date('13-DEC-18','DD-MON-RR'), 1, to_date('13-DEC-18','DD-MON-RR'), 1);

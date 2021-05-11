
Insert into CONTACT (CONTACT_ID,
                     CONTACT_DATE,
                     OFFENDER_ID,
                     NOTES,
                     VISOR_CONTACT,
                     STAFF_ID,
                     TEAM_ID,
                     SOFT_DELETED,
                     VISOR_EXPORTED,
                     PARTITION_AREA_ID,
                     OFFICE_LOCATION_ID,
                     ROW_VERSION,
                     ALERT_ACTIVE,
                     ATTENDED,
                     CREATED_DATETIME,
                     COMPLIED,
                     LAST_UPDATED_DATETIME,
                     EVENT_ID,
                     CONTACT_TYPE_ID,
                     PROVIDER_TEAM_ID,
                     CONTACT_OUTCOME_TYPE_ID,
                     CREATED_BY_USER_ID,
                     LAST_UPDATED_USER_ID,
                     TRUST_PROVIDER_FLAG,
                     STAFF_EMPLOYEE_ID,
                     PROBATION_AREA_ID,
                     TRUST_PROVIDER_TEAM_ID,
                     ENFORCEMENT)
values (2502719240,
        to_date('04-09-20','DD-MM-RR'),
        2500343964,
        'The notes field',
        'N', -- VISOR_CONTACT
        2500038545, -- STAFF_ID
        2500031218, -- TEAM_ID
        0, -- SOFT_DELETED
        'N', -- VISOR_EXPORTED
        0, -- PARTITION_AREA_ID
        null, -- OFFICE_LOCATION_ID
        3, -- ROW_VERSION
        null, -- ALERT_ACTIVE
        'Y', -- ATTENDED
        to_date('13-09-19','DD-MM-RR'),
        null,
        to_date('13-09-19','DD-MM-RR'), -- LAST_UPDATED_DATETIME
        2500295343, -- EVENT_ID
        1217, -- CONTACT_TYPE_ID
        null, -- PROVIDER_TEAM_ID
        19, -- CONTACT_OUTCOME_TYPE_ID
        2500040507, -- CREATED_BY_USER_ID
        2500040507, -- LAST_UPDATED_USER_ID
        0, -- TRUST_PROVIDER_FLAG
        2500038545, -- STAFF_EMPLOYEE_ID
        1500001001, -- PROBATION_AREA_ID
        2500031218, -- TRUST_PROVIDER_TEAM_ID
        null -- ENFORCEMENT
        );


Insert into CONTACT (CONTACT_ID,
                     CONTACT_DATE,
                     OFFENDER_ID,
                     NOTES,
                     VISOR_CONTACT,
                     STAFF_ID,
                     TEAM_ID,
                     SOFT_DELETED,
                     VISOR_EXPORTED,
                     PARTITION_AREA_ID,
                     ROW_VERSION,
                     ATTENDED,
                     CREATED_DATETIME,
                     COMPLIED,
                     LAST_UPDATED_DATETIME,
                     EVENT_ID,
                     CONTACT_TYPE_ID,
                     CONTACT_OUTCOME_TYPE_ID,
                     CREATED_BY_USER_ID,
                     LAST_UPDATED_USER_ID,
                     TRUST_PROVIDER_FLAG,
                     STAFF_EMPLOYEE_ID,
                     PROBATION_AREA_ID,
                     TRUST_PROVIDER_TEAM_ID,
                     ENFORCEMENT)
values (2502719241,
        to_date('04-09-20','DD-MM-RR'),
        2600343964,
        'The notes field',
        'N', -- VISOR_CONTACT
        2500038545, -- STAFF_ID
        2500031218, -- TEAM_ID
        0, -- SOFT_DELETED
        'N', -- VISOR_EXPORTED
        0, -- PARTITION_AREA_ID
        1, -- ROW_VERSION
        null, -- ATTENDED
        to_date('13-09-19','DD-MM-RR'),
        null,
        to_date('13-09-19','DD-MM-RR'), -- LAST_UPDATED_DATETIME
        2600295124, -- EVENT_ID
        1217, -- CONTACT_TYPE_ID
        null, -- CONTACT_OUTCOME_TYPE_ID
        2500040507, -- CREATED_BY_USER_ID
        2500040507, -- LAST_UPDATED_USER_ID
        0, -- TRUST_PROVIDER_FLAG
        2500038545, -- STAFF_EMPLOYEE_ID
        1500001001, -- PROBATION_AREA_ID
        2500031218, -- TRUST_PROVIDER_TEAM_ID
        null -- ENFORCEMENT
       );



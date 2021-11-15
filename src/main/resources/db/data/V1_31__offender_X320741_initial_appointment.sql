INSERT INTO R_CONTACT_TYPE (CONTACT_TYPE_ID,CODE,DESCRIPTION,SHORT_DESCRIPTION,SELECTABLE,NATIONAL_STANDARDS_CONTACT,ATTENDANCE_CONTACT,
RECORDED_HOURS_CREDITED,SENSITIVE_CONTACT,OFFENDER_LEVEL_CONTACT,ROW_VERSION,CREATED_DATETIME,APPEARS_IN_LIST_OF_CONTACTS,SMS_MESSAGE_TEXT,
OFFENDER_EVENT_0,LEGACY_ORDERS,LAST_UPDATED_DATETIME,CJA_ORDERS,DPA_EXCLUDE,TRAINING_SESSION_ID,CONTACT_OUTCOME_FLAG,CONTACT_LOCATION_FLAG,
CREATED_BY_USER_ID,CONTACT_ALERT_FLAG,LAST_UPDATED_USER_ID,FUTURE_SCHEDULED_CONTACTS_FLAG,CONTACT_TYPE_ICON_ID,EDITABLE,DEFAULT_HEADINGS,
PSS_RQMNT,RAR_ACTIVITY,SPG_OVERRIDE,NOMIS_CONTACT_TYPE,SPG_INTEREST,SGC_FLAG)
VALUES (2500038043,'COAI','Initial Appointment (NS)',null,'Y','N','N',
'N','N','N',1,to_date('16-OCT-19','DD-MON-RR'),'N','N',
'Y','Y',to_date('16-OCT-19','DD-MON-RR'),'Y',null,null,'Y','Y',
2500003526,'N',2500003526,'N',null,'Y',null,
null,'N',0,null,0,1);

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
values (2503719240,
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
        2500038043, -- CONTACT_TYPE_ID
        null, -- PROVIDER_TEAM_ID
        19, -- CONTACT_OUTCOME_TYPE_ID
        2500040507, -- CREATED_BY_USER_ID
        2500040507, -- LAST_UPDATED_USER_ID
        0, -- TRUST_PROVIDER_FLAG
        2500038545, -- STAFF_EMPLOYEE_ID
        1500001001, -- PROBATION_AREA_ID
        2500031218, -- TRUST_PROVIDER_TEAM_ID
        null -- ENFORCEMENT
        ),
        (2503719241,
                to_date('04-11-21','DD-MM-RR'),
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
                2500038043, -- CONTACT_TYPE_ID
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
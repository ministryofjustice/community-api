Insert into EVENT (EVENT_ID, OFFENDER_ID, EVENT_NUMBER,
                   REFERRAL_DATE, SOFT_DELETED, PARTITION_AREA_ID, ROW_VERSION, CREATED_BY_USER_ID,
                   CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, ORGANISATIONS,
                   IN_BREACH, ACTIVE_FLAG, BREACH_END, CPS_DATE, CPS_DOCUMENT, CPS_DOCUMENT_NAME, FTC_COUNT,
                   PENDING_TRANSFER, CONVICTION_DATE, FIRST_RELEASE_DATE, PSS_RQMNT_FLAG, CPS_LAST_UPDATED_USER_ID,
                   CPS_LAST_UPDATED_AUTH_PROV_ID, CPS_CREATED_PROVIDER_ID, CPS_CREATED_BY_USER_ID, CPS_CREATED_DATETIME,
                   CPS_ALFRESCO_DOCUMENT_ID, CPS_SOFT_DELETED, COURT_ID)
values (140, 40, '1', to_date('04-09-18', 'DD-MM-RR'), 0, 0, 1, 2500040507,
        to_date('04-09-19', 'DD-MM-RR'), 2500040507, to_date('04-09-19', 'DD-MM-RR'),
        ('2600007020', '1500001001'), 0, 1, null, to_date('02-09-19', 'DD-MM-RR'), null, null, 0, 0,
        to_date('03-09-19', 'DD-MM-RR'), null, 0, null, null, null, null,
        to_date('04-09-19', 'DD-MM-RR'), null, 0, 1500004905);


Insert into DISPOSAL (DISPOSAL_ID, DISPOSAL_DATE, LENGTH, PUNISHMENT, REDUCTION_OF_CRIME,
                      REFORM_AND_REHABILITION, PUBLIC_PROTECTION, REPARATION, RECOMMENDATION_NOT_STATED,
                      TERMINATION_DATE, EVENT_ID, PARTITION_AREA_ID, SOFT_DELETED, ROW_VERSION,
                      LEVEL_OF_SERIOUSNESS_ID, DISPOSAL_TYPE_ID, CREATED_DATETIME, CREATED_BY_USER_ID,
                      LAST_UPDATED_DATETIME, LAST_UPDATED_USER_ID,
                      LENGTH_2, OFFENDER_ID, ORGANISATIONS, ACTIVE_FLAG, UPW, LENGTH_IN_DAYS,
                      ENTRY_LENGTH_UNITS_ID, ENTRY_LENGTH_2_UNITS_ID, NOTIONAL_END_DATE, ENTRY_LENGTH,
                      USER_TERMINATION_DATE, ENTERED_NOTIONAL_END_DATE)
values (140, to_date('03-NOV-19', 'DD-MON-RR'), 11, 'N', 'N', 'N', 'N', 'N', 'N', to_date('03-NOV-20', 'DD-MON-RR'), 140,
        0, 0, 1, 516, 41, to_date('04-09-19', 'DD-MM-RR'), 2500040507, to_date('04-09-19', 'DD-MM-RR'),
        2500040507, 5, 2600343964, ('2600007020', '1500001001'), 0, 0, 1826, 1111, 1111,
        to_date('03-09-24', 'DD-MM-RR'), 5, null, null);

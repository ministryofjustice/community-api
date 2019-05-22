create table PRISON_OFFENDER_MANAGER
(
    PRISON_OFFENDER_MANAGER_ID NUMBER not null
        constraint XPKPRISON_OFFENDER_MANAGER
            primary key,
    OFFENDER_ID NUMBER not null
        constraint R_1197
            references OFFENDER,
    PROBATION_AREA_ID NUMBER not null
        constraint R_1198
            references PROBATION_AREA,
    ALLOCATION_TEAM_ID NUMBER not null
        constraint R_1199
            references TEAM,
    ALLOCATION_STAFF_ID NUMBER not null
        constraint R_1200
            references STAFF,
    ALLOCATION_REASON_ID NUMBER not null
        constraint R_1201
            references R_STANDARD_REFERENCE_LIST,
    TRANSFER_REASON_ID NUMBER,
--        constraint R_1202
--            references R_TRANSFER_REASON,
    ALLOCATION_DATE DATE not null,
    END_DATE DATE,
    ACTIVE_FLAG NUMBER not null
        constraint TRUE_OR_FALSE_1484563887
            check (ACTIVE_FLAG IN (0, 1)),
    SOFT_DELETED NUMBER not null
        constraint TRUE_OR_FALSE_1988641336
            check (SOFT_DELETED IN (0, 1)),
    ROW_VERSION NUMBER default 0 not null,
    CREATED_DATETIME DATE not null,
    CREATED_BY_USER_ID NUMBER not null,
    LAST_UPDATED_DATETIME DATE not null,
    LAST_UPDATED_USER_ID NUMBER not null,
    constraint ACTIVE_FLAG_END_DATE_15186317
        check ((ACTIVE_FLAG = 0 AND END_DATE IS NOT NULL)
            OR (ACTIVE_FLAG = 1 AND END_DATE IS NULL))

);

create table RESPONSIBLE_OFFICER
(
    RESPONSIBLE_OFFICER_ID NUMBER not null
        constraint XPKRESPONSIBLE_OFFICER
            primary key,
    OFFENDER_ID NUMBER not null
        constraint R_1204
            references OFFENDER,
    OFFENDER_MANAGER_ID NUMBER
        constraint R_1205
            references OFFENDER_MANAGER,
    PRISON_OFFENDER_MANAGER_ID NUMBER
        constraint R_1206
            references PRISON_OFFENDER_MANAGER,
    START_DATE DATE not null,
    END_DATE DATE,
    CREATED_BY_USER_ID NUMBER not null,
    CREATED_DATETIME DATE not null,
    LAST_UPDATED_USER_ID NUMBER not null,
    LAST_UPDATED_DATETIME DATE not null,
    ROW_VERSION NUMBER default 0 not null
);


Insert into TEAM (TEAM_ID, CODE, DESCRIPTION, DISTRICT_ID, LOCAL_DELIVERY_UNIT_ID, TELEPHONE, UNPAID_WORK_TEAM, ROW_VERSION,
FAX_NUMBER, CONTACT_NAME, START_DATE, END_DATE, CREATED_DATETIME, CREATED_BY_USER_ID, LAST_UPDATED_DATETIME, LAST_UPDATED_USER_ID,
TRAINING_SESSION_ID , PROBATION_AREA_ID, PRIVATE, SC_PROVIDER_ID , DESIGNATED_TRANSFER_TEAM )
values (2500034433,'ESXUTS','Unallocated',100,100,null,'Y',5,null,null,to_date('16-OCT-19','DD-MON-RR'),to_date('16-OCT-19','DD-MON-RR'),to_date('16-OCT-19','DD-MON-RR'),1,to_date('16-OCT-19','DD-MON-RR'),1,null,1,0,null,0);

Insert into STAFF (STAFF_ID, START_DATE, SURNAME, END_DATE, FORENAME, ROW_VERSION, FORENAME2, STAFF_GRADE_ID, TITLE_ID,
OFFICER_CODE, CREATED_BY_USER_ID, LAST_UPDATED_USER_ID, CREATED_DATETIME, LAST_UPDATED_DATETIME, TRAINING_SESSION_ID,
PRIVATE, SC_PROVIDER_ID, PROBATION_AREA_ID)
values (2500037453,to_date('16-OCT-19','DD-MON-RR'),'ESXUTSO',to_date('16-OCT-19','DD-MON-RR'),'ESXUTSO',1,'ESXUTSO',null,null,
'ESXUTSO',1,1,to_date('16-OCT-19','DD-MON-RR'),to_date('16-OCT-19','DD-MON-RR'),null,
0,null,1);
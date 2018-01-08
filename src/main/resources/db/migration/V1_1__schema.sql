create table USER_
(
  USER_ID NUMBER not null
    constraint XPKUSER_
    primary key,
  SURNAME VARCHAR2(35) not null,
  FORENAME VARCHAR2(35) not null,
  DISTINGUISHED_NAME VARCHAR2(500) not null
);

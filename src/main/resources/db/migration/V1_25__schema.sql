
-- Additional columns added to the offender table
ALTER TABLE OFFENDER
    ADD (
        IOM_NOMINAL CHAR(1),
        SAFEGUARDING_ISSUE CHAR(1),
        VULNERABILITY_ISSUE CHAR(1),
        DISABILITY CHAR(1),
        LEARNING CHAR(1),
        MENTAL_HEALTH CHAR(1)
     );
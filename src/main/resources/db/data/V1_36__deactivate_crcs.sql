update probation_area set end_date = to_date('2021-10-01 00:00:00', 'yyyy-mm-dd hh24:mi:ss')
                      where description like 'CPA %';
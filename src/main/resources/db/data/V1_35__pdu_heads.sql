create table r_level_2_head_of_level_2
(
    borough_id  INTEGER           not null,
    staff_id    INTEGER           not null,
    row_version INTEGER default 0 not null,
    constraint xpkr_level_2_head_of_level_2 primary key (borough_id, staff_id)
);

alter table r_level_2_head_of_level_2
    add (constraint r_1365 foreign key (borough_id) references borough (borough_id));

alter table r_level_2_head_of_level_2
    add (constraint r_1366 foreign key (staff_id) references staff (staff_id));


-- TODO add insert statements for some test data to use in the integration tests
-- insert into r_level_2_head_of_level_2 (borough_id, staff_id, row_version)
-- values (1, 2, 0);
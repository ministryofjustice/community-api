create table r_level_2_head_of_level_2
(
    borough_id  NUMBER           not null,
    staff_id    NUMBER           not null,
    row_version NUMBER default 0 not null,
    constraint xpkr_level_2_head_of_level_2 primary key (borough_id, staff_id)
);

alter table r_level_2_head_of_level_2
    add (constraint r_1365 foreign key (borough_id) references borough (borough_id));

alter table r_level_2_head_of_level_2
    add (constraint r_1366 foreign key (staff_id) references staff (staff_id));


insert into r_level_2_head_of_level_2 (borough_id, staff_id, row_version) values (1500028505, 2500002956, 1);
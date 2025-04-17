create table "user"
(
    id         uuid primary key,
    username   varchar(64)  not null,
    password   varchar(128) not null,
    first_name varchar(64)  not null,
    last_name  varchar(64)  not null,
    created_at timestamp    not null,

    constraint uq_user__username unique (username)
);
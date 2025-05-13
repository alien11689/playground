create table access_token
(
    token      uuid primary key,
    user_id    uuid      not null,
    expires_at timestamp not null,

    constraint fk_user_token__user foreign key (user_id) references "user" (id)
);
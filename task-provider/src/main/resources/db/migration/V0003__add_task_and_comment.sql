create table task
(
    id              uuid primary key,
    summary         varchar(255) not null,
    description     text,
    created_at      timestamp    not null,
    created_by      uuid         not null,
    status          varchar(16)  not null,
    last_updated_at timestamp    not null,
    last_updated_by uuid         not null,
    assigned_to     uuid,

    constraint fk_task__created_by foreign key (created_by) references "user" (id),
    constraint fk_task__last_updated_by foreign key (last_updated_by) references "user" (id),
    constraint fk_task__assigned_to foreign key (assigned_to) references "user" (id)
);

create table task_comment
(
    id         uuid primary key,
    task_id    uuid      not null,
    created_at timestamp not null,
    created_by uuid      not null,
    updated_at timestamp not null,
    comment    text      not null,

    constraint fk_task_comment__task foreign key (task_id) references task (id),
    constraint fk_task_comment__created_by foreign key (created_by) references "user" (id)
);
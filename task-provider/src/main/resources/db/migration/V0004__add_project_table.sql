create table project
(
    id          uuid      not null primary key,
    name        varchar(255) not null,
    description text,
    status      varchar(16) not null,
    created_at  timestamp not null,
    updated_at  timestamp not null
);

create unique index idx_project_name on project(name);
create index idx_project_status on project(status);

alter table task
    add column project_id uuid;

alter table task
    add constraint fk_task__project foreign key (project_id) references project (id);

insert into project (id, name, description, status, created_at, updated_at)
values ('00000000-0000-0000-0000-000000000001', 'Default Project', 'Default project for existing tasks', 'ACTIVE', now(), now())
on conflict do nothing;

update task
set project_id = '00000000-0000-0000-0000-000000000001'
where project_id is null;

alter table task
    alter column project_id set not null;

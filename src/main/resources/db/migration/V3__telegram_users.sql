create table public.telegram_users
(
    id          uuid        not null,
    external_id int8        not null,
    chat_id     int8        not null,
    last_name   varchar(255),
    first_name  varchar(255),
    status      varchar(32) not null,
    constraint telegram_users_pk primary key (id)
);
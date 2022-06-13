create table public.operations
(
    id            uuid,
    instrument_id uuid,
    type          varchar(64)    not null,
    date          timestamp      not null,
    count         int8           not null,
    payment       decimal(11, 4) not null,
    currency      varchar(8)     not null,
    external_id   varchar(32)    not null,
    constraint operations_pk primary key (id),
    constraint instrument_id_instruments_id foreign key (instrument_id) references public.instruments (id) on delete restrict on update restrict
);
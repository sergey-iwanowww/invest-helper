create table public.trading_days
(
    id          uuid,
    exchange    varchar(32) not null,
    date        date        not null,
    trading_day boolean     not null,
    start_date  timestamp   not null,
    end_date    timestamp   not null,
    constraint trading_days_pk primary key (id)
);
create table public.positions
(
    id              uuid,
    instrument_id   uuid           not null,
    portfolio_id    uuid           not null,
    balance_count   int8           not null,
    result          decimal(11, 4) not null,
    commission      decimal(11, 4) not null,
    balance_average decimal(11, 4) not null,
    constraint positions_pk primary key (id),
    constraint instrument_id_instruments_id foreign key (instrument_id) references public.instruments (id) on delete restrict on update restrict,
    constraint portfolio_id_portfolios_id foreign key (portfolio_id) references public.portfolios (id) on delete restrict on update restrict
);
create table public.candles
(
    id uuid not null,
    instrument_id uuid not null,
    time_frame varchar(32) not null,
    open_date timestamp not null,
    close_date timestamp not null,
    min decimal(11, 4) not null,
    max decimal(11, 4) not null,
    open decimal(11, 4) not null,
    close decimal(11, 4) not null,
    volume bigint,
    complete boolean not null,
    constraint candles_instrument_id_instruments_id foreign key (instrument_id) references public.instruments(id) on update restrict on delete restrict
);
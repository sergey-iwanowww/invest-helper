create table public.authors
(
    id   uuid                   not null,
    name character varying(512) not null
);

create table public.idea_tag_links
(
    idea_id uuid not null,
    tag_id  uuid not null
);

create table public.idea_triggers
(
    id          uuid                  not null,
    date        timestamp without time zone,
    price       numeric(11, 4),
    with_retest boolean default false,
    type        character varying(32) not null
);

create table public.ideas
(
    id                uuid                  not null,
    instrument_id     uuid,
    source_id         uuid                  not null,
    text              character varying,
    image_path        character varying(512),
    start_trigger_id  uuid                  not null,
    finish_trigger_id uuid,
    concept_type      character varying(32) not null,
    generated_date    timestamp without time zone not null,
    author_id         uuid                  not null,
    created_date      timestamp without time zone not null,
    deleted_date      timestamp without time zone,
    started_date      timestamp without time zone,
    started_price     numeric(11, 4),
    finished_date     timestamp without time zone,
    finished_price    numeric(11, 4)
);

create table public.instruments
(
    id            uuid                  not null,
    type          character varying(16) not null,
    ticker        character varying(32) not null,
    name          character varying(128),
    market        character varying(16) not null,
    trading_mode  character varying(4)  not null,
    currency_code character varying(8)  not null,
    figi          character varying(32) not null,
    sector        character varying(32) not null
);

create table public.portfolios
(
    id     uuid                   not null,
    name   character varying(256) not null,
    broker character varying(32)  not null
);

create table public.source_author_links
(
    source_id uuid not null,
    author_id uuid not null
);

create table public.sources
(
    id      uuid                   not null,
    type    character varying(32)  not null,
    name    character varying(512) not null,
    address character varying(512) not null
);

create table public.tags
(
    id   uuid                   not null,
    name character varying(255) not null
);

alter table only public.authors
    add constraint authors_pk primary key (id);

alter table only public.idea_triggers
    add constraint idea_triggers_pk primary key (id);

alter table only public.ideas
    add constraint ideas_pk primary key (id);

alter table only public.instruments
    add constraint instruments_pk primary key (id);

alter table only public.portfolios
    add constraint portfolios_pk primary key (id);

alter table only public.source_author_links
    add constraint source_author_links_pk primary key (source_id, author_id);

alter table only public.sources
    add constraint sources_pk primary key (id);

alter table only public.tags
    add constraint tags_pk primary key (id);

alter table only public.idea_tag_links
    add constraint idea_tag_links_idea_id_ideas_id_fk foreign key (idea_id) references public.ideas(id) on update restrict on delete restrict;

alter table only public.idea_tag_links
    add constraint idea_tag_links_tag_id_tags_id_fk foreign key (tag_id) references public.tags(id) on update restrict on delete restrict;

alter table only public.ideas
    add constraint ideas_author_id_authors_id_fk foreign key (author_id) references public.authors(id) on update restrict on delete restrict;

alter table only public.ideas
    add constraint ideas_finish_trigger_id_idea_triggers_id_fk foreign key (finish_trigger_id) references public.idea_triggers(id) on update restrict on delete restrict;

alter table only public.ideas
    add constraint ideas_instrument_id_instruments_id foreign key (instrument_id) references public.instruments(id) on update restrict on delete restrict;

alter table only public.ideas
    add constraint ideas_source_id_sources_id foreign key (source_id) references public.sources(id) on update restrict on delete restrict;

alter table only public.ideas
    add constraint ideas_start_trigger_id_idea_triggers_id_fk foreign key (start_trigger_id) references public.idea_triggers(id) on update restrict on delete restrict;

alter table only public.source_author_links
    add constraint source_author_links_author_id_authors_id_fk foreign key (author_id) references public.authors(id) on update restrict on delete restrict;

alter table only public.source_author_links
    add constraint source_author_links_source_id_sources_id_fk foreign key (source_id) references public.sources(id) on update restrict on delete restrict;

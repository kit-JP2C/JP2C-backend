DELETE from accounts;

alter table accounts add column if not exists email varchar(255) not null unique;
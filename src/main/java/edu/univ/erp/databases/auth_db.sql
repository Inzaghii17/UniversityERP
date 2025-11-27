

create table users
(
    user_id       int auto_increment
        primary key,
    username      varchar(80)                             not null,
    role          enum ('STUDENT', 'INSTRUCTOR', 'ADMIN') not null,
    password_hash varchar(255)                            not null,
    status        varchar(20) default 'active'            null,
    last_login    datetime                                null,
    constraint username
        unique (username)
);


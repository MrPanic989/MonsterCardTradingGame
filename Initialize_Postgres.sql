CREATE DATABASE mydb;

DROP TABLE IF EXISTS "person";

CREATE TABLE IF NOT EXISTS "person"(
    id UUID UNIQUE PRIMARY KEY not null ,
    username varchar(25) UNIQUE not null ,
    password varchar(55) not null ,
    authtoken varchar(55),
    admin boolean,
    name varchar(25),
    bio varchar(25),
    image varchar(25)
);

INSERT INTO "person" (ID, USERNAME, PASSWORD) VALUES (gen_random_uuid(), 'test', 'test');


SELECT * FROM "person";
-- ACHTUNG: Datenbank anlegen kann man separat machen, falls nötig:
-- CREATE DATABASE mydb;

--DROP TABLE IF EXISTS "person";

--CREATE TABLE IF NOT EXISTS "person"(
--    id UUID UNIQUE PRIMARY KEY not null ,
--    username varchar(25) UNIQUE not null ,
--    password varchar(55) not null ,
--    authtoken varchar(55),
--    admin boolean,
--    name varchar(25),
--    bio varchar(25),
--    image varchar(25)
--);

--INSERT INTO "person" (ID, USERNAME, PASSWORD) VALUES (gen_random_uuid(), 'test', 'test');


--SELECT * FROM "person";

-- 1) Bestehende Tabellen löschen
DROP TABLE IF EXISTS battles CASCADE;
DROP TABLE IF EXISTS trades CASCADE;
DROP TABLE IF EXISTS deck CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS packages CASCADE;
DROP TABLE IF EXISTS person CASCADE;

-- 2) person (User-Tabelle)
CREATE TABLE IF NOT EXISTS person (
      id          UUID         UNIQUE PRIMARY KEY NOT NULL,  -- Eindeutige ID des Users
      username    VARCHAR(25)  UNIQUE NOT NULL,
      password    VARCHAR(55)  NOT NULL,
      authtoken   VARCHAR(55),               -- Simple Auth-Token z.B. "kienboec-mtcgToken"
      admin       BOOLEAN      DEFAULT FALSE, -- True wenn User=admin
      name        VARCHAR(100),
      bio         VARCHAR(1000),
      image       VARCHAR(500),
      coins       INT          DEFAULT 20,    -- Jeder fängt mit 20 an
      elo         INT          DEFAULT 100,   -- Start-ELO
      gamesplayed INT          DEFAULT 0,
      wins        INT          DEFAULT 0,
      losses      INT          DEFAULT 0
);

-- 3) packages
CREATE TABLE IF NOT EXISTS packages (
      package_id UUID PRIMARY KEY,
      purchased  BOOLEAN DEFAULT FALSE
);

-- 4) cards
CREATE TABLE IF NOT EXISTS cards (
     card_id       UUID PRIMARY KEY,
     name          VARCHAR(100) NOT NULL,
     damage        FLOAT        NOT NULL,
     element_type  VARCHAR(50), -- WATER, FIRE, NORMAL
     card_type     VARCHAR(50), -- MONSTER, SPELL oder "special"
     owner         VARCHAR(25), -- Aktuell besitzender User (FK auf person.username)
     package_id    UUID,        -- Zu welchem Package gehört diese Card initially
     CONSTRAINT fk_cards_owner
         FOREIGN KEY (owner)
             REFERENCES person (username)
             ON DELETE SET NULL,    -- Kann man bei Bedarf anpassen (CASCADE oder RESTRICT)
     CONSTRAINT fk_cards_package
         FOREIGN KEY (package_id)
             REFERENCES packages (package_id)
             ON DELETE CASCADE
);

-- 5) deck
CREATE TABLE IF NOT EXISTS deck (
    username  VARCHAR(25) NOT NULL,
    card_id   UUID        NOT NULL,
    PRIMARY KEY (username, card_id),
    CONSTRAINT fk_deck_user
        FOREIGN KEY (username)
            REFERENCES person (username)
            ON DELETE CASCADE,
    CONSTRAINT fk_deck_card
        FOREIGN KEY (card_id)
            REFERENCES cards (card_id)
            ON DELETE CASCADE
);

-- 6) trades
CREATE TABLE IF NOT EXISTS trades (
      trade_id         UUID PRIMARY KEY,
      card_id          UUID NOT NULL,       -- Karte, die angeboten wird
      required_type    VARCHAR(50),         -- "monster" oder "spell"
      required_element VARCHAR(50),         -- z.B. "FIRE", "WATER", "NORMAL" (wenn du das brauchst)
      required_damage  FLOAT,
      username         VARCHAR(25),         -- Wem gehört das Angebot?
      CONSTRAINT fk_trades_card
          FOREIGN KEY (card_id)
              REFERENCES cards (card_id)
              ON DELETE CASCADE,
      CONSTRAINT fk_trades_user
          FOREIGN KEY (username)
              REFERENCES person (username)
              ON DELETE CASCADE
);

-- 7) battles
CREATE TABLE IF NOT EXISTS battles (
   battle_id  SERIAL PRIMARY KEY,
   challenger VARCHAR(25),
   opponent   VARCHAR(25),
   result     VARCHAR(100),
   log        TEXT,
   CONSTRAINT fk_battles_challenger
       FOREIGN KEY (challenger)
           REFERENCES person (username)
           ON DELETE SET NULL,   -- oder CASCADE, je nach Bedarf
   CONSTRAINT fk_battles_opponent
       FOREIGN KEY (opponent)
           REFERENCES person (username)
           ON DELETE SET NULL
);

COMMIT;

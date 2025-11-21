CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100),
    name VARCHAR(50) NOT NULL,
    password_hash CHAR(60) NOT NULL,
    selected_deck_id BIGINT,
    mmr SMALLINT NOT NULL DEFAULT 1000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE card_type AS ENUM ('Magic', 'Type');

CREATE TABLE cards (
    id BIGINT PRIMARY KEY,
    name VARCHAR(10) NOT NULL,
    card_type card_type NOT NULL
);

CREATE TABLE user_cards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    card_id BIGINT NOT NULL REFERENCES cards(id),
    count INT NOT NULL DEFAULT 1,
    UNIQUE (card_id, user_id)
);

CREATE TABLE decks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(31),
    user_id BIGINT NOT NULL REFERENCES users(id)
);

ALTER TABLE users
ADD FOREIGN KEY (selected_deck_id) REFERENCES decks(id);

CREATE TABLE deck_cards (
    id BIGSERIAL PRIMARY KEY,
    deck_id BIGINT NOT NULL REFERENCES decks(id),
    card_id BIGINT NOT NULL REFERENCES cards(id),
    count INT NOT NULL DEFAULT 1,
    UNIQUE (card_id, deck_id)
);

ALTER TABLE users
    ADD CONSTRAINT uq_users_email UNIQUE (email);

CREATE TABLE game_objects (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(31) UNIQUE
);

CREATE TABLE parameter_values(
    id BIGSERIAL PRIMARY KEY,
    parameter_id BIGINT,
    game_object_id BIGINT,
    value DOUBLE PRECISION,
    CONSTRAINT fk_game_object
        FOREIGN KEY (game_object_id)
        REFERENCES game_objects(id),
    CONSTRAINT fk_parameter
        FOREIGN KEY (parameter_id)
        REFERENCES parameters(id),
    CONSTRAINT uq_parameter_game_object
        UNIQUE (parameter_id, game_object_id)
);

CREATE TABLE parameters(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(31) UNIQUE
);

CREATE TYPE user_status AS ENUM (
  'Online',
  'OnMatching',
  'OnPlaying'
);

ALTER TABLE users
    ADD COLUMN status user_status NOT NULL DEFAULT 'Online';
-- game_objects 테이블의 시퀀스 재설정
-- 'game_objects_id_seq' 시퀀스 이름은 PostgeSQL의 명명 규칙에 따라 다를 수 있음
SELECT setval('game_objects_id_seq', (SELECT MAX(id) FROM game_objects), true);

-- parameters 테이블의 시퀀스 재설정
SELECT setval('parameters_id_seq', (SELECT MAX(id) FROM parameters), true);

-- parameter_values 테이블의 시퀀스 재설정
SELECT setval('parameter_values_id_seq', (SELECT MAX(id) FROM parameter_values), true);

ALTER TABLE user_cards
    DROP CONSTRAINT user_cards_user_id_fkey,
    ADD CONSTRAINT user_cards_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE decks
    DROP CONSTRAINT decks_user_id_fkey,
    ADD CONSTRAINT decks_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE deck_cards
    DROP CONSTRAINT deck_cards_deck_id_fkey,
    ADD CONSTRAINT deck_cards_deck_id_fkey
        FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE;

ALTER TABLE users ADD COLUMN member_id BIGINT UNIQUE;
ALTER TABLE users DROP COLUMN member_id;


-- Statistic tables
CREATE TABLE magics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE statistic_games (
    id BIGSERIAL PRIMARY KEY,
    win_user_id BIGINT NOT NULL,
    loss_user_id BIGINT NOT NULL,
    duration INTERVAL NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE statistic_game_cards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    statistic_game_id BIGINT NOT NULL REFERENCES statistic_games(id) ON DELETE CASCADE,
    card_id BIGINT NOT NULL REFERENCES cards(id),
    count INT
);

CREATE INDEX idx_statistic_game_card_user_id_statistic_game_id
    ON statistic_game_cards(user_id, statistic_game_id);

CREATE TABLE statistic_game_magics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    statistic_game_id BIGINT REFERENCES statistic_games(id) ON DELETE CASCADE,
    magic_id BIGINT NOT NULL REFERENCES magics(id),
    count INT
);

CREATE INDEX idx_statistic_game_magic_user_id_statistic_game_id
    ON statistic_game_magics(user_id, statistic_game_id);

ALTER TABLE statistic_games
    ALTER COLUMN duration TYPE BIGINT
        USING EXTRACT(EPOCH FROM duration);

CREATE TYPE game_type AS ENUM ('PVP', 'Practice');

ALTER TABLE statistic_games ADD COLUMN game_type game_type NOT NULL DEFAULT 'PVP';

CREATE TABLE magic_cards (
    id BIGSERIAL PRIMARY KEY,
    magic_id BIGINT REFERENCES magics(id),
    card_id BIGINT REFERENCES cards(id)
);


CREATE TABLE statistic_update_time (
    id BIGSERIAL PRIMARY KEY,
    statistic_game_id BIGINT REFERENCES statistic_games(id) ON DELETE CASCADE,
    name VARCHAR(31),
    min_interval_ms INT,
    max_interval_ms INT,
    mean_interval_ms FLOAT
);

ALTER TABLE statistic_update_time RENAME COLUMN min_interval_ms TO min_interval_ns;
ALTER TABLE statistic_update_time RENAME COLUMN max_interval_ms TO max_interval_ns;
ALTER TABLE statistic_update_time RENAME COLUMN mean_interval_ms TO mean_interval_ns;

CREATE TABLE statistic_delta_time (
    id BIGSERIAL PRIMARY KEY,
    statistic_game_id BIGINT REFERENCES statistic_games(id) ON DELETE CASCADE,

    min_delta_ms INT,
    max_delta_ms INT,
    mean_delta_ms FLOAT,
    frame_count INT
);

ALTER TABLE users DROP name;
ALTER TABLE users DROP email;
ALTER TABLE users DROP member_id;
ALTER TABLE users DROP password_hash;
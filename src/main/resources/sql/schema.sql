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

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'bot_tier') THEN
        CREATE TYPE bot_tier AS ENUM ('INTRO', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ELITE');
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS bot_personas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    tier bot_tier NOT NULL DEFAULT 'BEGINNER',
    deck_id BIGINT NOT NULL REFERENCES decks(id),
    thinking_time_ms INT NOT NULL DEFAULT 250,
    reaction_interval_frames INT NOT NULL DEFAULT 8,
    counter_aggression DOUBLE PRECISION NOT NULL DEFAULT 0.25,
    mmr SMALLINT NOT NULL DEFAULT 1000,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_bot_personas_thinking_time CHECK (thinking_time_ms >= 0),
    CONSTRAINT ck_bot_personas_reaction_interval CHECK (reaction_interval_frames >= 1),
    CONSTRAINT ck_bot_personas_counter_aggression CHECK (counter_aggression >= 0 AND counter_aggression <= 1)
);

CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(31) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS game_object_tags (
    game_object_id BIGINT REFERENCES game_objects(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT uq_game_object_id_tag_id UNIQUE (game_object_id, tag_id)
);

CREATE TABLE IF NOT EXISTS magic_tags (
    magic_id BIGINT NOT NULL REFERENCES magics(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (magic_id, tag_id)
);

CREATE TABLE IF NOT EXISTS tag_counter_rules (
    id BIGSERIAL PRIMARY KEY,
    attacker_tag_id BIGINT NOT NULL REFERENCES tags(id),
    target_tag_id BIGINT NOT NULL REFERENCES tags(id),
    weight DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    UNIQUE(attacker_tag_id, target_tag_id)
);

WITH required_tags AS (
    SELECT tag_name
    FROM (
        VALUES
            ('TYPE_Unit'),
            ('CAT_Small'),
            ('CAT_Ranged'),
            ('CAT_Flying'),
            ('CAT_AoE'),
            ('CAT_Building')
    ) AS tags(tag_name)
)
INSERT INTO tags(name)
SELECT tag_name
FROM required_tags rt
WHERE NOT EXISTS (
    SELECT 1 FROM tags t WHERE t.name = rt.tag_name
);

WITH rule_seed AS (
    SELECT *
    FROM (
        VALUES
            ('CAT_AoE', 'CAT_Small', 1.0)
    ) AS seed(attacker_tag_name, target_tag_name, weight)
)
INSERT INTO tag_counter_rules(attacker_tag_id, target_tag_id, weight)
SELECT attacker.id, target.id, rs.weight
FROM rule_seed rs
JOIN tags attacker ON attacker.name = rs.attacker_tag_name
JOIN tags target ON target.name = rs.target_tag_name
WHERE NOT EXISTS (
    SELECT 1
    FROM tag_counter_rules tcr
    WHERE tcr.attacker_tag_id = attacker.id
      AND tcr.target_tag_id = target.id
);

WITH magic_tag_seed AS (
    SELECT *
    FROM (
        VALUES
            ('magma_explosion', 'CAT_AoE'),
            ('water_explosion', 'CAT_AoE'),
            ('nature_explosion', 'CAT_AoE'),
            ('wind_explosion', 'CAT_AoE'),
            ('rock_explosion', 'CAT_AoE'),
            ('fire_explosion', 'CAT_AoE'),
            ('lightning_explosion', 'CAT_AoE'),
            ('sand_storm', 'CAT_AoE'),
            ('meteor_shower', 'CAT_AoE'),
            ('overgrowth', 'CAT_AoE'),
            ('razor_gale', 'CAT_AoE'),
            ('shock_overload', 'CAT_AoE'),
            ('crater', 'CAT_AoE')
    ) AS seed(magic_name, tag_name)
)
INSERT INTO magic_tags(magic_id, tag_id)
SELECT m.id, t.id
FROM magic_tag_seed mts
JOIN magics m ON m.name = mts.magic_name
JOIN tags t ON t.name = mts.tag_name
WHERE NOT EXISTS (
    SELECT 1
    FROM magic_tags mt
    WHERE mt.magic_id = m.id
      AND mt.tag_id = t.id
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

ALTER TABLE users
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

CREATE TABLE servers (
    id BIGSERIAL PRIMARY KEY,
    protocol VARCHAR(10) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    type VARCHAR(10) NOT NULL, -- GAME, LOBBY, ACCOUNT, ADMIN
    state VARCHAR(10) NOT NULL DEFAULT 'INACTIVE' -- ACTIVE, INACTIVE, DRAINING
);

CREATE TABLE user_magics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    magic_id BIGINT REFERENCES magics(id) ON DELETE CASCADE
);

ALTER TABLE user_magics
    ADD CONSTRAINT uq_user_magics_user_id_magic_id
        UNIQUE (user_id, magic_id);

CREATE TABLE pve_scenarios (
    id BIGINT PRIMARY KEY,
    stage_id VARCHAR(31) NOT NULL
);

CREATE TABLE pve_scenario_objectives (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL REFERENCES pve_scenarios(id) ON DELETE CASCADE,
    installer_id VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL,
    CONSTRAINT uq_pve_scenario_objective_scenario_installer UNIQUE (scenario_id, installer_id),
    CONSTRAINT uq_pve_scenario_objective_scenario_order UNIQUE (scenario_id, sort_order)
);

CREATE TABLE pve_scenario_installers (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL REFERENCES pve_scenarios(id) ON DELETE CASCADE,
    installer_id VARCHAR(50) NOT NULL,
    prefab_type VARCHAR(50) NOT NULL,
    master VARCHAR(20) NOT NULL,
    position_x INT NOT NULL,
    position_y INT NOT NULL,
    position_z INT NOT NULL,
    sort_order INT NOT NULL,
    CONSTRAINT uq_pve_scenario_installer_scenario_installer UNIQUE (scenario_id, installer_id),
    CONSTRAINT uq_pve_scenario_installer_scenario_order UNIQUE (scenario_id, sort_order)
);

CREATE TABLE pve_scenario_events (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL REFERENCES pve_scenarios(id) ON DELETE CASCADE,
    event_id VARCHAR(50) NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    trigger_value INT NOT NULL,
    speaker_installer_id VARCHAR(50),
    message_key VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL,
    CONSTRAINT uq_pve_scenario_event_scenario_event UNIQUE (scenario_id, event_id),
    CONSTRAINT uq_pve_scenario_event_scenario_order UNIQUE (scenario_id, sort_order)
);

CREATE TABLE pve_scenario_event_lines (
    id BIGSERIAL PRIMARY KEY,
    event_row_id BIGINT NOT NULL REFERENCES pve_scenario_events(id) ON DELETE CASCADE,
    line_order INT NOT NULL,
    line_text VARCHAR(255) NOT NULL,
    CONSTRAINT uq_pve_scenario_event_line_row_order UNIQUE (event_row_id, line_order)
);

ALTER TABLE pve_scenario_objectives
    DROP COLUMN scenario_id;
ALTER TABLE pve_scenario_objectives
    ADD COLUMN scenario_id BIGINT NOT NULL REFERENCES scenarios(id) ON DELETE CASCADE DEFAULT 1;
ALTER TABLE pve_scenario_installers
    DROP COLUMN scenario_id;
ALTER TABLE pve_scenario_installers
    ADD COLUMN scenario_id BIGINT NOT NULL REFERENCES scenarios(id) ON DELETE CASCADE DEFAULT 1;
ALTER TABLE pve_scenario_events
    DROP COLUMN scenario_id;
ALTER TABLE pve_scenario_events
    ADD COLUMN scenario_id BIGINT NOT NULL REFERENCES scenarios(id) ON DELETE CASCADE DEFAULT 1;

DROP TABLE pve_scenarios;


ALTER TABLE statistic_game_magics
    DROP CONSTRAINT statistic_game_magics_magic_id_fkey;

ALTER TABLE magic_cards
    DROP CONSTRAINT magic_cards_magic_id_fkey;
ALTER TABLE magic_cards
    ADD CONSTRAINT magic_cards_magic_id_fkey
        FOREIGN KEY (magic_id) REFERENCES magics(id) ON DELETE CASCADE;


ALTER TYPE game_type ADD VALUE 'PVE';

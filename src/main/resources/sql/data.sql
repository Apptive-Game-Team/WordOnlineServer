INSERT INTO cards(id, name, card_type)
VALUES
    (1, 'Fire', 'Type'),
    (2, 'Water', 'Type'),
    (3, 'Lightning', 'Type'),
    (4, 'Rock', 'Type'),
    (5, 'Nature', 'Type'),
    (6, 'Shoot', 'Magic'),
    (7, 'Build', 'Magic'),
    (8, 'Spawn', 'Magic'),
    (9, 'Explode', 'Magic'),
    (10, 'Wind', 'Type');

INSERT INTO game_objects
VALUES
    (1, 'slime'),
    (2, 'shoot'),
    (3, 'explode'),
    (4, 'spawn'),
    (5, 'build'),
    (6, 'field');

INSERT INTO parameters
VALUES
    (1, 'speed'),
    (2, 'damage'),
    (3, 'radius'),
    (4, 'hp'),
    (5, 'mass'),
    (6, 'duration');

INSERT INTO parameter_values(game_object_id, parameter_id, value)
VALUES
    (1, 3, 0.5),
    (2, 3, 0.5),
    (3, 3, 0.5),
    (5, 3, 0.5),
    (6, 3, 0.5),

    (1, 2, 3),
    (2, 2, 10),
    (3, 2, 8),

    (1, 4, 8),
    (5, 4, 5),

    (1, 1, 0.8),

    (1, 5, 1),

    (6, 6, 3);

INSERT INTO magics(id, name)
VALUES
    (1, 'fire_slime_swarm'),
    (2, 'water_slime_swarm'),
    (3, 'lightning_slime_swarm'),
    (4, 'nature_slime_swarm'),
    (5, 'rock_slime_swarm'),
    (6, 'wind_slime_swarm'),

    (7, 'fire_shot'),
    (8, 'water_shot'),
    (9, 'lightning_shot'),
    (10, 'nature_shot'),
    (11, 'rock_shot'),
    (12, 'wind_shot'),

    (13, 'fire_slime_nest'),
    (14, 'water_slime_nest'),
    (15, 'lightning_slime_nest'),
    (16, 'nature_slime_nest'),
    (17, 'rock_slime_nest'),
    (18, 'wind_slime_nest'),

    (19, 'fire_explosion'),
    (20, 'water_explosion'),
    (21, 'lightning_explosion'),
    (22, 'nature_explosion'),
    (23, 'rock_explosion'),
    (24, 'wind_explosion'),

    (25, 'cannon'),
    (26, 'tower'),
    (27, 'mana_well'),
    (28, 'aqua_archer'),
    (29, 'rock_golem'),
    (30, 'storm_rider'),
    (31, 'thunder_spirit'),
    (32, 'fire_spirit');

SELECT setval('magics_id_seq', (SELECT MAX(id) FROM magics), true);


-- For give card to User
INSERT INTO user_cards(user_id, card_id, count)
SELECT id, 11, 3
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM user_cards uc
    WHERE uc.user_id = u.id AND uc.card_id = 11
);

-- For give all magic to User
INSERT INTO user_magics(user_id, magic_id)
SELECT u.id, m.id
FROM users u, magics m
WHERE m.access_type = 'DEFAULT' AND
      NOT EXISTS(
          SELECT 1
          FROM user_magics um
          WHERE um.user_id = u.id AND um.magic_id = m.id
      );

INSERT INTO pve_scenarios(id, stage_id)
VALUES
    (11, '1-1'),
    (12, '1-2'),
    (13, '1-3'),
    (14, '1-4');

INSERT INTO pve_scenario_objectives(scenario_id, installer_id, sort_order)
VALUES
    (11, 'enemy_boss_1', 1),
    (12, 'nature_nest', 1),
    (12, 'water_nest', 2),
    (13, 'pve_vine_colony', 1),
    (14, 'pve_vine_witch', 1);

INSERT INTO pve_scenario_installers(
    scenario_id,
    installer_id,
    prefab_type,
    master,
    position_x,
    position_y,
    position_z,
    sort_order
)
VALUES
    (11, 'enemy_boss_1', 'PveNatureSlimeNest', 'RightPlayer', 14, 5, 0, 1),
    (12, 'nature_nest', 'PveNatureSlimeNest', 'RightPlayer', 14, 7, 0, 1),
    (12, 'water_nest', 'PveWaterSlimeNest', 'RightPlayer', 14, 3, 0, 2),
    (13, 'pve_vine_colony', 'PveVineColony', 'RightPlayer', 14, 5, 0, 1),
    (14, 'pve_vine_witch', 'PveVineWitch', 'RightPlayer', 14, 5, 0, 1);

INSERT INTO pve_scenario_events(
    id,
    scenario_id,
    event_id,
    trigger_type,
    trigger_value,
    speaker_installer_id,
    message_key,
    sort_order
)
VALUES
    (111, 11, 'intro', 'FrameNumGte', 10, 'enemy_boss_1', 'pve_1_1_intro', 1),
    (112, 11, 'enemyLine', 'FrameNumGte', 20, 'enemy_boss_1', 'pve_1_1_enemy_line', 2),
    (121, 12, 'intro', 'FrameNumGte', 10, 'water_nest', 'pve_1_2_intro', 1),
    (122, 12, 'enemyLine', 'FrameNumGte', 20, 'water_nest', 'pve_1_2_enemy_line', 2),
    (131, 13, 'intro', 'FrameNumGte', 10, 'pve_vine_colony', 'pve_1_3_intro', 1),
    (132, 13, 'enemyLine', 'FrameNumGte', 20, 'pve_vine_colony', 'pve_1_3_enemy_line', 2),
    (141, 14, 'intro', 'FrameNumGte', 10, 'pve_vine_witch', 'pve_1_4_intro', 1),
    (142, 14, 'enemyLine', 'FrameNumGte', 20, 'pve_vine_witch', 'pve_1_4_enemy_line', 2);

INSERT INTO pve_scenario_event_lines(event_row_id, line_order, line_text)
VALUES
    (111, 1, 'Stage 1-1'),
    (111, 2, 'Destroy the enemy nest!'),
    (112, 1, 'Burn it all down!'),

    (121, 1, 'Stage 1-2'),
    (121, 2, 'Destroy the enemy nest!'),
    (122, 1, 'Burn it all down!'),

    (131, 1, 'Stage 1-3'),
    (131, 2, 'Destroy the vine colony!'),
    (132, 1, 'Burn it all down!'),

    (141, 1, 'Stage 1-4'),
    (141, 2, 'Destroy the vine witch!'),
    (142, 1, 'Burn it all down!');

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


-- For give card to User
INSERT INTO user_cards(user_id, card_id, count)
SELECT id, 11, 3
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM user_cards uc
    WHERE uc.user_id = u.id AND uc.card_id = 11
);
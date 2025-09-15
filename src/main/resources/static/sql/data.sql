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
    (9, 'Explode', 'Magic');

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
INSERT INTO parameters(name)
SELECT 'quantity'
WHERE NOT EXISTS (
    SELECT 1
    FROM parameters
    WHERE name = 'quantity'
);

INSERT INTO parameters(name)
SELECT 'projectile_speed'
WHERE NOT EXISTS (
    SELECT 1
    FROM parameters
    WHERE name = 'projectile_speed'
);

INSERT INTO parameters(name)
SELECT 'z_force'
WHERE NOT EXISTS (
    SELECT 1
    FROM parameters
    WHERE name = 'z_force'
);

INSERT INTO game_objects(name)
SELECT 'ember_spirit'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'ember_spirit');

INSERT INTO game_objects(name)
SELECT 'seed_spirit'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'seed_spirit');

INSERT INTO game_objects(name)
SELECT 'water_slime'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'water_slime');

INSERT INTO game_objects(name)
SELECT 'fire_slime'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'fire_slime');

INSERT INTO game_objects(name)
SELECT 'electric_slime'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'electric_slime');

INSERT INTO game_objects(name)
SELECT 'leaf_slime'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'leaf_slime');

INSERT INTO game_objects(name)
SELECT 'rock_slime'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'rock_slime');

INSERT INTO game_objects(name)
SELECT 'wind_slime'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'wind_slime');

INSERT INTO game_objects(name)
SELECT 'water_shot'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'water_shot');

INSERT INTO game_objects(name)
SELECT 'fire_shot'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'fire_shot');

INSERT INTO game_objects(name)
SELECT 'electric_shot'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'electric_shot');

INSERT INTO game_objects(name)
SELECT 'rock_rolling'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'rock_rolling');

INSERT INTO game_objects(name)
SELECT 'wind_blade'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'wind_blade');

INSERT INTO game_objects(name)
SELECT 'will_o_wisp'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'will_o_wisp');

INSERT INTO game_objects(name)
SELECT 'bubble_spirit'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'bubble_spirit');

INSERT INTO game_objects(name)
SELECT 'bubble_generator'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'bubble_generator');

INSERT INTO game_objects(name)
SELECT 'fire_explode'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'fire_explode');

INSERT INTO game_objects(name)
SELECT 'water_explode'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'water_explode');

INSERT INTO game_objects(name)
SELECT 'water_explosion'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'water_explosion');

INSERT INTO game_objects(name)
SELECT 'leaf_explode'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'leaf_explode');

INSERT INTO game_objects(name)
SELECT 'rock_explode'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'rock_explode');

INSERT INTO game_objects(name)
SELECT 'electric_explode'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'electric_explode');

INSERT INTO game_objects(name)
SELECT 'wind_explode'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'wind_explode');

INSERT INTO game_objects(name)
SELECT 'magma_explosion'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'magma_explosion');

INSERT INTO game_objects(name)
SELECT 'shock_overload'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'shock_overload');

INSERT INTO game_objects(name)
SELECT 'fire_drop'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'fire_drop');

INSERT INTO game_objects(name)
SELECT 'rain_cloud'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'rain_cloud');

INSERT INTO game_objects(name)
SELECT 'nature_drop'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'nature_drop');

INSERT INTO game_objects(name)
SELECT 'rock_drop'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'rock_drop');

INSERT INTO game_objects(name)
SELECT 'lightning_drop'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'lightning_drop');

INSERT INTO game_objects(name)
SELECT 'leaf_drop'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'leaf_drop');

INSERT INTO game_objects(name)
SELECT 'wind_drop'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'wind_drop');

INSERT INTO game_objects(name)
SELECT 'sand_storm'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'sand_storm');

INSERT INTO game_objects(name)
SELECT 'will_o_wisp'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'will_o_wisp');

INSERT INTO game_objects(name)
SELECT 'fire_summon'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'fire_summon');

INSERT INTO game_objects(name)
SELECT 'electric_summon'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'electric_summon');

INSERT INTO game_objects(name)
SELECT 'rock_summon'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'rock_summon');

INSERT INTO game_objects(name)
SELECT 'wind_summon'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'wind_summon');

INSERT INTO game_objects(name)
SELECT 'fire_field'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'fire_field');

INSERT INTO game_objects(name)
SELECT 'water_field'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'water_field');

INSERT INTO game_objects(name)
SELECT 'electric_field'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'electric_field');

INSERT INTO game_objects(name)
SELECT 'leaf_field'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'leaf_field');

INSERT INTO game_objects(name)
SELECT 'fire_spirit'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'fire_spirit');

INSERT INTO game_objects(name)
SELECT 'magma_spirit'
WHERE NOT EXISTS (SELECT 1 FROM game_objects WHERE name = 'magma_spirit');

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 3
FROM game_objects go
JOIN parameters p ON p.name = 'quantity'
WHERE go.name IN ('ember_spirit', 'seed_spirit', 'water_slime')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 1
FROM game_objects go
JOIN parameters p ON p.name = 'mass'
WHERE go.name IN ('ember_spirit', 'seed_spirit', 'water_slime', 'fire_slime', 'electric_slime', 'leaf_slime', 'rock_slime', 'wind_slime')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.5
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name IN ('ember_spirit', 'seed_spirit', 'water_slime', 'fire_slime', 'electric_slime', 'leaf_slime', 'rock_slime', 'wind_slime')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 8
FROM game_objects go
JOIN parameters p ON p.name = 'hp'
WHERE go.name IN ('ember_spirit', 'seed_spirit', 'water_slime', 'fire_slime', 'electric_slime', 'leaf_slime', 'rock_slime', 'wind_slime')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.8
FROM game_objects go
JOIN parameters p ON p.name = 'speed'
WHERE go.name IN ('ember_spirit', 'seed_spirit', 'water_slime', 'fire_slime', 'electric_slime', 'leaf_slime', 'rock_slime', 'wind_slime')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 3
FROM game_objects go
JOIN parameters p ON p.name = 'damage'
WHERE go.name IN ('ember_spirit', 'seed_spirit', 'water_slime', 'fire_slime', 'electric_slime', 'leaf_slime', 'rock_slime', 'wind_slime')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 1
FROM game_objects go
JOIN parameters p ON p.name = 'attack_interval'
WHERE go.name IN ('ember_spirit', 'seed_spirit', 'water_slime', 'fire_slime', 'electric_slime', 'leaf_slime', 'rock_slime', 'wind_slime')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.5
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name IN ('water_shot', 'fire_shot', 'electric_shot', 'rock_rolling', 'wind_blade', 'will_o_wisp')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.5
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name = 'leaf_drop'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.5
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name = 'will_o_wisp'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 8
FROM game_objects go
JOIN parameters p ON p.name = 'speed'
WHERE go.name = 'will_o_wisp'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 10
FROM game_objects go
JOIN parameters p ON p.name = 'damage'
WHERE go.name IN ('water_shot', 'fire_shot', 'electric_shot', 'rock_rolling', 'wind_blade')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 8
FROM game_objects go
JOIN parameters p ON p.name = 'speed'
WHERE go.name IN ('water_shot', 'fire_shot', 'electric_shot', 'rock_rolling', 'wind_blade', 'will_o_wisp')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.45
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name = 'bubble_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 18
FROM game_objects go
JOIN parameters p ON p.name = 'hp'
WHERE go.name = 'bubble_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 1
FROM game_objects go
JOIN parameters p ON p.name = 'mass'
WHERE go.name = 'bubble_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.7
FROM game_objects go
JOIN parameters p ON p.name = 'speed'
WHERE go.name = 'bubble_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 5
FROM game_objects go
JOIN parameters p ON p.name = 'damage'
WHERE go.name = 'bubble_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 1.2
FROM game_objects go
JOIN parameters p ON p.name = 'attack_interval'
WHERE go.name = 'bubble_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 4.5
FROM game_objects go
JOIN parameters p ON p.name = 'attack_range'
WHERE go.name = 'bubble_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 8
FROM game_objects go
JOIN parameters p ON p.name = 'projectile_speed'
WHERE go.name = 'bubble_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.65
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name = 'bubble_generator'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 18
FROM game_objects go
JOIN parameters p ON p.name = 'hp'
WHERE go.name = 'bubble_generator'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 1000
FROM game_objects go
JOIN parameters p ON p.name = 'mass'
WHERE go.name = 'bubble_generator'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 1.2
FROM game_objects go
JOIN parameters p ON p.name = 'attack_interval'
WHERE go.name = 'bubble_generator'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 2.5
FROM game_objects go
JOIN parameters p ON p.name = 'attack_range'
WHERE go.name = 'bubble_generator'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 12
FROM game_objects go
JOIN parameters p ON p.name = 'duration'
WHERE go.name = 'bubble_generator'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 8
FROM game_objects go
JOIN parameters p ON p.name = 'projectile_speed'
WHERE go.name = 'bubble_generator'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.5
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name IN ('fire_explode', 'water_explode', 'leaf_explode', 'rock_explode', 'electric_explode', 'wind_explode', 'magma_explosion', 'shock_overload', 'sand_storm', 'rain_cloud', 'fire_field', 'water_field', 'electric_field', 'leaf_field', 'fire_drop', 'nature_drop', 'rock_drop', 'lightning_drop', 'wind_drop', 'fire_summon', 'electric_summon', 'rock_summon', 'wind_summon')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 8
FROM game_objects go
JOIN parameters p ON p.name = 'damage'
WHERE go.name IN ('fire_explode', 'water_explode', 'leaf_explode', 'rock_explode', 'electric_explode', 'wind_explode', 'magma_explosion', 'shock_overload', 'fire_drop', 'nature_drop', 'rock_drop', 'lightning_drop', 'wind_drop', 'leaf_drop')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 3
FROM game_objects go
JOIN parameters p ON p.name = 'duration'
WHERE go.name IN ('sand_storm', 'rain_cloud', 'fire_field', 'water_field', 'electric_field', 'leaf_field')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 5
FROM game_objects go
JOIN parameters p ON p.name = 'hp'
WHERE go.name IN ('fire_summon', 'electric_summon', 'rock_summon', 'wind_summon')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 1
FROM game_objects go
JOIN parameters p ON p.name = 'mass'
WHERE go.name = 'fire_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.35
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name = 'fire_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 9
FROM game_objects go
JOIN parameters p ON p.name = 'hp'
WHERE go.name = 'fire_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.8
FROM game_objects go
JOIN parameters p ON p.name = 'speed'
WHERE go.name = 'fire_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 4
FROM game_objects go
JOIN parameters p ON p.name = 'damage'
WHERE go.name = 'fire_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 1
FROM game_objects go
JOIN parameters p ON p.name = 'attack_interval'
WHERE go.name = 'fire_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 4
FROM game_objects go
JOIN parameters p ON p.name = 'attack_range'
WHERE go.name = 'fire_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 3
FROM game_objects go
JOIN parameters p ON p.name = 'mass'
WHERE go.name = 'magma_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.9
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name = 'magma_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 45
FROM game_objects go
JOIN parameters p ON p.name = 'hp'
WHERE go.name = 'magma_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.45
FROM game_objects go
JOIN parameters p ON p.name = 'speed'
WHERE go.name = 'magma_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 8
FROM game_objects go
JOIN parameters p ON p.name = 'damage'
WHERE go.name = 'magma_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 1.6
FROM game_objects go
JOIN parameters p ON p.name = 'attack_interval'
WHERE go.name = 'magma_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 4
FROM game_objects go
JOIN parameters p ON p.name = 'attack_range'
WHERE go.name = 'magma_spirit'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 0.5
FROM game_objects go
JOIN parameters p ON p.name = 'radius'
WHERE go.name = 'water_explosion'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 8
FROM game_objects go
JOIN parameters p ON p.name = 'damage'
WHERE go.name = 'water_explosion'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT go.id, p.id, 3
FROM game_objects go
JOIN parameters p ON p.name = 'z_force'
WHERE go.name = 'water_explosion'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_values pv
      WHERE pv.game_object_id = go.id
        AND pv.parameter_id = p.id
  );

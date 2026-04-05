# Client Game Data Versioning & Caching

This document describes the plan for versioning and caching game configuration data (magic recipes, game parameters, elemental chart, card definitions) on the Unity client using PlayerPrefs.

---

## Why This Is Needed

After the lockstep migration, the client simulation runs locally. It needs the same game configuration the server uses:

| Data | Current location | Problem |
|---|---|---|
| Magic recipes | `LocalCombinedMagicData.cs` — hardcoded C# list | Out of sync when server DB changes |
| Game parameters (HP, damage, mana cost, ...) | Server DB + `ParameterService` | Client has no access |
| Elemental chart | `ElementalChart.java` — hardcoded Java | Not available to client at all |
| Card definitions | Server DB `cards` table | Client uses a local C# enum |

Without a versioned cache, one of two things is true: the data is duplicated and drifts, or the client fetches it on every startup (slow and fragile offline).

The versioned cache solves this:
- Server exposes config as a single versioned JSON endpoint.
- Client downloads it once and stores it in PlayerPrefs.
- On next launch the client compares its cached version to the server's current version — only re-fetches on mismatch.
- The cached data feeds both the MagicBook scene and (after lockstep migration) the local simulation.

---

## Data Included in the Cache

### 1. Magic Recipes
Currently parsed from the database in `DatabaseMagicParser.java`. Each recipe maps a sorted card list to a spell.

```json
{
  "id": 32,
  "name": "fire_spirit",
  "cards": ["Spawn", "Fire"],
  "spritePath": "Game/spawn/fire_spirit"
}
```

### 2. Game Parameters
Balance values stored in `parameter_values` DB table, accessed via `ParameterService`. Also includes hardcoded constants from `PlayerData.java`, `GameConfig.java`, `ManaCharger.java`.

```json
{
  "slime":  { "hp": 8,   "damage": 3,  "radius": 0.5, "speed": 0.8, "mass": 1 },
  "shoot":  { "damage": 10, "radius": 0.5 },
  "explode":{ "damage": 8,  "radius": 0.5 },
  "field":  { "duration": 3 },
  "player": { "max_hp": 100, "max_card_num": 6, "max_mana": 10,
              "mana_charge_interval": 0.2, "mana_charge_value": 1 },
  "world":  { "fps": 20, "width": 18, "height": 10,
              "gravity": 4.0, "x_bound": 15, "y_bound": 10 },
  "fire":   { "mana_cost": 2 },
  "water":  { "mana_cost": 2 },
  "lightning": { "mana_cost": 2 },
  "rock":   { "mana_cost": 2 },
  "nature": { "mana_cost": 2 },
  "wind":   { "mana_cost": 2 },
  "shoot_card": { "mana_cost": 1 },
  "build_card": { "mana_cost": 1 }
}
```

### 3. Elemental Chart
Currently hardcoded in `ElementalChart.java` as a 2D float array indexed by `MagicType` ordinal.

```json
[
  { "attacker": "Fire",      "defender": "Water",     "multiplier": 2.0 },
  { "attacker": "Fire",      "defender": "Nature",    "multiplier": 0.5 },
  { "attacker": "Nature",    "defender": "Fire",      "multiplier": 2.0 },
  { "attacker": "Water",     "defender": "Fire",      "multiplier": 0.5 },
  { "attacker": "Water",     "defender": "Nature",    "multiplier": 2.0 },
  { "attacker": "Water",     "defender": "Lightning", "multiplier": 2.0 },
  { "attacker": "Wind",      "defender": "Lightning", "multiplier": 2.0 },
  { "attacker": "Wind",      "defender": "Rock",      "multiplier": 2.0 },
  { "attacker": "Rock",      "defender": "Fire",      "multiplier": 0.5 }
]
```
Only non-1.0 entries are stored; everything else defaults to 1.0.

### 4. Card Definitions
Currently a Java enum (`CardType`) and a `cards` DB table.

```json
[
  { "id": 1,  "name": "Fire",      "cardType": "Type" },
  { "id": 2,  "name": "Water",     "cardType": "Type" },
  { "id": 3,  "name": "Lightning", "cardType": "Type" },
  { "id": 4,  "name": "Rock",      "cardType": "Type" },
  { "id": 5,  "name": "Nature",    "cardType": "Type" },
  { "id": 10, "name": "Wind",      "cardType": "Type" },
  { "id": 6,  "name": "Shoot",     "cardType": "Magic" },
  { "id": 7,  "name": "Build",     "cardType": "Magic" },
  { "id": 8,  "name": "Spawn",     "cardType": "Magic" },
  { "id": 9,  "name": "Explode",   "cardType": "Magic" }
]
```

---

## Full Config Payload Shape

`GET /api/data/config` returns:

```json
{
  "version": "a3f9c2d1",
  "magicRecipes": [ ... ],
  "parameters": { ... },
  "elementalChart": [ ... ],
  "cards": [ ... ]
}
```

`version` is a content hash (SHA-256 truncated to 8 hex chars) computed from the serialized body. It changes automatically whenever any data changes — no manual version bumping required.

---

## Server Changes

### New: `GameDataController`
`game/src/main/java/com/wordonline/server/data/controller/GameDataController.java`

```java
@RestController
@RequestMapping("/api/data")
public class GameDataController {

    @GetMapping("/version")
    public GameDataVersionDto getVersion() {
        return new GameDataVersionDto(gameDataService.getVersion());
    }

    @GetMapping("/config")
    public GameConfigDto getConfig() {
        return gameDataService.getConfig();
    }
}
```

Authentication: public (no auth required — config data is not sensitive).

### New: `GameDataService`
`game/src/main/java/com/wordonline/server/data/service/GameDataService.java`

- Aggregates data from `DatabaseMagicParser`, `ParameterService`, `ElementalChart`, `CardRepository`.
- Serializes to `GameConfigDto`.
- Computes and caches the version hash. Hash is recomputed on cache invalidation of `DatabaseMagicParser` or `ParameterService`.

```java
@Service
public class GameDataService {
    private volatile String cachedVersion = null;
    private volatile GameConfigDto cachedConfig = null;

    public String getVersion() {
        if (cachedVersion == null) buildCache();
        return cachedVersion;
    }

    public GameConfigDto getConfig() {
        if (cachedConfig == null) buildCache();
        return cachedConfig;
    }

    public void invalidate() {
        cachedVersion = null;
        cachedConfig = null;
    }

    private synchronized void buildCache() {
        GameConfigDto config = aggregate();
        String json = objectMapper.writeValueAsString(config);
        String hash = sha256Hex(json).substring(0, 8);
        config.setVersion(hash);
        cachedConfig = config;
        cachedVersion = hash;
    }
}
```

`invalidate()` is called from `DatabaseMagicParser.invalidate()` and `ParameterService.invalidate()` (both already have invalidation methods — hook in here).

### New DTOs
`game/src/main/java/com/wordonline/server/data/dto/`

```java
record GameDataVersionDto(String version) {}

record GameConfigDto(
    String version,
    List<MagicRecipeDto> magicRecipes,
    Map<String, Map<String, Double>> parameters,
    List<ElementalChartEntryDto> elementalChart,
    List<CardDefinitionDto> cards
) {}

record MagicRecipeDto(long id, String name, List<String> cards, String spritePath) {}
record ElementalChartEntryDto(String attacker, String defender, double multiplier) {}
record CardDefinitionDto(long id, String name, String cardType) {}
```

---

## Client Changes

### PlayerPrefs Keys

```csharp
public static class GameDataPrefs
{
    public const string Version = "GameData.Version";   // e.g. "a3f9c2d1"
    public const string Config  = "GameData.Config";    // full JSON string
}
```

### New: `GameDataManager`
`client/Assets/Scripts/Data/GameDataManager.cs`

Singleton MonoBehaviour, loaded before any scene that needs game data (use Unity's `RuntimeInitializeOnLoadMethod` or place in a persistent bootstrapper scene).

```csharp
public class GameDataManager : MonoBehaviour
{
    public static GameDataManager Instance { get; private set; }
    public static GameConfig Config { get; private set; }

    // Called before any scene loads
    [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.BeforeSceneLoad)]
    private static void Bootstrap() { ... }

    public IEnumerator Initialize()
    {
        // 1. Fetch server version
        yield return FetchVersion(out string serverVersion);

        string cachedVersion = PlayerPrefs.GetString(GameDataPrefs.Version, "");

        if (cachedVersion == serverVersion && PlayerPrefs.HasKey(GameDataPrefs.Config))
        {
            // Cache is valid — load from PlayerPrefs
            string json = PlayerPrefs.GetString(GameDataPrefs.Config);
            Config = JsonUtility.FromJson<GameConfig>(json);
        }
        else
        {
            // Cache is stale or missing — fetch full config
            yield return FetchConfig(out GameConfig config);
            Config = config;

            // Persist to PlayerPrefs
            string json = JsonUtility.ToJson(config);
            PlayerPrefs.SetString(GameDataPrefs.Config, json);
            PlayerPrefs.SetString(GameDataPrefs.Version, config.version);
            PlayerPrefs.Save();
        }
    }
}
```

### New: `GameConfig` (C# DTO)
`client/Assets/Scripts/Data/GameConfig.cs`

```csharp
[Serializable]
public class GameConfig
{
    public string version;
    public List<MagicRecipeData> magicRecipes;
    public GameParameters parameters;
    public List<ElementalChartEntry> elementalChart;
    public List<CardDefinitionData> cards;
}

[Serializable]
public class MagicRecipeData
{
    public long id;
    public string name;
    public List<string> cards;       // card names as strings
    public string spritePath;
}

[Serializable]
public class ElementalChartEntry
{
    public string attacker;
    public string defender;
    public float multiplier;
}

[Serializable]
public class CardDefinitionData
{
    public long id;
    public string name;
    public string cardType;          // "Type" or "Magic"
}
```

`GameParameters` is a nested class matching the JSON parameter map structure. Since `JsonUtility` doesn't support `Dictionary`, use a flat struct:

```csharp
[Serializable]
public class GameParameters
{
    // Player
    public int playerMaxHp = 100;
    public int playerMaxCardNum = 6;
    public int playerMaxMana = 10;
    public float playerManaChargeInterval = 0.2f;
    public int playerManaChargeValue = 1;

    // World physics
    public int fps = 20;
    public float gravity = 4f;
    public int xBound = 15;
    public int yBound = 10;

    // Per-card mana costs (flat fields, one per card)
    public int manaFire = 2;
    public int manaWater = 2;
    // ... etc.

    // Per-object stats (flat fields)
    public float slimeHp = 8;
    public float slimeDamage = 3;
    public float slimeRadius = 0.5f;
    public float slimeSpeed = 0.8f;
    // ... etc.
}
```

> **Note**: `JsonUtility` cannot deserialize `Dictionary<string, Dictionary<string, double>>`. Use flat fields or a custom JSON parser (e.g. Newtonsoft.Json via Unity package) if dynamic lookup is needed.

### Existing files to replace/update

| File | Action |
|---|---|
| `LocalCombinedMagicData.cs` | Replace hardcoded list with `GameDataManager.Config.magicRecipes` |
| `CombinedMagicResolver.cs` | Read recipes from `GameDataManager.Config` instead of static list |
| `UserMagicService.cs` | No change — still fetches unlocked IDs from `/api/users/mine/magics`; filter against `Config.magicRecipes` |
| `ElementalChart.java` | After migration: expose via `GameDataService`; long-term keep Java version as source of truth |

---

## Initialization Flow

```
App Launch
  │
  ▼
BootstrapScene (new, lightweight)
  │
  ├─ GameDataManager.Initialize()
  │     │
  │     ├─ GET /api/data/version  ──────────────────────────────┐
  │     │                                                        │
  │     ├─ Compare with PlayerPrefs["GameData.Version"]          │
  │     │                                                        │
  │     ├─ [MATCH] load PlayerPrefs["GameData.Config"] → parse  │
  │     │                                                        │
  │     └─ [MISMATCH] GET /api/data/config ────────────────────►│
  │                    store to PlayerPrefs                       │
  │                    update version key                         │
  │
  ▼
LoginScene (proceeds normally)
```

If the version fetch fails (no network), the client falls back to the cached config silently. If there is no cache at all, show an error screen.

---

## Cache Invalidation (Server Side)

The version hash recomputes automatically when:

1. A magic recipe is added/removed/changed in the `magics` or `magic_cards` DB table → `DatabaseMagicParser.invalidate()` is called → `GameDataService.invalidate()` is triggered.
2. A parameter value is changed in `parameter_values` → `ParameterService.invalidate()` → `GameDataService.invalidate()`.
3. The `ElementalChart` source code is changed → `GameDataService.invalidate()` must be called manually on startup (acceptable since this is a deploy-time change).

There is no need to manually maintain a version number anywhere.

---

## Migration Path

### Phase 1 — Server endpoint (no client change)
- Implement `GameDataService`, `GameDataController`, DTOs.
- Verify `GET /api/data/config` returns correct data.
- Verify `GET /api/data/version` returns a stable hash that changes when data changes.

### Phase 2 — Client cache infrastructure (no behavior change)
- Implement `GameDataManager`, `GameConfig` C# classes.
- Add `BootstrapScene` or `RuntimeInitializeOnLoadMethod` bootstrap.
- On first launch: fetch and cache. On subsequent launches: load from PlayerPrefs.
- Keep using `LocalCombinedMagicData.cs` for now — just verify cache loads correctly.

### Phase 3 — Switch data sources
- `CombinedMagicResolver` reads from `GameDataManager.Config.magicRecipes`.
- `MagicBookScene` / `MagicInfoFactory` reads from `GameDataManager.Config`.
- Delete `LocalCombinedMagicData.cs`.

### Phase 4 — Feed simulation
- After lockstep Phase 3 (client simulation), `SimWorld.Init()` reads parameters from `GameDataManager.Config.parameters` instead of hardcoded constants.
- `SimElementalChart` reads from `GameDataManager.Config.elementalChart`.

---

## Files Summary

### Server: New
| File | Purpose |
|---|---|
| `data/controller/GameDataController.java` | REST endpoints `/api/data/version` and `/api/data/config` |
| `data/service/GameDataService.java` | Aggregates + hashes config |
| `data/dto/GameConfigDto.java` | Full config payload |
| `data/dto/GameDataVersionDto.java` | Version-only response |
| `data/dto/MagicRecipeDto.java` | Per-recipe shape |
| `data/dto/ElementalChartEntryDto.java` | Per-matchup shape |
| `data/dto/CardDefinitionDto.java` | Per-card shape |

### Server: Changed
| File | Change |
|---|---|
| `game/domain/magic/parser/DatabaseMagicParser.java` | Call `gameDataService.invalidate()` from existing `invalidate()` |
| `game/service/ParameterService.java` | Call `gameDataService.invalidate()` from existing `invalidate()` |

### Client: New
| File | Purpose |
|---|---|
| `Data/GameDataManager.cs` | Singleton, version check, fetch, PlayerPrefs storage |
| `Data/GameConfig.cs` | C# DTO matching server JSON |

### Client: Changed
| File | Change |
|---|---|
| `Data/Magic/LocalCombinedMagicData.cs` | Replaced by `GameDataManager.Config.magicRecipes` |
| `Data/Magic/CombinedMagicResolver.cs` | Read recipes from `GameDataManager.Config` |

# Lifesteal+

Created by **Falthera**.

## Features

- Heart-based PvP: lose hearts on death, gain hearts on player kills
- Archetype system with unique passive abilities
- Revive mechanics via Revival Totem crafting recipe
- Heart Crystal crafting recipe
- Full MySQL or SQLite database support (via HikariCP)
- PlaceholderAPI integration for server stats tracking
- Vault economy support for heart transactions
- Hot-reloadable configuration
- Comprehensive permission-based command system
- Anvil-based item crafting with custom recipes
- Custom GUI for heart management

## Installation

1. Build the plugin with `./gradlew shadowJar` or download the latest release.
2. Place `Lifesteal+.jar` in your server's `plugins/` folder.
3. Start or reload your server.
4. Edit the generated `plugins/Lifesteal+/config.yml` to configure database, hearts, and archetypes.
5. Use `/lifesteal reload` to apply changes without a restart.

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/lifesteal` | Opens the main GUI | `lifesteal.gui` |
| `/lifesteal hearts <player> <amount>` | Give or remove hearts | `lifesteal.hearts` |
| `/lifesteal revive <player>` | Revive a dead player | `lifesteal.revive` |
| `/lifesteal archetype <player> <archetype>` | Set player archetype | `lifesteal.archetype` |
| `/lifesteal reload` | Reload configuration | `lifesteal.reload` |
| `/lifesteal give <player> <item>` | Give plugin items | `lifesteal.give` |

Aliases: `/ls`, `/hearts`, `/lifesteal`

## Recipes

### Heart Crystal
- **Ingredients:** Diamond, Nether Star, Emerald
- **Result:** Heart Crystal — instantly adds 1 heart (2 HP) to the user on right-click.

### Revival Totem
- **Ingredients:** Golden Apple, Totem of Undying, Ghast Tear
- **Result:** Revival Totem — revives a banned/eliminated player when used on their grave or bed.

## Configuration

The plugin uses `config.yml` with full hot-reload support. Key options include:
- Starting heart count
- Minimum and maximum hearts
- Kill reward and death penalty multipliers
- Database settings (SQLite or MySQL)
- Archetype definitions and passive cooldowns
- GUI layout and display names

Run `/lifesteal reload` to apply changes without restarting.

## PlaceholderAPI

Lifesteal+ registers the following placeholders:

| Placeholder | Description |
|-------------|-------------|
| `%lifesteal_hearts%` | Current hearts of the viewing player |
| `%lifesteal_max_hearts%` | Maximum allowed hearts |
| `%lifesteal_kills%` | Total player kills credited to hearts |
| `%lifesteal_deaths%` | Total deaths |
| `%lifesteal_archetype%` | Active archetype name |
| `%lifesteal_rank%` | Heart rank (e.g. Bronze, Silver, Gold) |

## Vault

When Vault is present, Lifesteal+ can:
- Charge an economy fee for `/lifesteal hearts` transactions
- Reward killers with in-server currency for PvP kills
- Use your existing economy provider for heart shop integrations

Enable economy features in `config.yml` under the `economy` section.

## Database Setup

### SQLite (default)
No external setup required. Data is stored in `plugins/Lifesteal+/data.db`. Suitable for small to medium servers.

### MySQL
Set the following in `config.yml`:
```yaml
database:
  type: mysql
  host: localhost
  port: 3306
  database: lifesteal
  username: user
  password: pass
  pool-size: 10
```
Ensure your MySQL server is running and the user has `CREATE`/`SELECT`/`INSERT`/`UPDATE`/`DELETE` privileges. HikariCP manages connection pooling automatically.

## Developer API

If you are developing another plugin and want to interact with Lifesteal+:

```java
// Access the API
LifestealApi api = JavaPlugin.getPlugin(LifestealApi.class);

// Get hearts
int hearts = api.getHearts(Player player);

// Set hearts
api.setHearts(Player player, int hearts);

// Get archetype
String archetype = api.getArchetype(Player player);

// Set archetype
api.setArchetype(Player player, String archetypeId);

// Listen for events
@EventHandler
public void onHeartChange(HeartChangeEvent event) {
    Player player = event.getPlayer();
    int oldHearts = event.getOldHearts();
    int newHearts = event.getNewHearts();
}
```

All API classes are shaded and available in your build classpath.

## Building

```bash
./gradlew shadowJar
```

Output: `build/libs/Lifesteal+.jar`

## License

MIT License — see [LICENSE](LICENSE) for full text.

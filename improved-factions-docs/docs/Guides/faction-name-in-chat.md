# Adding Faction Names to Player Chat Messages

This guide explains how to display a player's faction name in chat using **Improved Factions**, **PlaceholderAPI (PAPI)**, and a compatible **chat formatting plugin**.

> ⚠️ **Plugin Behavior Changes Over Time**
> This guide is written based on the most common plugin setups as of now. Plugins like **ChatInjector** have changed behavior between versions. Always double-check plugin configs, changelogs, and documentation when setting up.

---

## Requirements

1. [Improved Factions](https://modrinth.com/plugin/improved-factions)
2. [PlaceholderAPI (PAPI)](https://www.spigotmc.org/resources/placeholderapi.6245/)
3. A chat formatting plugin (e.g., EssentialsX or LPC):

   * EssentialsX users **must** use **ChatInjector**:

     * For Minecraft 1.20+, use [ChatInjector 1.20+ (v1.2+)](https://www.spigotmc.org/resources/chatinjector-1-20.81201/)
     * For older versions, use legacy ChatInjector versions

---

## Steps

### 1. Install the Plugins

* Add **Improved Factions**, **PAPI**, and a chat formatter (e.g., EssentialsX or LPC) to your server’s `plugins` folder.
* Start the server to generate the configuration files.

---

### 2. Add PlaceholderAPI Placeholders

* Improved Factions provides placeholders such as `%faction_name%`, `%faction_rank%`, etc.
* These are auto-registered—check your server logs to confirm.

---

### 3. Configure Chat Formatting

#### EssentialsX (With ChatInjector)

EssentialsX alone doesn't support PAPI placeholders. Use **ChatInjector**, but note version differences:

##### ➤ **ChatInjector < v1.2 (legacy versions)**

* Uses `{}` for placeholders (not `%`)
* No additional dependencies required

```yaml
# Essentials/config.yml
format: "{DISPLAYNAME} [&a{faction_name}&r] >> {MESSAGE}"
```

##### ➤ **ChatInjector ≥ v1.2 (for 1.20+ servers)**

* Uses standard `%%` placeholders like `%faction_name%`
* Requires **ProtocolLib** to function

```yaml
# Essentials/config.yml
format: "%displayname% [&a%faction_name%&r] >> %message%"
```

> ✅ Be sure to install [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) for versions 1.2+!

---

### LPC Chat Formatter

LPC is a lightweight standalone chat formatting plugin that works well with PlaceholderAPI and Improved Factions.
You can download it here:
🔗 [LPC Chat Formatter (1.7.10 – 1.21.4)](https://www.spigotmc.org/resources/lpc-chat-formatter-1-7-10-1-21-4.68965/)

* Supports `%` placeholders natively.
* Very easy to configure for custom chat styles.

```yaml
# LPC/config.yml
chat-format: "[%faction_name%] {name} >> {message}"
```

> 📝 **Tip:** If you're using LPC, you don't need ChatInjector or ProtocolLib—it's a simpler solution if you're not tied to EssentialsX's formatting.

---

### 4. Handling Factionless Players

By default, `%faction_name%` may return an empty string. To handle this cleanly:

```yaml
# ImprovedFactions/config.yml
default-placeholders:
  name: "No Faction"
```

---

### 5. Add Colors

Use `&` codes for coloring:

```yaml
format: "%player% [&a%faction_name%&r] >> %message%"
```

---

### 6. Test Your Setup

* Join a faction with `/f create <name>`
* Check if chat displays `[FactionName]`
* Leave a faction and confirm it displays "No Faction"

---

## Troubleshooting

* **Placeholder not showing?**
  Run `/papi parse me %faction_name%` to test placeholder output.
* **Factionless value missing?**
  Add `default-placeholders` in the Improved Factions config.
* **Essentials config missing `format:`?**
  Consider switching to **LPC**, which provides clearer formatting options.

---

For help, visit the [Improved Factions Discord](https://discord.com/invite/VmSbFNZejz) or open an issue on
[GitHub](https://github.com/ToberoCat/ImprovedFactions).

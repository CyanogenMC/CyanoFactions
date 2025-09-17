<h1 align="center">
  <img src="projectInfo/banner.png" alt="CyanoFactions Banner" width="850">
</h1>
  
<div align="center">

[![CodeFactor](https://www.codefactor.io/repository/github/CyanogenMC/cyanofactions/badge/main?style=for-the-badge)](https://www.codefactor.io/repository/github/CyanogenMC/cyanofactions/overview/main)
[![License](https://img.shields.io/github/license/CyanogenMC/cyanofactions?style=for-the-badge)](https://github.com/CyanogenMC/cyanofactions/blob/main/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/CyanogenMC/cyanofactions?style=for-the-badge)](https://github.com/CyanogenMC/cyanofactions/issues)
[![GitHub Release](https://img.shields.io/github/release/CyanogenMC/cyanofactions?include_prereleases&style=for-the-badge)](https://github.com/CyanogenMC/cyanofactions/releases/latest)

**A lightweight factions plugin with a highly vanilla experience.**

A fork of ToberoCat's [ImprovedFactions](https://github.com/ToberoCat/ImprovedFactions) with extra features and fixes.

</div>

## 📖 Table of Contents

- [📖 Table of Contents](#-table-of-contents)
- [🔗 Links](#-links)
- [🔧 Prerequisites](#-prerequisites)
- [🛠️ Installation](#️-installation)
- [📌 Placeholders](#-placeholders)

## 🔗 Links

- [Releases](https://github.com/CyanogenMC/CyanoFactions/releases)
- [Modrinth](https://modrinth.com/plugin/cyanofactions)
- [Dev Builds](https://github.com/CyanogenMC/CyanoFactions/actions/workflows/build.yml)

## 🔧 Prerequisites

- A Minecraft server running Bukkit, Spigot, PaperMC (recommended) or any other fork of Paper/Spigot (untested)
- Java 17 or higher

## 🛠️ Installation
1. Download the latest release from either the GitHub releases page or Modrinth
2. Place the downloaded `CyanoFactions-[version].jar` file into your server's `plugins` folder
3. Start/restart the server to generate the configuration files

## 📌 Placeholders

This plugin has [PlaceholderAPI](https://ci.extendedclip.com/job/PlaceholderAPI/) support. Below is a list of all available placeholders:

**Base Placeholders**:

| Placeholder            | Description                                                     |
| ---------------------- | --------------------------------------------------------------- |
| `%faction_owner%`      | The owner of the player's faction                               |
| `%faction_name%`       | The name of the player's faction                                |
| `%faction_rank%`       | The rank of the player in the faction                           |
| `%faction_join_mode%`  | The join mode of the player's faction                           |
| `%faction_in_faction%` | Returns `true` if the player is in a faction, `false` otherwise |

**When `power-raids` module is enabled (default)**:

| Placeholder                            | Description                                                  |
| -------------------------------------- | ------------------------------------------------------------ |
| `%faction_power%`                      | The power of your faction                                    |
| `%faction_max_power%`                  | The maximum power of your faction                            |
| `%faction_next_power_gain%`            | The power that will be gained on the next power gain         |
| `%faction_claim_upkeep_cost%`          | The cost of maintaining your claims                          |
| `%faction_next_claim_cost%`            | The cost of claiming the next chunk                          |
| `%faction_active_accumulation%`        | The power currently being accumulated by your active members |
| `%faction_inactive_accumulation%`      | The power you currently lose due to inactive members         |
| `%faction_next_accumulation_cycle%`    | The time left until the next power accumulation cycle        |
| `%faction_next_claim_keep_cost_cycle%` | The time left until the next claim keep cost cycle           |

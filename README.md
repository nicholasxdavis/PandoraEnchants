<td style="text-align: center;">
PandoraEnchants 1.0.0 
  <br />
<br />

<a href="#"> <img src="https://img.shields.io/badge/Version-1.0.0-skyblue?style=for-the-badge" alt="Current Version"> </a>

<br /><hr />

About PandoraEnchants

PandoraEnchants is a comprehensive custom enchantment plugin designed for Factions servers running Spigot/Paper 1.21. It introduces a unique "One Enchant Per Item" mechanic to maintain strict PvP balance while offering powerful abilities. The plugin features a robust system of over 50 custom enchantments, "God Sets" that bypass limitations, and full integration with vanilla Enchanting Tables and Anvils.

Key Features

Balanced Gameplay: Enforces a strict limit of one custom enchantment per item to prevent power creep.

Unique Enchantments: Includes powerful effects like Lifesteal, Rage, Vein Miner, Lumberjack, Double Jump, and more.

God Sets: Special pre-configured armor sets (Iron, Diamond, Netherite) that bypass the one-enchant restriction, allowing for ultimate "God" gear.

GUI Editor: A live preview editor (/pe editor) to view and manage enchantments directly in-game.

Vanilla Integration:

Enchanting Table: Custom enchants can be obtained naturally through the enchanting table.

Anvil Support: Combine custom enchanted items and books with configurable costs.

Visual Effects: High-quality particle effects and sounds for immersive combat and mining.

Smart Mechanics:

Vein Miner & Lumberjack: Break entire veins of ore or trees instantly.

Double Jump: Launch into the air with special boots.

Auto Smelt & Telekinesis: Automatically smelt ores and send drops directly to inventory.

</td>

<br /><hr />

Commands
/pe add <enchant> [level] | Adds a specific custom enchantment to the held item.

/pe remove | Removes the custom enchantment from the held item.

/pe info <enchant> | View detailed information, rarity, and stats of an enchantment.

/pe list [page] | Lists all available custom enchantments.

/pe book <enchant> [level] | Spawns a custom enchanted book.

/pe godset <tier> | Spawns a full God Set (Iron, Diamond, or Netherite).

/pe godkit give <player> <tier> | Gives a specific God Set to a player.

/pe editor | Opens the GUI Editor with live preview.

/pe reload | Reloads the plugin configuration.

Dependencies
To run PandoraEnchants, your server requires the following:

Java 21 or higher.

Spigot/Paper 1.21 or compatible fork.

Configuration

The plugin is highly configurable via config.yml and enchantments.yml. You can adjust enchantment weights, costs, effects, and the "one enchant" rule logic.

Example Enchantment Configuration
From enchantments.yml
lifesteal: enabled: true display_name: "&7Lifesteal" description: "&7Heals you when you damage enemies" definition: max_level: 5 needs_permission: false anvil_cost: 15 supported: - weapon enchanting_table: weight: 1 min_cost_base: 12 max_cost_base: 26

vein_miner: enabled: true display_name: "&7Vein Miner" description: "&7Breaks connected blocks of the same type" definition: max_level: 5 supported: - tool enchanting_table: weight: 15 min_cost_base: 6

License
Copyright © 2025 Blacnova Development

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see https://www.gnu.org/licenses/.

<br /><hr />

<td style="text-align: center;">

Support & Links

Report Bugs • View Source

</td>

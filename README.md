# Solo Leveling Final

> A large-scale Forge 1.20.1 action-RPG mod project inspired by the Solo Leveling universe.

[**Download the latest release**](https://github.com/enesworks/solo-leveling-final/releases/latest) · [**View source code**](https://github.com/enesworks/solo-leveling-final/tree/main/src/main/java/dev/eness/sololevelingfinal/core)

## Overview

Solo Leveling Final is a combined Forge mod focused on campaign progression, boss encounters, custom combat systems, dungeons, visual effects, UI, and story-driven gate progression.

The project is structured as one unified mod: gameplay logic, assets, world data, client systems, networking, progression, and campaign content are maintained in the same Gradle project.

## Featured Systems

- Story campaign with persistent, save-safe progression
- Gate-based dungeon flow with safe return handling
- Custom boss encounters: Antares, Rakan, Tarnak, Sillad, Legia, Baran and more
- Cartenon Temple return encounter and Red Gate progression
- Three Monarchs arena and Antares final arena
- Custom entities, combat logic, VFX, particles, animations, HUD and UI systems
- Hunter, guild, party, dungeon, shadow and rank-related systems
- World generation, authored structures, datapack dungeons and custom dimensions
- Server-side progress ownership and one-time reward protection

## Campaign Flow

```text
Baran / Demon Castle
  → Cartenon Temple Return
  → Normal Gates
  → Ancient Keys & Legia
  → Normal Gates
  → Red Gate, Baruka & Sillad
  → Normal Gates
  → Three Monarchs: Sillad, Rakan & Tarnak
  → Normal Gates
  → Antares Final Encounter
```

## Tech Stack

| Area | Technology |
| --- | --- |
| Game | Minecraft 1.20.1 |
| Mod Loader | Forge 47.4.10 |
| Language | Java 17 |
| Build System | Gradle |
| Animation Runtime | GeckoLib |
| Integration | Mixins, datapack content, custom networking |
| Source Structure | Unified Forge mod project |

## Source Layout

```text
src/main/java/dev/eness/sololevelingfinal/core/
├── campaign/      # Story stages, gates, rewards, arena progression
├── dungeon/       # Dungeon generation and runtime systems
├── entity/        # Bosses, mobs and custom entity behaviour
├── client/        # HUD, GUI, renderers, VFX and visual systems
├── network/       # Client/server messages
├── procedures/    # Gameplay actions and triggers
├── world/         # World and dimension logic
└── ...            # Guild, party, items, mixins, commands and utilities

src/main/resources/
├── assets/        # Models, textures, animations, sounds and UI assets
├── data/          # Advancements, structures, dimensions and world data
└── META-INF/      # Forge mod metadata
```

## Installation

1. Install **Minecraft 1.20.1** and **Forge 47.4.10**.
2. Download `Solo-Leveling-Final-1.0.0.jar` from the [Releases](https://github.com/enesworks/solo-leveling-final/releases/latest) page.
3. Place the file in your Minecraft `mods` folder.
4. Launch the game with the Forge profile.

## Building From Source

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

The built mod JAR is generated in:

```text
build/libs/
```

## Project Status

Release `v1.0.0` is available. Core boss encounters, campaign systems, custom assets and the unified source project are included in this repository.

## Disclaimer

This is an unofficial fan-made development and portfolio project. Solo Leveling and related names belong to their respective rights holders. This project is not affiliated with or endorsed by the original creators or publishers.


# Particle Text

[![CI](https://github.com/Despical/ParticleText/actions/workflows/build.yml/badge.svg)](https://github.com/Despical/ParticleText/actions/workflows/build.yml)
![Java 25](https://img.shields.io/badge/Java-25-007396.svg)
![Gradle](https://img.shields.io/badge/Gradle-9.7.0-079ec0?logo=gradle&logoColor=white)
![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62b47a)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

Particle Text renders persistent, customizable text using native Minecraft particles. It is built for modern Paper servers and keeps rendering work bounded through cached point clouds, distance filtering, configurable sampling, and a single shared scheduler.

---

## Features

- Create and edit particle text without restarting the server.
- Customize text, particle, scale, font, enabled state, position, and three-axis rotation.
- Manage renderers through commands or an Inventory Framework menu.
- Use Adventure MiniMessage and hex colors in every configurable message.
- Resolve PlaceholderAPI placeholders inside renderer text and expose a native `particletext` expansion.
- Cache configuration values and rebuild runtime state with `/pt reload`.
- Limit particles by viewer distance, pixel sampling, and a per-renderer point cap.
- Persist renderer data in a human-readable `renderers.yml` file.

---

## Requirements

- Java 25
- A Paper-compatible server
- PlaceholderAPI (optional)

---

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/pt create <id> <text>` | Create a renderer using cached defaults | `particletext.command.create` |
| `/pt delete <id>` | Delete a renderer | `particletext.command.delete` |
| `/pt list` | List renderer IDs | `particletext.command.list` |
| `/pt menu` | Open the renderer management menu | `particletext.command.menu` |
| `/pt teleport <id>` | Teleport to a renderer | `particletext.command.teleport` |
| `/pt tphere <id>` | Move a renderer to your location | `particletext.command.teleport` |
| `/pt text <id> <text>` | Change displayed text | `particletext.command.edit` |
| `/pt setsize <id> <scale>` | Change renderer scale | `particletext.command.edit` |
| `/pt font <id> <name> <style> <size>` | Change the AWT font | `particletext.command.edit` |
| `/pt particle <id> <particle>` | Change the data-free particle type | `particletext.command.edit` |
| `/pt enabled <id> <true\|false>` | Enable or disable rendering | `particletext.command.edit` |
| `/pt inverted <id> <true\|false>` | Toggle foreground/background pixels | `particletext.command.edit` |
| `/pt rotate <id> <x\|y\|z> <angle>` | Set an axis rotation | `particletext.command.edit` |
| `/pt reload` | Reload all cached files and the render task | `particletext.command.reload` |
| `/pt help` | Show command help | `particletext.command.help` |
| `/pt version` | Show the installed version | `particletext.command.version` |

`/particletext` is available as an alias. The `particletext.admin` permission grants every administrative permission.

---

## Configuration

- `config.yml` contains cached performance limits, renderer defaults, and menu settings.
- `messages.yml` contains MiniMessage-compatible messages and menu text.
- `renderers.yml` stores renderer-specific text, particle, font, transform, state, and location data.

Changes made directly to these files take effect after `/pt reload`. Existing renderers keep their own font, scale, particle, and state when defaults change; the defaults apply to newly created renderers.

---

## PlaceholderAPI

Renderer text may contain installed PlaceholderAPI placeholders. Global placeholders are refreshed according to `performance.placeholder-refresh-ticks` and the point cloud is rebuilt only when the resolved text changes.

Particle Text provides:

- `%particletext_total%`
- `%particletext_enabled%`
- `%particletext_disabled%`
- `%particletext_nearest_id%`
- `%particletext_nearest_text%`
- `%particletext_nearest_distance%`
- `%particletext_renderer:<id>:text%`
- `%particletext_renderer:<id>:particle%`
- `%particletext_renderer:<id>:scale%`
- `%particletext_renderer:<id>:enabled%`
- `%particletext_renderer:<id>:inverted%`
- `%particletext_renderer:<id>:world%`

---

## Building

Clone the repository and run:

```bash
./gradlew build
```

On Windows:

```cmd
gradlew.bat build
```

The packaged plugin is created at `build/libs/particle-text-2.0.0.jar`.

---

## Contributing and security

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request and follow the [Code of Conduct](CODE_OF_CONDUCT.md). Report vulnerabilities privately as described in [SECURITY.md](SECURITY.md).

---

## License

Particle Text is licensed under the [GNU General Public License v3.0](LICENSE).

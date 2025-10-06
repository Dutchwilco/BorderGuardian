# BorderGuardian

A comprehensive world border plugin that allows administrators to set, manage, and animate world borders with customizable damage and visual effects for players who venture beyond the safe zone.

## Features

- **Square Border System**: Set borders based on X/Z coordinates (not circular)
- **Dynamic Border Control**: Set, expand, and shrink borders with configurable speeds
- **Persistent Configuration**: Border settings save automatically and persist through restarts
- **Player Protection**: Customizable damage and effects for players outside the border
- **Visual Feedback**: Particle effects and action bar warnings
- **Flexible Speed Control**: Adjust border animation speed in real-time

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/border set <radius>` | Set the border radius | `borderguardian.admin` |
| `/border expand <amount> [duration]` | Expand the border | `borderguardian.admin` |
| `/border shrink <amount> [duration]` | Shrink the border | `borderguardian.admin` |
| `/border speed <speed>` | Set border animation speed (blocks/second) | `borderguardian.admin` |
| `/border info` | Display current border information | `borderguardian.admin` |
| `/border reload` | Reload configuration files | `borderguardian.admin` |

### Command Examples

```
/border set 2500          # Set border to 2500 blocks (allows X:2500 or Z:2500)
/border expand 500 60     # Expand border by 500 blocks over 60 seconds
/border shrink 1000 120   # Shrink border by 1000 blocks over 120 seconds
/border speed 0.3333      # Set speed to 1 block per 3 seconds
/border info              # Show current radius and speed
```

## Permissions

- `borderguardian.admin` - Access to all border commands
- `borderguardian.bypass` - Bypass border damage and effects

## Configuration

### config.yml

```yaml
border:
  default-radius: 1000        # Default border radius on first start
  animation-speed: 1.0        # Blocks per second for animations
  center-x: 0                 # Border center X coordinate
  center-z: 0                 # Border center Z coordinate

damage:
  enabled: true               # Enable damage for players outside border
  amount: 1.0                 # Damage amount per interval
  interval: 20                # Ticks between damage (20 = 1 second)

effects:
  enabled: true               # Enable potion effects outside border
  wither-level: 1             # Wither effect level (0 = disabled)
  slowness-level: 2           # Slowness effect level (0 = disabled)
  nausea-enabled: true        # Enable nausea effect

particles:
  enabled: true               # Show particle effects at border
  type: REDSTONE              # Particle type
  density: 5                  # Particles per check

warnings:
  action-bar: true            # Show action bar warnings
  sound: true                 # Play warning sounds
  sound-type: BLOCK_NOTE_BLOCK_PLING
  sound-volume: 1.0
  sound-pitch: 0.5
```

### messages.yml

Fully customizable messages with color code support (`&` codes):

```yaml
border-set: "&aWorld border set to {radius} blocks!"
border-expanded: "&aWorld border expanding by {amount} blocks over {duration} seconds!"
border-shrunk: "&cWorld border shrinking by {amount} blocks over {duration} seconds!"
border-speed-set: "&aBorder animation speed set to {speed} blocks per second!"
outside-border: "&c&lWARNING: You are outside the world border!"
config-reloaded: "&aConfiguration reloaded successfully!"
```

## How It Works

### Square Border System

Unlike circular borders, BorderGuardian uses a **square calculation**:
- `/border set 2000` allows you to go to positions like (2000, 2000) or (-2000, 2000)
- Border checks if **either** X or Z coordinate exceeds the radius
- More intuitive for Minecraft's grid-based world

### Border Animations

When using expand/shrink commands:
1. Target radius is calculated
2. Border changes gradually based on speed setting
3. Current radius is saved to config when animation completes
4. Settings persist through server restarts

### Speed Control

The speed parameter controls how fast the border moves:
- Speed is measured in **blocks per second**
- Example: `0.3333` = 1 block per 3 seconds
- Speed is saved and used for all future animations

## Installation

1. Download the latest `BorderGuardian.jar`
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure settings in `plugins/BorderGuardian/config.yml`
5. Customize messages in `plugins/BorderGuardian/messages.yml`

## Requirements

- Minecraft 1.20.4+
- Java 17+
- Spigot or Paper server

## Support

For issues, suggestions, or questions, please contact me on Discord: Dutchwilco

## Author

Created by **Dutchwilco**

---

**Note**: All border settings (radius, speed, center) are automatically saved to config.yml and persist through server restarts.

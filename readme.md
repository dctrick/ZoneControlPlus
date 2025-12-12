# ZoneControlPlus Features

ZoneControlPlus is a powerful region management plugin designed to enhance PvP and survival experiences using WorldGuard regions.

## Main Features

### 1. Restricted "Cage" Regions (Damage-On-Exit)
Keep players trapped inside specific arenas or zones. If a player **actively leaves** a designated region, they are marked as a violator and will take continuous damage until they return. This prevents players from running away from fights in PvP zones.
*   **Stateful Tracking**: Damage only starts if you *leave* the region. Standing near the border is safe.
*   **Continuous Damage**: You cannot outrun the damage. It persists until you return.
*   **Pushback**: Optionally knocks the player back towards the region.

### 2. Advanced Region Protection
Granular control over block interactions and placement. Prevent players from verifying protecting their bases or arenas while still allowing specific gameplay elements.
*   **Undestroyable/Unplaceable Blocks**: Whitelist specific blocks that cannot be broken or placed.
*   **Blocked Interactions**: Prevent interaction with specific blocks (e.g., chests, doors) in fight zones.
*   **Enderpearl Control**: Stop players from pearling into or out of regions.

---

## Additional Features & Configuration

### Auto-Heal Zones
Automatically regenerate player health while they are inside safe zones or lobbies.

```yaml
    auto-heal:
      enabled: true
      hearts-per-5-sec: 1.0  # Amount of hearts to heal
      max-health: 20         # Heal up to this amount
```

### Elytra Control
Disable elytra usage in specific regions (e.g., PvP arenas) to force ground combat. Gliding is cancelled immediately upon trying to use it or entering the region.

```yaml
    elytra-disabled: true    # Set to true to disable elytra flying
```

### Potion Control
Manage which potion effects are allowed. Block specific potions from being consumed or clear effects when entering a region.

```yaml
    potion-control:
      blocked:               # List of blocked potions
      - INVISIBILITY
      clear-on-enter:        # Effects to clear when entering
      - SPEED
      give-on-enter:         # Effects to give when entering
        SPEED:
          duration: 200      # Ticks
          amplifier: 1
```

### Enderpearl Restrictions
Prevent players from escaping or entering regions using enderpearls.

```yaml
    no-pearl: false          # Disable pearling entirely in this region
    no-outpearl: true        # Prevent pearling from INSIDE to OUTSIDE
```

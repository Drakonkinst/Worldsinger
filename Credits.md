# Credits

## Development Team

* **Drakonkinst**: Project Lead, Programmer

### Art Team

* **FurbyMom22**: 2D Artist
    * Aether Spore Block Texture

### Other Textures

* u/SeiyoNoShogun
    * Steel Block texture
* _trbz (SimplySteel)
    * Steel Ingot
    * Crude Iron
    * Steel Armor
    * Steel Tools
    * Flint and Steel Variants
* mortuusars (The Salt)
    * Salted Food Overlay
    * Salt Ore

## Attributions

Best effort has been made to properly attribute software from external sources. In addition,
credit is given to some referenced mods even if not strictly required by their license.

Please raise an issue if you feel something is not credited properly.
See `vendor/ThirdPartyNotices.txt` for
additional credits, including licenses and legal notices.

### Referenced Mods

Below is a comprehensive list of all mods used as a reference, no matter how small, during
development.
A description of how the reference was used and significant changes in our mod are included.

* **Fluidlogged 1.2.1** by **Leximon**
  ([GitHub](https://github.com/Leximon/Fluidlogged/tree/v1-1.20), [Modrinth](https://modrinth.com/mod/fluidlogged))
    * Implementation for allowing waterloggable blocks to be fluidloggable in multiple fluids
    * Changes
        * Ported from Fabric 1.19.2
        * Rewrote almost all mixins to increase compatibility
* **Simply Steel 2.2.0** by **trbz_**
  ([GitHub](https://github.com/ethanhmaness/Simply-Steel), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/simply-steel-forge))
    * Design and textures for Steel crafting, Steel Armor and Tools, Steel Anvil, and Flint
      and Steel Variants
    * No license provided, but the mod author left a message allowing any others to use the GitHub
      repository (see `vendor/SimplySteel.png`)
    * Changes
        * Rewritten from Forge 1.18.2 to Fabric
* **Freelook for Clients 1.0.0** by **Pixelstormer**
  ([GitHub](https://github.com/Pixelstormer/freelook_for_clients/tree/dev), [Modrinth](https://modrinth.com/mod/freelook-for-clients))
    * Implementation for allowing players to look around without moving their player model, used for
      Midnight Creature Possession
    * Changes
        * Specialized for implementation of an entirely different feature, does not offer Freelook
          functionality
* **Identity 2.1.0** by **Draylar**
  ([GitHub](https://github.com/Draylar/identity), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/identity))
    * Implementation of rendering one entity as another, used for Midnight Creatures and other
      shapeshifting mobs
    * Changes
        * Modified to make shapeshifting mobs appear as other mobs, rather than changing the
          player's appearance.
* Cardinal Components (to be added)
* The Salt (to be added)

### Library Mods

*TBA*

### Inspiration

Although no source code nor assets were taken from these mods, we still wish to credit them as
inspirations for our mod and its features.

* **It's Thirst** by **its0v3r**
  ([GitHub](https://github.com/its0v3r/Its-Thirst/tree/1.19.3), [Modrinth](https://modrinth.com/mod/its-thirst))
    * Design for a lightweight thirst system that integrates well with vanilla Minecraft.
    * No source code or assets are taken from this mod to respect its LGPL license
    * In addition, the mod author gave their personal blessing to use the design, source code,
      and assets

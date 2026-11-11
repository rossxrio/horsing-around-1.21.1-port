# Horsing Around for NeoForge 1.21.1

A standalone NeoForge 1.21.1 build of [Horsing Around](https://github.com/GabePurc/horsing-around) by GabePurc
(gaits, momentum, stamina, sure footing, an over-the-shoulder riding camera, the horse's own look-ahead, rider and leg
posing). Licensed LGPL-3.0 like the original (`COPYING`, `COPYING.LESSER`).

## How it relates to upstream

Upstream builds every Minecraft version from one `src/` with [Stonecutter](https://stonecutter.kikugie.dev/); its
`1.21.1-neoforge` node is the NeoForge 1.21.1 build. This repo is that node, flattened: Stonecutter's output for
1.21.1-neoforge with the `//?` markers and other versions' code removed, and the Fabric, Forge and Realistic Horse
Genetics (Forge-only) files left out, as upstream's NeoForge build leaves them out.

Behaviour matches upstream 0.7.0 exactly: every class in this jar disassembles (`javap -c -p`) to the same bytecode
as upstream's `horsing-around-neoforge-0.7.0+1.21.1.jar`, apart from line numbers. The jar holds the same files.

## Build and run

JDK 21 (Gradle finds an installed one or downloads it).

- `./gradlew build`: jar in `build/libs/`.
- `./gradlew runClient` / `runServer`: dev games (in `run/`). They load Entity Model Features and Entity Texture
  Features, as upstream's dev games do; `-Pplain` leaves them out.
- `./gradlew auditMixins`: a dev client and a dedicated server load every class the mixins patch, then quit; it fails
  if any injection misses.

Optional integrations, compiled against only: Entity Model Features / Entity Texture Features, Shoulder Surfing
Reloaded, Icy's Better Horses (by reflection and mixin plugins).

## Syncing a newer upstream version

1. In a clone of upstream: `./gradlew :1.21.1-neoforge:jar` (writes
   `versions/1.21.1-neoforge/build/generated/stonecutter/{main,client}`).
2. `python -I scripts/strip_stonecutter.py <generated>/main <out>/main` and the same for `client`. It prints any branch
   it couldn't classify and every file that was inactive for this version.
3. Replace `src/` with the output, then delete `dev/horsingaround/{fabric,forge}`, `client/{fabric,forge}`,
   `client/mixin/rhg`, `fabric.mod.json`, `META-INF/mods.toml` and `horsingaround.rhg.client.mixins.json`. Take the
   mixin configs and `neoforge.mods.toml` from upstream's built jar (they're filled in there), with
   `version = "${version}"` in the toml.
4. Bump `mod_version` (and dependency versions from upstream's `versions/1.21.1-neoforge/gradle.properties`), build,
   compare against upstream's jar, run `auditMixins`.

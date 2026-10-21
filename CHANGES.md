# Changes from the original Split Self

Comparison base: Pryzmm's Fabric project at
Minecraft 1.21.1 / Java 21.
This branch targets Minecraft 1.20.1 / Forge 47.2.20 / Java 17.
The changes below describe implemented code and platform adaptations. They are
not a claim of complete gameplay parity or completed client acceptance testing.

## Platform and build

- Replaced Fabric Loom, Fabric entry points, registrations, events and networking
  with ForgeGradle, Forge registries/events, Forge configuration and Forge packets.
  Adapted mixins, rendering hooks, entities, blocks, world generation and APIs to
  Minecraft 1.20.1. Fabric API is no longer a requirement.
- Added a standalone Gradle Wrapper build at the repository root, explicit SAFE
  and UNSAFE build properties, edition markers and edition-named artifacts.
- Bundled the Windows x86-64 JavaCV/JavaCPP/FFmpeg runtime in the all-in-one JAR
  instead of using the original Fabric video integration. The transition MP4 is
  packaged locally. JavaCV's optional camera module descriptors are excluded.
- Retained the original Discord RPC library. Packaged the desktop applications
  into a separate embedded `desktop-apps.jar` for launching outside Forge's class
  loader. These integrations are existing mod features adapted for Forge.

## Added controls and recovery

- Added a local permission layer for external effects, personal information,
  microphone use and computer interactions. Servers cannot enable these client
  permissions. User overrides remain effective in both editions.
- SAFE defaults all four permissions to OFF and shows the consent flow. UNSAFE
  defaults them to ON and also shows Information with a personal-data toggle. The
  editions use separate client configuration files. Reopening Information keeps
  the current settings.
- Ported the original SAFE/UNSAFE Information warning paragraphs, colors,
  Continue / Toggle PII buttons, pause behavior and blocked Escape dismissal.
  PII is the only setting on this screen. Other local permissions remain in the
  config/client commands. Narrow screens wrap/scroll the original paragraphs.
- Matched the original 1.0.2 SAFE's fixed exclusions: DOYOUSEEME, COMMAND, EJECT,
  FREEDOM and the shared Notepad typing helper. The helper restriction also
  applies to THEOTHERSCREENSHOT's dialogue window; its screenshot remains.
  Permission switches cannot re-enable these actions in SAFE. Other retained
  integrations still follow the local permission gates; see the README table.
- Split those executable implementations into `src/unsafe/java` and their
  scripts/assets into `src/unsafe/resources`. SAFE compiles an empty counterpart
  from `src/safe/java`. Its main JAR and embedded desktop application JAR omit
  COMMAND, drive ejection, wallpaper/FREEDOM and the Notepad typing window code.
  Separate edition work directories prevent stale UNSAFE classes/resources from
  leaking into a subsequent SAFE build. Catalog names and documentation remain.
  See the README for the exact gates and the MEMORY application exception.
- Added persisted transition intent and recovery/session handling, including
  retries, heartbeat/timeout handling and cancellation of stale callbacks after
  a disconnect or a new session. These are changes to failure recovery.
- Added operator diagnostic commands for individual events, transitions, video,
  finale scenes and speech-text processing. Diagnostic text processing can run
  without activating the microphone.

## Changed implementations and observable differences

- Expanded **COMMAND** on Windows: the original only opened a terminal. This
  port runs a fixed PowerShell display script in that terminal and prints the
  detected running browser names, the local user name, Windows' reported
  Internet connectivity, a `Minecraft 1.20.1 (Split Self)` game label, and the
  message `I will help you remember` for an English Windows UI (also the fallback for
  other UI languages). The message follows Windows' UI culture, independently
  of Minecraft's selected language. Browser detection checks running processes
  against a fixed list (Chrome, Edge, Firefox, Brave, Opera and other).
  The user name is replaced with the localized redacted value when personal
  information permission is OFF. COMMAND requires effects and computer
  interaction permissions in UNSAFE and is permanently disabled in SAFE.
- Replaced Fabric Shriek speech integration with offline Windows System.Speech.
  It requires an installed system recognizer and the microphone permission gates.
- Replaced the original animated settings/language panels with native Minecraft
  widgets wired through Forge. Retained the backup confirmation instead of
  automatically accepting experimental/backup warnings.
- Implemented FREEZE using a captured still frame instead of deliberately
  stalling the CPU. Screen capture uses the normal renderer; image handling has
  size limits, and the captured-screen watchdog releases the frame after timeout.
- Migrated world state to Forge NBT saved data with a legacy `data/splitself.json`
  importer. Import preserves the old JSON file and runs only when NBT state is
  absent; it does not make Fabric and Forge saves generally interchangeable.
- Adapted structure placement to Minecraft 1.20.1 pieces/chunks and relocated
  templates to `data/splitself/structures`. Converted the six `es_*` templates'
  item data and DataVersion for 1.20.1. Destructive world effects operate on
  loaded chunks; zero-delay recursive scheduling is bounded to at least one tick.

## Ported content

The implementation includes handlers for all 67 event names in the checked-in
catalog, story/dialogue and world state, dimensions and structures, The Other /
The Forgotten, image frames, finale/exit-door scenes, panorama transitions,
desktop applications and external integrations. These are ports of original
content, rather than 67 newly invented events. Handler coverage does not prove
that every scene, visual effect or multiplayer path behaves identically.

## Developer additions

- Added 15 isolated probes integrated with `check`, covering edition identity,
  permission defaults/revocation, event rules, transition recovery, dialogue,
  structure data, localization and consistency of resources with event handlers.
- Added an English README, credits, third-party license notices, an output/cache
  `.gitignore` and a Java 17 SAFE/UNSAFE CI build matrix.
- Removed dependencies on private reference files, workspace journals and feature
  tracking from the developer build and probes. Excluded one-time converters,
  delivery tooling, caches, logs and compiled mod artifacts from the source diff.

## Remaining limitations

Both editions build and pass the isolated probes. Real-client acceptance is
still needed for Forge/Mixin startup, GPU effects, audio/video, structures,
finale scenes, reconnect behavior, multiplayer and external integrations.
The packaged video natives and speech backend target Windows; cross-platform
equivalence is not established. The retained `broken_memory.nbt` still contains
1.21 item components and needs a separate compatibility fix. No game content
was changed as part of preparing the clean source branch.

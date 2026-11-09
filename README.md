# Split Self — Forge 1.20.1

Unofficial Forge port of [Split Self by Pryzmm](https://github.com/Pryzmm/Split-Self).
Targets Minecraft **1.20.1**, Forge **47.2.20**, and a **Java 17 JDK**.
The original Fabric project is preserved in this branch's Git history.

## Changes from the original

This branch moves the original Minecraft 1.21.1 / Fabric / Java 21 mod to
Minecraft 1.20.1 / Forge / Java 17. It adds edition-specific local permission
controls, transition recovery, diagnostic commands and developer probes. Video,
speech, menus, saved data and structure placement use adapted implementations.
The 67 event handlers are ports of existing content. See [CHANGES.md](CHANGES.md)
for the additions, behavior differences and remaining compatibility limits.

## Build and checks

Set `JAVA_HOME` to your Java 17 JDK, or put that JDK on `PATH`. From this directory:

```sh
./gradlew build -PsplitselfEdition=SAFE
./gradlew build -PsplitselfEdition=UNSAFE
```

On Windows use `gradlew.bat` instead of `./gradlew`. No global Gradle installation
is needed. The first build downloads Gradle and dependencies from the Gradle,
Forge, Maven Central and Minecraft repositories. Default edition: SAFE.

`build` runs the isolated probes through `check` and produces edition-named JARs
in `build/libs/`. Install the **`-all.jar`** for your chosen edition; it includes
the Discord RPC and Windows video runtime dependencies. Both JARs contain the same flattened RPC and FFmpeg runtime. Build output is ignored by Git.

For IntelliJ, import this directory as a Gradle project using JDK 17, then run
`./gradlew genIntellijRuns -PsplitselfEdition=SAFE` and refresh Gradle before
selecting the generated `runClient` configuration. Use Gradle build/run delegation
so `prepareRuntimeLibraries` runs before launch. Regenerate configurations after
switching editions. `./gradlew runClient -PsplitselfEdition=SAFE` uses the same
runtime without IntelliJ. The dependency probe loads RPC and the native FFmpeg
libraries without opening Minecraft or connecting to Discord:
`./gradlew runtimeDependenciesProbe -PsplitselfEdition=SAFE`.

Run `./gradlew probe -PsplitselfEdition=SAFE` for probes alone. They check edition
identity, permission defaults and revocation, event rules, transition recovery,
dialogue, structure NBT, localization and resource/catalog consistency. They do
not launch Minecraft, a server, desktop applications, or microphone recognition.

Then you need to be sure [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) version 47.4.23 (recommend) or 47.2.20 are downloaded to Minecraft version 1.20.1

## Editions and permissions

Both editions have mod ID `splitself`: **install exactly one**.

| Setting | SAFE default | UNSAFE default |
| --- | --- | --- |
| `externalEffects` | false | true |
| `personalInformation` | false | true |
| `microphone` | false | true |
| `computerInteractions` | false | true |

Local permission files are `config/splitself-safe-client.toml` and
`config/splitself-unsafe-client.toml`. A server cannot enable these local settings.
User overrides remain effective in both editions. **Both editions show the
Information screen**, also available through `/splitself information`. It follows
the original warning layout with **Continue** and **Toggle PII**: its only setting
is whether personal information may be shown. It does not expose effects,
computer-interaction or microphone switches. Those existing permissions remain
in the local config and client commands. UNSAFE starts with all four permissions
enabled on a fresh config; reopening Information does not reset the user's
choices. SAFE/UNSAFE use their respective original warning text and a colored
PII status. Gameplay settings use Forge's common config separately.

Computer actions require both `externalEffects` and `computerInteractions`.
Personal information actions additionally require `personalInformation`.
Microphone recognition requires `externalEffects`, `personalInformation` and
`microphone`. The MEMORY application is an explicit exception to the computer
interaction switch and requires `externalEffects` alone. Desktop effects can
create files, change the clipboard/wallpaper, open applications and use network
services. Review the settings before using UNSAFE.

### Feature availability

SAFE follows the functional removals in the original **1.0.2 SAFE** release.
The following restrictions are fixed by the build edition: enabling permissions
in SAFE cannot restore the removed actions.

The excluded implementations and their scripts/assets live in `src/unsafe` and
are compiled and packaged **only for UNSAFE**. SAFE uses the empty implementation
in `src/safe`; its outer JAR and embedded `desktop-apps.jar` omit the command
script, drive-ejection code, wallpaper/overlay code and typing-window class.
The event names, translations and documentation remain for compatibility and
disclosure. Other integrations listed below remain in SAFE with permission gates.
Edition work directories are separate (`build/safe` and `build/unsafe`); final
distribution JARs for both editions remain in `build/libs`.
`check` recursively audits the plain/all JARs and embedded JARs for excluded SAFE
implementations, and verifies that the UNSAFE payload is present. UNSAFE also runs
an extra probe for drive-command rules and COMMAND script construction without
starting any process. These checks establish artifact contents, not approval by
a distribution platform.

| Feature | SAFE | UNSAFE |
| --- | --- | --- |
| Information screen (Continue / Toggle PII only) | Available; PII default OFF | Available; PII default ON |
| `DOYOUSEEME` wallpaper change | Disabled by edition | Available with effects + computer interactions |
| `COMMAND` terminal event | Disabled by edition | Windows terminal with browser/user/connectivity/game information; requires effects + computer interactions; user name redacted when personal information is OFF |
| `EJECT` optical-drive action | Disabled by edition | Available with effects + computer interactions |
| `FREEDOM` desktop overlay | Disabled by edition | Available with effects + computer interactions; supported OS required |
| `NOTEPAD` typing window | Disabled by edition | Available with effects + computer interactions |
| Typing window accompanying `THEOTHERSCREENSHOT` | Disabled by edition; screenshot portion retained | Available with effects + computer interactions |
| Other original desktop/file/clipboard/browser/RPC integrations | Retained, subject to local permissions | Retained, subject to local permissions |
| MEMORY application | Requires effects; exempt from computer interactions toggle | Same requirement |
| Personal information and microphone | Available only with the required local permissions | Same requirements; fresh defaults ON |
| In-game story, structures, world-changing events and visuals | Retained, subject to gameplay settings | Retained, subject to gameplay settings |

The original SAFE still contains other desktop and network integrations; this
port keeps those behind the permission switches. SAFE does not mean that all
external integrations or world-changing horror effects have been removed.
The catalog retains all 67 handler names, including the edition-disabled events,
matching the original SAFE's no-op behavior. Availability means implemented code,
not completed gameplay or platform acceptance testing.

## Install

Install Forge 47.2.20 for Minecraft 1.20.1 with Java 17. Close Minecraft, remove
any other Split Self edition, and copy the chosen `-all.jar` into the instance's
`mods` folder. Restart the client. Fabric API and the original Fabric mod are not
required. Back up saves before playing; world-changing horror events are part of
the mod. Use a separate test world for acceptance testing.

## Known limitations

This is a development port. Builds and offline probes do not establish gameplay
parity. Forge/Mixin startup, rendering, audio/video, world structures, the finale,
multiplayer and external actions still need acceptance testing in a real client.
The bundled video natives target Windows x86-64. Offline speech uses Windows
System.Speech and requires an installed recognizer; desktop integrations and
external services depend on the host OS and service availability. The Fabric
Shriek and video integrations have been adapted. Menus use native Forge widgets.
The retained `broken_memory.nbt` still contains 1.21 item components; its item
contents need a separate compatibility fix and in-game verification on 1.20.1.

## Credits and rights

Original mod, story, code and assets: Pryzmm and upstream contributors. Translation
credits are preserved in [CREDITS.md](CREDITS.md). This port was developed with
AI assistance. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for dependency
attribution, licenses and source links. Upstream declares `CC0-1.0` in its Fabric
metadata; that existing declaration is retained in Forge metadata. No new blanket
license is asserted for third-party components.

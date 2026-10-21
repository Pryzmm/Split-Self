# Attribution and third-party components

## Split Self

Original author: **Pryzmm**, with the upstream contributors listed in CREDITS.md.
Source: https://github.com/Pryzmm/Split-Self
Base commit: `e2c1726dbda822ac92d08ab84283ebbc736573de`.
Its `src/main/resources/fabric.mod.json` declares `CC0-1.0`; upstream has no
standalone LICENSE file at this commit. The Forge metadata preserves that
declaration without relicensing dependencies. Original source copyright comments
and asset credits are retained where present.

## Discord RPC 1.0.3

`libs/discord-rpc-1.0.3.jar` is the library used by the original mod, preserved
unchanged. Only `dev/firstdark/rpc/**` is embedded in the mod JAR; Fabric metadata
and logging configuration are omitted.

Copyright (c) 2024 HypherionSA and Contributors. MIT license, copied verbatim to
`licenses/discord-rpc-MIT.txt` from:
https://github.com/firstdarkdev/discord-rpc/blob/master/license.txt
Source: https://github.com/firstdarkdev/discord-rpc

## Video runtime

The all-in-one JAR embeds JavaCV 1.5.10, JavaCPP 1.5.10, FFmpeg JavaCPP presets
6.1.1-1.5.10, and the Windows x86-64 JavaCPP/FFmpeg native artifacts from Maven
Central. Their Java classes and native files are copied unchanged. Module
descriptors and dependency META-INF entries are excluded to avoid JavaCV's
optional camera module requirements. License texts are included separately under
`META-INF/licenses/` in the built mod.

JavaCV, JavaCPP and JavaCPP presets offer Apache-2.0 or GPL with the Classpath
exception. Their complete upstream license texts are in `licenses/`.

- https://github.com/bytedeco/javacv/tree/1.5.10
- https://github.com/bytedeco/javacpp/tree/1.5.10
- https://github.com/bytedeco/javacpp-presets/tree/1.5.10/ffmpeg

FFmpeg has its own license and optional codec dependencies; see the verbatim
FFmpeg `LICENSE.md` and LGPL/GPL texts in `licenses/`. The native artifact used
here is the standard `windows-x86_64` build, not the `-gpl` variant. The JavaCPP
build recipe and linked component versions are available at:
https://github.com/bytedeco/javacpp-presets/blob/1.5.10/ffmpeg/cppbuild.sh
FFmpeg source for version 6.1.1: https://github.com/FFmpeg/FFmpeg/tree/n6.1.1
Review the complete native dependency notices and corresponding-source duties
before distributing compiled binaries; this source branch does not include
compiled mod artifacts or claim to complete a binary-release licensing audit.

## Build tooling

Gradle Wrapper 8.1.1 is provided under Apache-2.0. Its upstream LICENSE is in
`licenses/gradle-APACHE-2.0.txt`.
Source: https://github.com/gradle/gradle/tree/v8.1.1
Forge/Minecraft and other resolved build dependencies retain their own licenses;
their binaries are downloaded by Gradle rather than committed here.

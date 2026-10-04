# Split Self | Horror Mod
## Disclaimers

This mod can interact with your PC in multiple ways; this goes further into detail as to the changes made on your device. None of the changes or actions made to your device will damage it in any way. Every version of this mod will not do anything not mentioned outside of this list.

> [!IMPORTANT]
> As of the latest updates, some PC interaction events may not happen, due to only Windows and (mostly) Mac OS support being currently implemented. Please create an issue if you experience something abnormal, or a pull request if you have a fix.

> [!NOTE]
> Personally Identifiable Information (PII) can and will be shown to you through this mod. This is client sided and no information will be sent to any Minecraft multiplayer server or other players. If you wish for this information to be hidden, you can toggle it off by the warning screen shown when you first join a world. You can also do `/splitself information` to show the warning screen again and toggle it off from there. **PII is toggled OFF by default.**

You are highly encouraged to play without reading this, as it may ruin the experience. But if you worry that damage may be done, well, it is open source after all. :D



With all that being said, here are the events that interact with your computer in some way or another:
- When clicking 'Continue' on the warning screen, a `begin.txt` file will be created on your desktop
- The 'PoemScreen' event will say your device name. *This can potentially be your name*
- The 'DoYouSeeMe' event will change your background wallpaper to a distorted pre-made screenshot
- The 'Notepad' event will open a PowerShell notepad to talk to you outside of the game, stating your device name
- The 'TheOtherScreenshot' event will do the same as the 'Notepad' event, but also open a newly created image
- The 'Command' event will open the system's command prompt, but it doesn't execute anything and is meant to merely scare the player
- For some events, the mod will sometimes read the user's system name out to them
  - For some Youtubers, the mod will instead call them by their public name, or a variant of their name
  - Events that do this are namely: PoemScreen, Notepad, Mine
- The 'Emergency' event will reveal your city via `GeoIP`. VPNs will counteract this.
- The 'Freedom' event will temporarily cover your entire screen and say "Let Me Free."
- The 'Shrink' event will take you out of fullscreen, shrink your game slowly and start shaking.
- The 'Frame' event will look through your default Minecraft folder's screenshots, before looking through the current instance's screenshots.
- The 'Name' event will pull *some* user's previous Minecraft name history through `api.ashcon.app`
- The 'Logs' event will create a `latest.log` file onto your desktop
- The 'Eject' event will open your optical drive, if found and available
- The 'Memory' and 'Reminder' events will open a seperate custom application
- The 'Weather' event will state your city's weather
- The 'Memories' and 'Morse' events will create an image file on your desktop
- The 'Clipboard' event will add something to your clipboard history
- The 'RPC' and 'DiscordName' event will interact with discord and may say your discord handle
- The 'PlayerData' event will create a fake player data file on your desktop
- The 'Spotify' and 'Search' events will open their respective app or search engine
- Saying 'Hello' to The Forgotten entity will state your devices name

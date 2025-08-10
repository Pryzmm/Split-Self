# Feature Branch -- Revert Wallpaper Change

## Description:
If the mod ever changes the wallpaper, the mod will always attempt to revert the wallpaper change when the game is closing.

## Caveats
* This only works on Windows.
* Currently, the method of restoring the wallpaper is a bit hacky. This may change in the future.
* Game freezes slightly when closing (powershell scripts take a bit to run).
* Will not attempt if the user proceeded to change the wallpaper *after* the mod changed their wallpaper but *before* the game closes.
* Doesn't work if the game crashes.

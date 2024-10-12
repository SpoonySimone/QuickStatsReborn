# Mod Resources
This folder is used as a place to store images and videos about the mod

## Conversions
### gif -> mp4
- Create palette first

```ffmpeg -i input.mp4 -vf "fps=30,palettegen=max_colors=256" -y palette.png```

- Actual conversion while keeping max color depth and quality @ 30fps

```ffmpeg -i input.mp4 -i palette.png -lavfi "fps=30 [x]; [x][1:v] paletteuse=dither=floyd_steinberg" output.gif```

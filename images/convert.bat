ffmpeg -i input.mp4 -vf "fps=30,palettegen=max_colors=256" -y palette.png
ffmpeg -i input.mp4 -i palette.png -lavfi "fps=30 [x]; [x][1:v] paletteuse=dither=floyd_steinberg" output.gif
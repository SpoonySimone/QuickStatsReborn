for %%f in (*.mp4) do (
    ffmpeg -i "%%f" -vf "fps=30,palettegen=max_colors=256" -y palette.png
    ffmpeg -i "%%f" -i palette.png -lavfi "fps=30 [x]; [x][1:v] paletteuse=dither=floyd_steinberg" "%%~nf.gif"
    del palette.png
)

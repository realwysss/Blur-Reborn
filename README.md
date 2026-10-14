<p align="center"><img src="icon.png" width="128"></p>

# Blur Reborn

camera motion blur for minecraft 1.21.11. the whole picture blurs when you move the camera, your hand and the interface stay sharp.

client side fabric mod, no fabric api needed. settings through mod menu with cloth config:

- **motion blur on**: turn it off and the mod costs nothing
- **mode**: turn (default), smooth or depth
- **strength**: slider from 0% to 100% (default 50%). 0% is off, 50% looks like a normal camera, 100% is very strong

## modes

- **turn**: blurs everything on screen evenly when you turn the camera, sky included. looks like a real camera.
- **smooth**: blends every frame with the one before it, so moving mobs and particles blur too. it can show a light double image on fast turns.
- **depth**: the old one. it also blurs by distance when you walk, near blocks blur more than far ones.

## download

`blur-reborn-mc1.21.11-1.1.0.jar` on the releases page. minecraft 1.21.11, fabric loader 0.16 or newer.

## why it is light

- one fullscreen pass, 24 samples per pixel with a little noise so there is no banding, plus one copy of the frame
- when you stand still nothing is drawn (turn and depth)
- when the setting is off nothing is drawn at all
- the blur length is set by exposure time, not by frames, so it looks the same at 60 or 240 fps
- teleports, dimension changes and long pauses are ignored so you never get a smear

## pictures

same spot, mod off and on (turn mode). the camera turns at the same speed in every picture:

| off | 100% |
|---|---|
| ![turn off](screenshots/turn-off.png) | ![turn 100](screenshots/turn-100.png) |

| 50% | smooth mode |
|---|---|
| ![turn 50](screenshots/turn-50.png) | ![smooth](screenshots/smooth.png) |

walking sideways in depth mode (the wall and the ground blur by distance, the hand stays sharp):

| off | 100% |
|---|---|
| ![strafe off](screenshots/strafe-off.png) | ![strafe 100](screenshots/strafe-100.png) |

standing still, off and on look the same:

| off | on |
|---|---|
| ![still off](screenshots/still-off.png) | ![still on](screenshots/still-on.png) |

![settings](screenshots/settings.png)

## how it works

a mixin at the end of `LevelRenderer.renderLevel` (after the world, before the hand and the gui). turn mode rebuilds the view direction of every pixel and moves it back with the previous frame's rotation to get a velocity, then averages the frame along that velocity. depth mode does the same with the depth buffer and the camera position. smooth mode mixes the frame with the previous one. the blur is capped at a share of the screen so fast flicks do not turn into mush.

## tests

`./gradlew runClientGameTest` builds a wall of colored blocks, fakes a steady camera turn and a steady sideways walk, and takes screenshots in every mode. it also opens the cloth config screen and checks the config.

license: MIT

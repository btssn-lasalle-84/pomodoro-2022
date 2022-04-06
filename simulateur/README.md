# Simulateur POMODORO 2022

## Présentation du protocole implanté dans le simulateur M5

Ce document présente rapidement le fonctionnement du simulateur ainsi que le protocole implémenté. Le protocole complet est disponible dans Google Drive. Actuellement, la version du protocole est la **0.1** (04 avril 2022).

## Configuration du simulateur



## platform.ini

```ini
[env:m5stack-core-esp32]
platform = espressif32
board = m5stack-core-esp32
;board_build.partitions = huge_app.csv
board_build.partitions = no_ota.csv
debug_build_flags = -Os # optimize for size
framework = arduino
lib_deps =
  # M5Stack
  1851
build_flags=-std=gnu++11
upload_port = /dev/ttyUSB0
monitor_port = /dev/ttyUSB0
monitor_speed = 115200
```

## Auteur

- Thierry Vaira <<tvaira@free.fr>>

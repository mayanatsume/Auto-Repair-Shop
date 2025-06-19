#!/usr/bin/env bash

FX_LIB=/usr/share/openjfx/lib
MODULES=javafx.controls,javafx.fxml

# 1) Compila Java
mkdir -p bin
javac --module-path "$FX_LIB" --add-modules $MODULES \
      -d bin src/oficina/*.java src/oficina/ui/*.java

# 2) Copia recursos (CSS e imagem)
mkdir -p bin/oficina/ui
cp src/oficina/ui/styles.css bin/oficina/ui/
cp src/oficina/ui/car.png       bin/oficina/ui/

# 3) Executa JavaFX
java --module-path "$FX_LIB" --add-modules $MODULES \
     -cp bin oficina.ui.FXMain

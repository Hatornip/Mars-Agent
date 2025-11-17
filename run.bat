@echo off
echo Lancement de l'application de configuration de la simulation...

set CP=bin;jade\lib\jade.jar

java -cp "%CP%" sma.Main

echo Application terminee.

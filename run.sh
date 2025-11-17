#!/bin/bash

echo "Lancement de l'application de configuration de la simulation..."

CP="bin:jade/lib/jade.jar"

java -cp "$CP" sma.Main
echo "Application terminée."

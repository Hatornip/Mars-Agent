#!/bin/bash

echo "Nettoyage du répertoire de destination (bin)..."

mkdir -p bin

rm -f bin/sma/*.class

echo "Compilation des fichiers source Java..."

CP="jade/lib/jade.jar"

find src -name "*.java" > sources.txt

javac -d bin -cp "$CP" @sources.txt

rm sources.txt
echo "Compilation terminée avec succès. Les fichiers .class sont dans le dossier /bin."

@echo off
echo Nettoyage du repertoire de destination (bin)...

if not exist bin mkdir bin

del /Q bin\sma\*.class > nul 2>&1

echo Compilation des fichiers source Java...

set CP=jade\lib\jade.jar

javac -d bin -cp "%CP%" src\sma\*.java

echo Compilation terminee avec succes. Les fichiers .class sont dans le dossier /bin.

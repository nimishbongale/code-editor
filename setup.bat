@echo off
javac TestTerminal.java
javac -cp ".;lib/jars/*" RSTAUIDemoApp.java
echo ***** N45 Editor *****
echo Welcome! 
echo You can now use the command n45edit :folderpath: to open the N45Editor from the required folder!
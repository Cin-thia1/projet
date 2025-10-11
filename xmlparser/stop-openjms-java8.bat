@echo off
cd /d C:\Users\Lenovo\Desktop\openjms-0.7.7-beta-1
:: Définir Java 8 pour OpenJMS
set JAVA_HOME=C:\Progra~1\Java\jdk1.8.0_202
set PATH=%JAVA_HOME%\bin;%PATH%

:: Définir le dossier OpenJMS
set OPENJMS_HOME=C:\Users\Lenovo\Desktop\openjms-0.7.7-beta-1

:: Arrêter OpenJMS
%OPENJMS_HOME%\bin\openjms.bat stop

pause

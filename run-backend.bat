@echo off
title ResQ Dispatch - Spring Boot Server
echo ===============================================================================
echo    RESQ DISPATCH: STARTING SPRING BOOT REST BACKEND (Port 8080)
echo ===============================================================================
echo.

cd /d "%~dp0backend"
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Java 17 was not found. Install Eclipse Temurin 17 and set JAVA_HOME.
    pause
    exit /b 1
)

set "MAVEN_CMD=mvn"
if exist "%~dp0.tools\apache-maven-3.9.11\bin\mvn.cmd" set "MAVEN_CMD=%~dp0.tools\apache-maven-3.9.11\bin\mvn.cmd"

echo Launching Spring Boot with embedded H2 database...
echo Web Dashboard will be available at: http://localhost:8080/
echo H2 Database Console available at:  http://localhost:8080/h2-console
echo.

call "%MAVEN_CMD%" spring-boot:run
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [NOTE] Install Maven or place Apache Maven 3.9.11 in .tools\apache-maven-3.9.11.
    pause
)

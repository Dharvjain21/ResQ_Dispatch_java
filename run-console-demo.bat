@echo off
title ResQ Dispatch - Stage 1 Console Simulation Runner
echo ===============================================================================
echo    RESQ DISPATCH: SMART AMBULANCE DISPATCH ^& EMERGENCY MANAGEMENT SYSTEM
echo                      Stage 1 Core Java ^& DSA Verification
echo ===============================================================================
echo.

cd /d "%~dp0cli-demo"
if not exist "bin" mkdir bin

if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot"
if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo [ERROR] Java 17 was not found. Install Eclipse Temurin 17 and set JAVA_HOME.
    pause
    exit /b 1
)

echo [1/2] Compiling ConsoleSimulationDemo.java...
"%JAVA_HOME%\bin\javac.exe" -d bin src\com\resq\console\ConsoleSimulationDemo.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Running Demonstration Scenarios...
echo.
"%JAVA_HOME%\bin\java.exe" -cp bin com.resq.console.ConsoleSimulationDemo

echo.
echo ===============================================================================
echo Demonstration execution finished.
echo ===============================================================================
pause

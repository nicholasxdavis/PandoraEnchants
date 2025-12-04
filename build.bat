@echo off
echo ============================================
echo PandoraEnchants Build Script
echo ============================================
echo.

echo Cleaning previous build...
call mvn clean

echo.
echo Building JAR...
call mvn package

echo.
echo ============================================
echo Build Complete!
echo ============================================
echo.
echo JAR location: target\PandoraEnchants-1.0.0.jar
echo.
pause


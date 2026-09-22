@echo off
set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

java -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -cp %WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %*

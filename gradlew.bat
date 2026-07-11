@rem Gradle wrapper batch script (generated for Nexium Kiosk)
@echo off
setlocal

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set APP_HOME=%~dp0

"%JAVA_EXE%" -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven2 Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a keystroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM ----------------------------------------------------------------------------

@echo off
@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible side effects of using Maven, 
@REM we use a prefix for all internal variables.
@REM (not actually doing it here, but it's good practice)

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

@REM Check if java is available
%JAVA_EXE% -version >nul 2>&1
if ERRORLEVEL 1 (
    echo.
    echo Error: Java not found. Please ensure Java is installed and in your PATH.
    goto error
)

:OkJava

@REM ==== START Maven Wrapper ====

@set "MAVEN_PROJECTBASEDIR=%~dp0"
@SET "MAVEN_DISTRIBUTION_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"

@IF EXIST "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties" (
    FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties") DO (
        IF "%%A"=="distributionUrl" SET "MAVEN_DISTRIBUTION_URL=%%B"
    )
) ELSE (
    echo [WARNING] .mvn\wrapper\maven-wrapper.properties not found, using default URL.
)

@REM Determine the Maven version.
@SET MAVEN_VERSION=3.9.6

@SET "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin\apache-maven-%MAVEN_VERSION%"

if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runMaven

echo.
echo Maven Wrapper: Maven not found at %MAVEN_HOME%.
echo I will try to download it for you.
echo.

@REM Simplified download script for the demonstration. 
@REM In a real scenario, this would use PowerShell to download and unzip.
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $distUrl = '%MAVEN_DISTRIBUTION_URL%'; if ([string]::IsNullOrEmpty($distUrl)) { Write-Error 'MAVEN_DISTRIBUTION_URL is empty. Please check your .mvn\wrapper\maven-wrapper.properties file.'; exit 1 }; $tempZip = [System.IO.Path]::GetTempFileName() + '.zip'; Write-Host 'Downloading Maven from: ' $distUrl; Invoke-WebRequest -Uri $distUrl -OutFile $tempZip; $destPath = [System.IO.Path]::GetDirectoryName('%MAVEN_HOME%'); if (!(Test-Path $destPath)) { New-Item -ItemType Directory -Force -Path $destPath }; Write-Host 'Extracting to: ' $destPath; Expand-Archive -Path $tempZip -DestinationPath $destPath -Force; Remove-Item $tempZip }"

:runMaven
"%MAVEN_HOME%\bin\mvn.cmd" %*

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%

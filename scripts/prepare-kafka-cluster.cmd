@echo off
REM The script is developed for instructional and training purposes
REM Author: Binnur Kurt <binnur.kurt@gmail.com>
setlocal EnableExtensions EnableDelayedExpansion

set KAFKA_HOME=C:\kafka_2.13-4.3.0
set DEMO_HOME=C:\kafka
set DEMO_HOME_PROP=c:/kafka
set "JAVA_HOME=c:\DEVEL\stage\opt\jdk-26.0.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%KAFKA_HOME%"

echo Cleaning old demo data...
rmdir /s /q "%DEMO_HOME%" 2>nul
mkdir "%DEMO_HOME%\config" >nul
mkdir "%DEMO_HOME%\data" >nul

echo Creating controller configuration files...
call :writeController 3001 9093
call :writeController 3002 9094
call :writeController 3003 9095

echo Creating broker configuration files...
call :writeBroker 1 9092
call :writeBroker 2 9096
call :writeBroker 3 9097
call :writeBroker 4 9098
call :writeBroker 5 9099

echo Generating KRaft cluster UUIDs...
set CLUSTER_ID=1uXhCR_rThGsfFrC1Jezsw

REM for /f "usebackq tokens=*" %%i in (`bin\windows\kafka-storage.bat random-uuid 2^>nul`) do set "CLUSTER_ID=%%i"
for /f "usebackq tokens=*" %%i in (`bin\windows\kafka-storage.bat random-uuid 2^>nul`) do set "C3001_DIR_ID=%%i"
for /f "usebackq tokens=*" %%i in (`bin\windows\kafka-storage.bat random-uuid 2^>nul`) do set "C3002_DIR_ID=%%i"
for /f "usebackq tokens=*" %%i in (`bin\windows\kafka-storage.bat random-uuid 2^>nul`) do set "C3003_DIR_ID=%%i"

if not defined CLUSTER_ID (
  echo Could not generate CLUSTER_ID.
  exit /b 1
)

set "INITIAL_CONTROLLERS=3001@localhost:9093:%C3001_DIR_ID%,3002@localhost:9094:%C3002_DIR_ID%,3003@localhost:9095:%C3003_DIR_ID%"

echo Formatting controller storage...
call bin\windows\kafka-storage.bat format --cluster-id "%CLUSTER_ID%" --config "%DEMO_HOME%\config\controller-3001.properties" --initial-controllers "%INITIAL_CONTROLLERS%" 
call bin\windows\kafka-storage.bat format --cluster-id "%CLUSTER_ID%" --config "%DEMO_HOME%\config\controller-3002.properties" --initial-controllers "%INITIAL_CONTROLLERS%" 
call bin\windows\kafka-storage.bat format --cluster-id "%CLUSTER_ID%" --config "%DEMO_HOME%\config\controller-3003.properties" --initial-controllers "%INITIAL_CONTROLLERS%"

echo Formatting broker storage...
call bin\windows\kafka-storage.bat format --cluster-id "%CLUSTER_ID%" --config "%DEMO_HOME%\config\broker-1.properties" --no-initial-controllers
call bin\windows\kafka-storage.bat format --cluster-id "%CLUSTER_ID%" --config "%DEMO_HOME%\config\broker-2.properties" --no-initial-controllers
call bin\windows\kafka-storage.bat format --cluster-id "%CLUSTER_ID%" --config "%DEMO_HOME%\config\broker-3.properties" --no-initial-controllers
call bin\windows\kafka-storage.bat format --cluster-id "%CLUSTER_ID%" --config "%DEMO_HOME%\config\broker-4.properties" --no-initial-controllers
call bin\windows\kafka-storage.bat format --cluster-id "%CLUSTER_ID%" --config "%DEMO_HOME%\config\broker-5.properties" --no-initial-controllers

echo.
echo Kafka demo cluster prepared.
echo Cluster ID: %CLUSTER_ID%
echo Config dir: %DEMO_HOME%\config
echo Data dir:   %DEMO_HOME%\data
echo.
exit /b 0

:writeController
set "ID=%~1"
set "PORT=%~2"
(
echo process.roles=controller
echo node.id=%ID%
echo listeners=CONTROLLER://localhost:%PORT%
echo listener.security.protocol.map=CONTROLLER:PLAINTEXT
echo controller.listener.names=CONTROLLER
echo controller.quorum.bootstrap.servers=localhost:9093,localhost:9094,localhost:9095
echo metadata.log.dir=%DEMO_HOME_PROP%/data/controller-%ID%
echo log.dirs=%DEMO_HOME_PROP%/kafka-logs/data/controller-%ID%
) > "%DEMO_HOME%\config\controller-%ID%.properties"
exit /b

:writeBroker
set "ID=%~1"
set "PORT=%~2"
(
echo process.roles=broker
echo node.id=%ID%
echo listeners=PLAINTEXT://localhost:%PORT%
echo advertised.listeners=PLAINTEXT://localhost:%PORT%
echo listener.security.protocol.map=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
echo inter.broker.listener.name=PLAINTEXT
echo controller.listener.names=CONTROLLER
echo controller.quorum.bootstrap.servers=localhost:9093,localhost:9094,localhost:9095
echo log.dirs=%DEMO_HOME_PROP%/data/broker-%ID%
echo num.partitions=3
echo default.replication.factor=3
echo min.insync.replicas=2
echo offsets.topic.replication.factor=3
echo transaction.state.log.replication.factor=3
echo transaction.state.log.min.isr=2
echo group.initial.rebalance.delay.ms=0
) > "%DEMO_HOME%\config\broker-%ID%.properties"
exit /b
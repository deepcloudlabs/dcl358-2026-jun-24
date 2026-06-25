REM The script is developed for instructional and training purposes
REM Author: Binnur Kurt <binnur.kurt@gmail.com>
@echo off
setlocal

set "KAFKA_HOME=C:\kafka_2.13-4.3.0"
set "DEMO_HOME=C:\kafka"
set "JAVA_HOME=c:\DEVEL\stage\opt\jdk-26.0.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%KAFKA_HOME%"

set KAFKA_HEAP_OPTS=-Xms512m -Xmx2048m

echo Starting 3 Kafka controllers...
start "kafka-controller-3001" cmd /k "set KAFKA_OPTS=-Dkafka.logs.dir=C:/kafka/logs/controller-3001&& bin\windows\kafka-server-start.bat C:\kafka\config\controller-3001.properties"
start "kafka-controller-3002" cmd /k "set KAFKA_OPTS=-Dkafka.logs.dir=C:/kafka/logs/controller-3002&& bin\windows\kafka-server-start.bat C:\kafka\config\controller-3002.properties"
start "kafka-controller-3003" cmd /k "set KAFKA_OPTS=-Dkafka.logs.dir=C:/kafka/logs/controller-3003&& bin\windows\kafka-server-start.bat C:\kafka\config\controller-3003.properties"

timeout /t 8 /nobreak

echo Starting 5 Kafka brokers...
start "kafka-broker-1" cmd /k bin\windows\kafka-server-start.bat C:\kafka\config\broker-1.properties
start "kafka-broker-2" cmd /k bin\windows\kafka-server-start.bat C:\kafka\config\broker-2.properties
start "kafka-broker-3" cmd /k bin\windows\kafka-server-start.bat C:\kafka\config\broker-3.properties
start "kafka-broker-4" cmd /k bin\windows\kafka-server-start.bat C:\kafka\config\broker-4.properties
start "kafka-broker-5" cmd /k bin\windows\kafka-server-start.bat C:\kafka\config\broker-5.properties

echo.
echo Bootstrap servers:
echo localhost:9092,localhost:9096,localhost:9097,localhost:9098,localhost:9099
echo.
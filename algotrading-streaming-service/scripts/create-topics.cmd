@echo off
setlocal EnableExtensions

if "%KAFKA_HOME%"=="" set "KAFKA_HOME=C:\DEVEL\stage\opt\kafka_2.13-4.3.0"
if "%BOOTSTRAP%"=="" set "BOOTSTRAP=localhost:9092,localhost:9096,localhost:9097,localhost:9098,localhost:9099"
if "%PARTITIONS%"=="" set "PARTITIONS=12"
if "%REPLICATION_FACTOR%"=="" set "REPLICATION_FACTOR=3"

call :createTopic trades 86400000
call :createTopic market-analytics.trades.validated 86400000
call :createTopic market-analytics.candles-1m 604800000
call :createTopic market-analytics.vwap-5m 604800000
call :createTopic market-analytics.alerts 604800000
exit /b 0

:createTopic
"%KAFKA_HOME%\bin\windows\kafka-topics.bat" ^
  --bootstrap-server "%BOOTSTRAP%" ^
  --create ^
  --if-not-exists ^
  --topic "%~1" ^
  --partitions "%PARTITIONS%" ^
  --replication-factor "%REPLICATION_FACTOR%" ^
  --config retention.ms=%~2
exit /b 0

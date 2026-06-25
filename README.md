# DCL-358: Implementing Event-Driven Microservice Architecture using Spring Boot and Apache Kafka

These projects are created as part of the following training: DCL-358 "Implementing Event-Driven Microservice Architecture using Spring Boot and Apache Kafka"

Please follow the link for the complete training catalog: https://www.deepcloudlabs.com/resources

Kurulum Bilgisi:
========================================
Eğitimde lab çalışmaları için gerekli olan çalışma ortamının kurulumu için öncelikle aşağıdaki bağlantıda yer alan sıkıştırılmış dosyayı makinanıza indirmeniz gerekiyor: https://courseware.deepcloudlabs.com/software/DEVEL-stage-2025b-java.se.and.spring.zip

Sıkıştırılmış dosyayı **C:\\** dizinine açtıktan sonra dizin yapısı aşağıda gösterildiği şekilde olacaktır:

![Installation folder](DEVEL-stage.png?raw=true "C: drive after decompress DEVEL-stage-2025b-java.se.and.spring.zip")

Diskinizdeki dizin yapısını yukarıdaki ile karşılaştırarak kontrol ediniz. **C:** sürücünüzün dolu dolması durumunda farklı bir sürücüye sıkıştırılmış dosyayı açabilirsiniz. Ancak bu durumda bir kaç konfigürasyon dosyasında değişiklik yapmanız gerekecektir. Lütfen, eğitim sırasında bu değişikliklerin neler olduğunu eğitmeninize sorunuz. 

# Apache Kafka Training Notes

This repository contains hands-on examples and conceptual notes for learning **Apache Kafka** as a distributed event streaming platform. The focus is on Kafka’s core architectural building blocks: brokers, controllers, topics, partitions, replication, durability, and basic local installation on Windows.

## 1. Kafka in One Sentence

Apache Kafka is a distributed event streaming platform that stores messages durably, distributes them across partitions, and allows producer and consumer applications to exchange high-throughput event streams.

A simplified message flow is:

```text
Producer Application  --->  Topic  --->  Consumer Application
                          messages       polling
```

Kafka topics are **conceptual streams**. Internally, Kafka stores topic data inside **partitions**, and partitions are stored as files on broker nodes.

---

## 2. Durability and Replication

Kafka provides durability by writing messages to disk and replicating partitions across multiple broker nodes.

In distributed systems, a common fault-tolerance rule is:

```text
2f + 1
```

Where:

```text
f = number of tolerated failures
```

For example, if we want to tolerate `f = 2` controller failures:

```text
2f + 1 = 2(2) + 1 = 5 controllers
```

So, a production-grade Kafka cluster that tolerates two controller failures should have **5 controller nodes**.

Example:

```text
Controller count: 5
Failure tolerance: 2

1 active controller leader
4 controller followers
```

Kafka elects one controller as the active leader. If the active controller fails, another controller is selected through leader election.

---

## 3. Kafka Node Roles

A Kafka node can act as a:

```text
Broker
Controller
Broker + Controller
```

In modern Kafka KRaft mode, the two main roles are:

### Broker

A broker is responsible for message brokerage.

Its responsibilities include:

```text
Receiving messages from producers
Writing messages to files
Serving messages to consumers
Managing partitions and replicas
```

Conceptually:

```text
Message ---> Broker ---> File
```

### Controller

A controller is responsible for metadata management.

Its responsibilities include:

```text
Cluster metadata management
Leader election
Partition leadership management
Broker membership tracking
Consistent metadata persistence
```

Conceptually:

```text
Metadata ---> Controller ---> Consistent Metadata Log ---> File
```

---

## 4. Cluster Topology

A Kafka cluster consists of multiple nodes.

Example production-style topology:

```text
Cluster: 10 nodes
Fault tolerance target: f = 2
Controller count: 2f + 1 = 5
Broker count: 10
```

A simplified view:

```text
Kafka Cluster
│
├── Controllers
│   ├── Controller 1 - Leader
│   ├── Controller 2
│   ├── Controller 3
│   ├── Controller 4
│   └── Controller 5
│
└── Brokers
    ├── Broker 1
    ├── Broker 2
    ├── Broker 3
    ├── ...
    └── Broker 10
```

Producer and consumer applications communicate with broker nodes:

```text
Producer / Consumer Applications ---> Broker Nodes
```

---

## 5. Topics, Partitions, and Segments

A Kafka topic is a logical event stream.

Example topic:

```text
customer-events
```

Internally, a topic is divided into partitions for horizontal scalability.

```text
Topic ---> Partitions ---> Segments
```

Example:

```text
customer-events
│
├── partition-001
│   ├── segment-1
│   ├── segment-2
│   └── segment-3
│
├── partition-011
│
└── partition-021
```

Partitions allow Kafka to distribute data across brokers.

For example, with 10 nodes and many partitions:

```text
10 broker nodes
1000 partitions
100M messages
30 minutes of retention or processing window
```

Each node may host many partitions:

```text
Per node: approximately 100 partitions
```

---

## 6. Partitioning and Sharding

Kafka records are key-value pairs:

```text
(key, value)
```

Kafka uses the record key to determine the target partition.

Conceptually:

```text
(key, value) ---> partitioning function ---> partition
```

Example:

```text
("customer-123", event) ---> hash("customer-123") ---> partition-021
```

This gives Kafka horizontal scalability because records can be distributed across many partitions and broker nodes.

---

## 7. Producer Performance Considerations

A producer sends messages to Kafka brokers.

In a high-throughput system, producer-side bottlenecks may appear in:

```text
CPU utilization
Network bandwidth
Disk I/O
```

Example saturation indicators:

```text
CPU usage:              90%+
Network bandwidth:      90%+
Disk I/O utilization:   90%+
```

If a 10-node cluster becomes saturated, the cluster can be scaled horizontally:

```text
10 nodes ---> 12 nodes
```

Kafka is designed for this kind of horizontal scaling.

---

## 8. Consumer Offset

Kafka consumers track their reading position using offsets.

Each consumer maintains a current offset for each partition it consumes.

```text
Consumer ---> Partition ---> Current Offset
```

Example:

```text
partition-001 offset: 105
partition-002 offset: 981
partition-003 offset: 2044
```

The offset tells Kafka where the consumer should continue reading.

This enables:

```text
Reliable message processing
Consumer restart recovery
Replay of previous messages
Parallel consumption
```

---

# Local Installation on Windows

The following section demonstrates a simple standalone Kafka setup on Windows for training and demo purposes.

## 1. Go to the installation directory

```cmd
cd /d C:\DEVEL\stage\opt
```

## 2. Configure Java

Set `JAVA_HOME`:

```cmd
set JAVA_HOME=C:\DEVEL\stage\opt\jdk-26.0.1
```

Update `PATH`:

```cmd
set PATH=%JAVA_HOME%\bin;%PATH%
```

Verify Java:

```cmd
java -version
```

Expected output:

```text
java version "26.0.1" 2026-04-21
Java(TM) SE Runtime Environment (build 26.0.1+8-34)
Java HotSpot(TM) 64-Bit Server VM (build 26.0.1+8-34, mixed mode, sharing)
```

## 3. Enter the Kafka directory

```cmd
cd kafka_2.13-4.3.0
```

## 4. Generate a Kafka cluster ID

```cmd
bin\windows\kafka-storage.bat random-uuid
```

Example output:

```text
xTEd8GKjT_Gct3FJo9S7iQ
```

## 5. Format Kafka storage

Use the generated cluster ID:

```cmd
bin\windows\kafka-storage.bat format -t xTEd8GKjT_Gct3FJo9S7iQ --standalone -c config\server.properties
```

This initializes Kafka storage metadata for the local standalone server.

## 6. Start Kafka

```cmd
start bin\windows\kafka-server-start.bat config\server.properties
```

This starts the Kafka server in a separate Windows command window.

---

# Creating a Topic

Create a topic named `customer-events`:

```cmd
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic customer-events --partitions 3 --replication-factor 1
```

Expected output:

```text
Created topic customer-events.
```

Explanation:

```text
--bootstrap-server localhost:9092
```

Connects to the local Kafka broker.

```text
--topic customer-events
```

Creates a topic named `customer-events`.

```text
--partitions 3
```

Creates 3 partitions for horizontal scaling.

```text
--replication-factor 1
```

Uses a replication factor of 1 because this is a local standalone demo.

---

# Describe the Topic

```cmd
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic customer-events
```

This command displays topic metadata such as:

```text
Partition count
Replication factor
Partition leaders
Partition replicas
In-sync replicas
```

---

# Produce Messages

Start a console producer:

```cmd
bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic customer-events
```

Then type messages:

```text
customer-created
customer-updated
customer-deleted
```

Each line is sent as a Kafka message.

---

# Consume Messages

Start a console consumer:

```cmd
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic customer-events --from-beginning
```

Expected output:

```text
customer-created
customer-updated
customer-deleted
```

---

# Summary

Kafka provides a scalable, durable, distributed messaging and event streaming infrastructure.

The key architectural concepts are:

```text
Broker       -> handles messages and partitions
Controller   -> manages cluster metadata and leader election
Topic        -> logical event stream
Partition    -> unit of horizontal scaling
Segment      -> physical file-based storage unit
Offset       -> consumer reading position
Replication  -> durability and fault tolerance
```

For local training, a standalone Kafka server is sufficient.

For production-grade deployment, Kafka should be deployed as a multi-node cluster with multiple brokers, replicated partitions, and an odd number of controllers such as 3 or 5 depending on the desired fault tolerance level.

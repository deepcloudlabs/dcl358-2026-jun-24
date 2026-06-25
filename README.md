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

# Apache Kafka Demo Cluster and Event-Driven Modeling Notes

## 1. Demo Cluster Topology

In this demo, we run an Apache Kafka cluster on a single Windows machine. The cluster is designed for educational purposes and simulates a multi-node Kafka deployment by running each Kafka process on a different port.

The cluster consists of:

| Role                  | Number of Nodes | Responsibility                                                    |
| --------------------- | --------------: | ----------------------------------------------------------------- |
| Controller            |               3 | Manages Kafka metadata, leader election, and cluster coordination |
| Broker                |               5 | Stores topic partitions and serves producer/consumer traffic      |
| Total Kafka Processes |               8 | 3 controllers + 5 brokers                                         |

With 3 controller nodes, the controller quorum can tolerate the failure of 1 controller node:

```text
n = 3 controllers
f = 1 tolerated controller failure
```

This is because a majority of controllers must remain available for the metadata quorum to continue operating.

---

## 2. Kafka Installation

Download Apache Kafka 4.3.0:

```text
https://www.apache.org/dyn/closer.lua/kafka/4.3.0/kafka_2.13-4.3.0.tgz?action=download
```

Extract the archive to:

```text
C:\kafka_2.13-4.3.0
```

Then open `cmd.exe` and move into the Kafka installation directory:

```cmd
cd /d C:\kafka_2.13-4.3.0
```

The `/d` option allows `cd` to change both the directory and the drive.

---

## 3. Demo Scripts

Two scripts are used to prepare and start the local Kafka cluster:

| Script                      | Purpose                                                   |
| --------------------------- | --------------------------------------------------------- |
| `prepare-kafka-cluster.cmd` | Creates the configuration files and formats Kafka storage |
| `start-kafka-demo.cmd`      | Starts 3 controllers and 5 brokers                        |

Copy the scripts into the Kafka installation directory:

```cmd
copy C:\Users\binku\Downloads\prepare-kafka-cluster.cmd .
copy C:\Users\binku\Downloads\start-kafka-demo.cmd .
```

Prepare the cluster:

```cmd
prepare-kafka-cluster.cmd
```

Start the cluster:

```cmd
start-kafka-demo.cmd
```

The cluster should start 8 separate Kafka processes:

```text
Controller 3001
Controller 3002
Controller 3003

Broker 1
Broker 2
Broker 3
Broker 4
Broker 5
```

Each process uses a different port.

---

## 4. Kafka Roles in KRaft Mode

Modern Kafka runs in KRaft mode, where ZooKeeper is no longer required.

Kafka processes may have different roles:

```properties
process.roles=controller
```

or:

```properties
process.roles=broker
```

A controller node manages metadata. A broker node stores and serves topic partitions.

In a separated production-like topology, controllers and brokers are different processes. In this demo, we simulate that architecture on a single Windows machine.

---

## 5. Broker and Controller Separation

The demo cluster has:

```text
3 dedicated controllers
5 dedicated brokers
```

This is different from a combined-role Kafka node. In this setup:

```text
Controllers manage the control plane.
Brokers manage the data plane.
```

The control plane is responsible for:

```text
Cluster metadata
Topic metadata
Partition leadership
Broker registration
Leader election
```

The data plane is responsible for:

```text
Receiving records from producers
Storing topic partitions
Replicating records
Serving records to consumers
```

---

# 6. Domain Modeling and Kafka

## 6.1 Domain Modeling

In Domain-Driven Design, the domain model represents the core business concepts of the system.

Typical tactical DDD building blocks are:

```text
Aggregate
Entity
Value Object
Domain Event
Repository
Domain Service
```

For example, in a customer management bounded context, `Customer` can be modeled as an aggregate.

```text
Customer Aggregate
 ├── CustomerId
 ├── Name
 ├── Email
 ├── AddressList
 └── PhoneList
```

The aggregate is the transactional consistency boundary. Business invariants are enforced inside the aggregate.

Example invariants:

```text
A customer must have a valid identity.
A customer cannot have more than a certain number of phone numbers.
A customer address must be valid before it is added.
A deactivated customer cannot be modified.
```

---

## 6.2 Aggregate Persistence

An aggregate is usually persisted in a database.

Example:

```text
Aggregate: Customer
Persistence: Database
```

In a traditional CRUD-oriented model, the current state of the customer is stored.

In an event-driven or event-sourced model, changes in the aggregate can also be represented as events.

---

## 6.3 Domain Events

A domain event represents something important that happened in the domain.

Example:

```text
CustomerAddressChangedEvent
```

This event means that the customer’s address was changed successfully according to the rules of the domain model.

A domain event should usually be named in the past tense:

```text
CustomerRegistered
CustomerAddressChanged
CustomerPhoneNumberAdded
CustomerDeactivated
```

A domain event is not a command. A command asks the system to do something. An event says that something has already happened.

| Concept | Meaning                      | Example                  |
| ------- | ---------------------------- | ------------------------ |
| Command | Request to perform an action | `ChangeCustomerAddress`  |
| Event   | Fact that something happened | `CustomerAddressChanged` |

---

## 6.4 Kafka as Event Storage / Messaging Infrastructure

Domain events can be published to a messaging system such as:

```text
Apache Kafka
RabbitMQ
ActiveMQ
RocketMQ
```

In this demo, Kafka is used as the event streaming platform.

Example:

```text
Customer Aggregate
      |
      | emits
      v
CustomerAddressChangedEvent
      |
      | published to
      v
Kafka topic: customer-events
```

Kafka stores events in durable, ordered logs called topic partitions.

---

# 7. Kafka Topic Design

## 7.1 Topic

For this demo, the main topic is:

```text
customer-events
```

This topic stores customer-related domain events.

Example events:

```json
{
  "eventId": "evt-1001",
  "eventType": "CustomerRegistered",
  "customerId": "cust-123",
  "occurredAt": "2026-06-25T10:15:00Z",
  "payload": {
    "fullName": "Jane Doe",
    "email": "jane@example.com"
  }
}
```

---

## 7.2 Partitions

The topic has 3 partitions:

```text
customer-events
 ├── partition-0
 ├── partition-1
 └── partition-2
```

Partitions allow Kafka to scale horizontally. Each partition is an ordered, append-only log.

Ordering is guaranteed only within a single partition.

Therefore, for customer events, the Kafka message key should usually be the `customerId`.

Example:

```text
key = customerId
value = CustomerRegistered event
```

This ensures that all events for the same customer are routed to the same partition and processed in order.

---

## 7.3 Replication Factor

The topic uses a replication factor of 3:

```text
replication.factor = 3
```

This means each partition has 3 replicas distributed across brokers.

One replica is the leader. The others are followers.

Producers and consumers interact with the leader replica. Follower replicas replicate data from the leader.

---

# 8. Example Partition Distribution

## 8.1 Partition 0

```text
customer-events::partition-0

Leader:   Broker 1
Replicas: Broker 1, Broker 3, Broker 5
ISR:      Broker 1, Broker 5
```

Visual representation:

```text
Broker 1  -> Leader
Broker 3  -> Follower
Broker 5  -> Follower
```

---

## 8.2 Partition 1

```text
customer-events::partition-1

Leader:   Broker 2
Replicas: Broker 2, Broker 4, Broker 5
```

Visual representation:

```text
Broker 2  -> Leader
Broker 4  -> Follower
Broker 5  -> Follower
```

---

## 8.3 Partition 2

```text
customer-events::partition-2

Leader:   Broker 3
Replicas: Broker 3, Broker 2, Broker 1
```

Visual representation:

```text
Broker 3  -> Leader
Broker 2  -> Follower
Broker 1  -> Follower
```

---

# 9. Leader, Follower, and ISR

## 9.1 Leader Replica

Each partition has exactly one leader replica at a given time.

The leader handles:

```text
Producer writes
Consumer reads
Replication coordination
```

Example:

```text
Producer -> Partition Leader -> Followers
```

---

## 9.2 Follower Replica

Follower replicas copy data from the leader.

Followers do not normally serve client reads and writes in the standard Kafka model. Their main responsibility is to stay synchronized with the leader.

---

## 9.3 ISR: In-Sync Replicas

ISR means:

```text
In-Sync Replicas
```

ISR is the set of replicas that are sufficiently up-to-date with the leader.

Example:

```text
Replicas: [Broker 1, Broker 3, Broker 5]
ISR:      [Broker 1, Broker 5]
```

This means Broker 3 is a replica, but it is not currently considered in-sync.

A replica can fall out of ISR if it lags behind the leader for too long.

A related broker configuration is:

```properties
replica.lag.time.max.ms=30000
```

This means that if a follower replica does not catch up within the configured time window, it may be removed from the ISR.

---

# 10. Producer Acknowledgement Semantics

## 10.1 Producer Write Flow

The producer writes to the leader replica of the target partition:

```text
Producer -> Leader Replica -> Follower Replicas
```

Replication from leader to followers is asynchronous from the perspective of the distributed system, but producer acknowledgement behavior depends on the `acks` setting.

---

## 10.2 acks=all

For stronger durability, producers can use:

```properties
acks=all
```

With `acks=all`, the producer receives an acknowledgement only after the record has been written to the leader and replicated to the required in-sync replicas.

The effective durability also depends on:

```properties
min.insync.replicas
```

For example:

```properties
acks=all
min.insync.replicas=2
```

This means the write is considered successful only if the leader and at least one additional in-sync replica can acknowledge the record.

---

## 10.3 Why ISR Matters

If acknowledgement comes only after successful replication to the required ISR members, the data is more reliable.

Conceptually:

```text
Producer
   |
   v
Partition Leader
   |
   v
ISR Replicas
   |
   v
ACK returned to producer
```

If too few ISR replicas are available, Kafka may reject the write instead of accepting a record that cannot meet the durability requirement.

This is a key mechanism for balancing availability and consistency.

---

# 11. Important Replication-Related Broker Settings

Kafka exposes several settings related to replication behavior. For introductory purposes, the most important ones are:

```properties
default.replication.factor=3
min.insync.replicas=2
replica.lag.time.max.ms=30000
replica.fetch.backoff.ms=1000
replica.fetch.max.bytes=1048576
replica.fetch.min.bytes=1
replica.fetch.wait.max.ms=500
```

Explanation:

| Setting                      | Meaning                                                                       |
| ---------------------------- | ----------------------------------------------------------------------------- |
| `default.replication.factor` | Default number of replicas for new topics                                     |
| `min.insync.replicas`        | Minimum number of ISR replicas required for successful writes when `acks=all` |
| `replica.lag.time.max.ms`    | Maximum time a follower may lag before being removed from ISR                 |
| `replica.fetch.backoff.ms`   | Backoff interval used by followers when fetching from leaders                 |
| `replica.fetch.max.bytes`    | Maximum bytes fetched by a replica in one request                             |
| `replica.fetch.min.bytes`    | Minimum data size the follower tries to fetch                                 |
| `replica.fetch.wait.max.ms`  | Maximum wait time for replica fetch responses                                 |

For most demo scenarios, the default Kafka values are sufficient. The key settings to understand are:

```properties
default.replication.factor=3
min.insync.replicas=2
acks=all
```

---

# 12. Consumer Groups

Kafka consumers usually work as members of a consumer group.

A consumer group allows multiple consumers to share the work of reading from a topic.

Example:

```text
Topic: customer-events
Partitions: 3

Consumer Group: customer-service-projection
 ├── Consumer A -> partition-0
 ├── Consumer B -> partition-1
 └── Consumer C -> partition-2
```

A partition can be consumed by only one consumer within the same consumer group at a time.

If there are more consumers than partitions, some consumers will remain idle.

If there are fewer consumers than partitions, some consumers will process multiple partitions.

---

## 12.1 Consumer Offsets

Kafka tracks the position of each consumer group using offsets.

An offset identifies the position of a record inside a partition.

Example:

```text
customer-events / partition-0 / offset-42
```

The consumer group stores committed offsets so that it can continue processing from the correct position after a restart.

Important offset-related settings include:

```properties
offsets.topic.replication.factor=3
offsets.retention.minutes=10080
offsets.commit.timeout.ms=5000
```

---

## 12.2 Rebalancing

When consumers join or leave a consumer group, Kafka performs a rebalance.

During a rebalance, partitions are reassigned among available consumers.

Relevant settings include:

```properties
group.initial.rebalance.delay.ms=0
group.consumer.heartbeat.interval.ms=5000
group.consumer.session.timeout.ms=45000
```

For demo purposes, setting `group.initial.rebalance.delay.ms=0` allows the group to start consuming more quickly.

---

# 13. WebSocket, SSE, and Messaging Systems

Real-time systems can use different communication models depending on the direction and nature of data flow.

## 13.1 WebSocket

WebSocket provides bidirectional communication:

```text
Client <----> Server
```

It supports:

```text
Text messages
Binary messages
Streaming-like interaction
Real-time communication
```

Typical clients:

```text
Web browser
Mobile application
CLI client
Desktop application
```

WebSocket is useful when both sides need to send data at any time.

Example use cases:

```text
Chat applications
Trading dashboards
Collaborative editing
Real-time monitoring
Multiplayer games
```

---

## 13.2 Server-Sent Events

Server-Sent Events, or SSE, provide one-way communication from server to client:

```text
Server ----> Client
```

SSE is usually text-based and commonly uses JSON payloads.

Example use cases:

```text
Notification streams
Live status updates
Progress updates
Monitoring dashboards
```

SSE is simpler than WebSocket when the client only needs to receive updates from the server.

---

## 13.3 Push Notification

Push notification is used when the server needs to notify a user or device, often through platform-specific infrastructure.

Example use cases:

```text
Mobile app notification
Browser notification
System alert
```

---

## 13.4 Messaging Systems

Messaging systems decouple producers and consumers.

Examples:

```text
Apache Kafka
RabbitMQ
ZeroMQ
```

Kafka is especially strong for:

```text
High-throughput event streaming
Durable event logs
Replayable events
Partitioned scalability
Consumer-group-based parallel processing
Event-driven microservices
```

---

# 14. Kafka in Event-Driven Architecture

Kafka is commonly used as the backbone of event-driven systems.

A typical event-driven flow is:

```text
Command
   |
   v
Aggregate
   |
   v
Domain Event
   |
   v
Kafka Topic
   |
   v
Consumer / Projection / Integration Handler
```

Example:

```text
ChangeCustomerAddress command
   |
   v
Customer aggregate validates the change
   |
   v
CustomerAddressChanged event is produced
   |
   v
Event is published to customer-events topic
   |
   v
Other services consume the event
```

Possible consumers:

```text
Customer read-model projection
Notification service
Audit service
CRM integration service
Analytics pipeline
```

---

# 15. Recommended Kafka Message Design for Domain Events

A customer event should include metadata and payload.

Example:

```json
{
  "eventId": "evt-1001",
  "eventType": "CustomerAddressChanged",
  "aggregateType": "Customer",
  "aggregateId": "cust-123",
  "occurredAt": "2026-06-25T12:30:00Z",
  "version": 7,
  "payload": {
    "street": "Example Street",
    "city": "Istanbul",
    "country": "Türkiye"
  }
}
```

Recommended Kafka key:

```text
aggregateId
```

For customer events:

```text
key = customerId
```

This preserves event ordering per customer because Kafka sends records with the same key to the same partition.

---

# 16. Demo Summary

In this demo, we built a Kafka cluster with:

```text
3 controllers
5 brokers
3 partitions
replication factor = 3
acks = all
```

The key architectural ideas are:

```text
Controllers manage metadata.
Brokers store and replicate data.
Topics are split into partitions.
Partitions are replicated across brokers.
Each partition has one leader.
Followers replicate from the leader.
ISR defines which replicas are sufficiently synchronized.
acks=all improves producer-side durability.
Consumer groups provide scalable event consumption.
Kafka can serve as the event backbone of a DDD-oriented architecture.
```

From a DDD perspective, Kafka should not be treated merely as a technical queue. It is part of the integration and eventing infrastructure that carries meaningful domain events between bounded contexts and downstream consumers.

For local training, a standalone Kafka server is sufficient.

For production-grade deployment, Kafka should be deployed as a multi-node cluster with multiple brokers, replicated partitions, and an odd number of controllers such as 3 or 5 depending on the desired fault tolerance level.

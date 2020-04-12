# Hadoop-Env
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
![Scala](https://img.shields.io/badge/Scala-2.11.4%2B-blue)
![Java](https://img.shields.io/badge/Java-8%2B-lightgrey)
![Hadoop](https://img.shields.io/badge/Hadoop-3.1.3-green)

## Basic Overview

Project to simulate a local Hadoop environment in a Windows machine. Runs a HBase Server and Thrift Server for connectivity and code testing, as well as a Zookeeper node and a Kafka broker with a <code>PLAINTEXT</code> listener.


## Hadoop Version
Latest release is compatible with Hadoop 3.1.3.
This might be compatible with other versions by replacing the Hadoop zip file in the resources folder with a stable Hadoop winutils zip file, but there is no guarantee stuff won't start breaking.


## Use

By running LocalHadoopEnviroment, a small one node local cluster will start with the following services:
- HBase Server
- HBase Thrift Server
- Zookeeper Server
- Kafka Broker

All these services can be reached by connecting to localhost, and their respective default ports. If you need to alter any of the default configs, just do so by altering the hbase-site.xml and kafka.properties file, or add your own in the same folder (eg: core-site.xml).

### Connecting to the cluster

For convenience, there are already classes to connect to the local cluster. These are:
- HBaseShell: simulates an actual shell process to access the HBase server through the dists Ruby files
- KafkaConsoleConsumer: simulates a shell consumer for a Kafka topic
- KafkaConsoleProducer: simulates a shell producer for a Kafka topic
- KafkaTopicList: lists all Kafka topics in the current local enviroment

### Necessary configs

You need to add $HADOOP_HOME to your environment variables, as well as adding $HADOOP_HOME/bin to your PATH, as specified in the [Hadoop Wiki](https://cwiki.apache.org/confluence/display/HADOOP2/WindowsProblems).

$HADOOP_HOME will depend on your local configs, but it should point to where the Hadoop winutils file is unpacked. Usually at 'C:\Users\user\AppData\Local\Temp\hadoop-winutils'.

## Latest Development Changes
```bash
git clone https://github.com/josebatalheiro/hadoop-env
```

## Future Improvements
Add Hadoop testing utilites for:
- HDFS
- Spark
- Hive

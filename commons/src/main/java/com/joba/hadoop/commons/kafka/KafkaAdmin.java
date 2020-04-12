package com.joba.hadoop.commons.kafka;

import com.joba.hadoop.commons.Loggable;
import kafka.admin.AdminOperationException;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.common.errors.TopicExistsException;
import scala.collection.Seq;

import java.io.Closeable;
import java.util.Properties;

/**
 * @author jose batalheiro
 * @project hadoop-env
 */
public class KafkaAdmin extends Loggable implements Closeable {

    private static final int ZK_SESSION_TIMEOUT_MS = 6000;
    private static final int ZK_CONNECTION_TIMEOUT_MS = 6000;

    private final ZkClient zkClient;
    private final ZkConnection zkConnection;

    public KafkaAdmin(String zookeeperConnect, String zookeeperSuffix) {
        this(zookeeperConnect, zookeeperSuffix, ZK_SESSION_TIMEOUT_MS, ZK_CONNECTION_TIMEOUT_MS);
    }

    public KafkaAdmin(String zookeeperConnect, String zookeeperSuffix, int sessionTimeout, int connectionTimeout) {
        zkClient = new ZkClient(zookeeperConnect + zookeeperSuffix, sessionTimeout, connectionTimeout,
                ZKStringSerializer$.MODULE$);
        this.zkConnection = new ZkConnection(zookeeperConnect, sessionTimeout);
    }

    /**
     * @param topicConfig detailed configuration properties for the topic. Use {@link kafka.log.LogConfig} to get the
     *                    names of the available properties.
     * @return true if the topic is created, false if it already exists.
     */
    public boolean createTopic(String topic, int partitions, int replicationFactor, Properties topicConfig)
            throws AdminOperationException {

        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);
        if (!AdminUtils.topicExists(zkUtils, topic)) {
            try {
                AdminUtils.createTopic(zkUtils, topic, partitions, replicationFactor, topicConfig, RackAwareMode.Enforced$.MODULE$);
                INFO("Created Kafka topic '{}' with {} partitions and replication factor {}.",
                        topic, partitions, replicationFactor);
                return true;
            } catch (TopicExistsException e) {
                return false;
            }
        }
        return false;
    }

    public Properties getTopicConfig(String topic) {
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);
        return AdminUtils.fetchEntityConfig(zkUtils, "topic", topic);
    }

    public void changeTopicConfig(String topic, Properties properties) {
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);
        AdminUtils.changeTopicConfig(zkUtils, topic, properties);
    }

    public Seq<String> getAllTopics() {
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);

        return zkUtils.getAllTopics();
    }

    @Override
    public void close() {
        try {
            zkClient.close();
            zkConnection.close();
        } catch (InterruptedException e) {
            ERROR("Failed to close Zookeeper connection", e);
        }
    }
}
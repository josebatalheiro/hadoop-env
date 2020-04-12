package com.joba.hadoop.test;

import com.joba.hadoop.commons.kafka.KafkaAdmin;
import kafka.Kafka$;
import kafka.admin.AdminOperationException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.master.HMasterCommandLine;
import org.apache.hadoop.hbase.thrift.ThriftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.joba.hadoop.utils.fileutils.FileUtils.*;

/**
 * Requires JDK8 to be set as the JDK for the project, as the current version of HBase does not work with JDK9+.
 * This class initializes a local Hadoop environment, with HBase, Kafka and Zookeeper services running on localhost.
 *
 *
 * @author jose batalheiro
 * @project hadoop-env
 */
public class LocalHadoopEnvironment {
    private static final String ZOOKEEPER_CONNECTION = "127.0.0.1:2181";
    private static final String HADOOP_ZIP_FILE = "hadoop-3.1.3.zip";
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalHadoopEnvironment.class);
    private static final long RETRY_INTERVAL_MS = 3000L;
    private static final File WINUTILS_DIRECTORY;

    static {
        if (System.getProperty("java.version").matches("\\d\\d\\..*.*")) {
            LOGGER.info("Current version of Hadoop does not recognize JDK > 9. Passing JDK1.8.2 as Java version.");
            System.setProperty("java.version", "1.8.2");
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        WINUTILS_DIRECTORY = new File(tmpDir + File.separator + "hadoop-winutils");
        deleteDirectory(WINUTILS_DIRECTORY);
        boolean success = WINUTILS_DIRECTORY.mkdir();
        if (!success) {
            LOGGER.warn("Could not create Hadoop winutils directory '" +
                    WINUTILS_DIRECTORY.getAbsolutePath() + "'.");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LOGGER.warn("Shutdown hook thread interrupted. Exiting immediately.");
                }
                finally {
                    deleteDirectory(WINUTILS_DIRECTORY);
                }
            }
        }));
    }

    private static final String HBASE_NAMESPACE = "test";
    private static final String[][] HBASE_TABLES = {
            new String[]{"test", "cf"}
    };

    private static final String[] KAFKA_TOPICS = {"test"};

    /**
     * Starts HBase service and creates needed HBase tables.
     */
    public static void main(String[] args) throws Exception {
        setupWinUtils();
        run();
    }

    private static void run() throws InterruptedException, IOException {
        // Initialize services first.
        List<Thread> serviceThreads = startServices();
        initHBaseTables();
        initKafkaTopics();

        registerShutdownHook(serviceThreads);
    }

    private static List<Thread> startServices() {

        List<Thread> threads = new ArrayList<>();

        // Starts an embedded Zookeeper and HBase in local mode.
        threads.add(new Thread(() -> {
            HMasterCommandLine.LocalHMaster.main(new String[]{"start"});
        }, "hbase-thread"));
        // Starts an embedded Zookeeper and HBase in local mode.

        threads.add(new Thread(() -> {
            try {
                ThriftServer.main(new String[]{"start"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "hbase-thrift"));

        threads.add(new Thread(() -> Kafka$.MODULE$.main(new String[]{LocalHadoopEnvironment.class.getClassLoader().getResource("kafka.properties").getFile()}), "kafka-thread"));

        // Start startServices first.
        for (Thread thread : threads) {
            thread.start();
        }

        return threads;
    }

    private static void registerShutdownHook(final List<Thread> serviceThreads) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            for (Thread thread : serviceThreads) {
                thread.interrupt();
            }
        }, "shutdown-hook"));
    }

    private static void initHBaseTables() throws IOException, InterruptedException {

        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);

        LOGGER.info("Initializing HBase tables.");
        Admin admin = conn.getAdmin();
        boolean success = false;
        while (!success) {
            try {
                try {
                    admin.createNamespace(NamespaceDescriptor.create(HBASE_NAMESPACE).build());
                    LOGGER.info("Created namespace '{}'.", HBASE_NAMESPACE);
                } catch (NamespaceExistException e) {
                    LOGGER.warn("Namespace '{}' already exists.", HBASE_NAMESPACE);
                }

                for (String[] tableDetails : HBASE_TABLES) {
                    try {
                        HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(HBASE_NAMESPACE + ":" +
                                tableDetails[0]));
                        for (int i = 1; i < tableDetails.length; i++) {
                            tableDesc.addFamily(new HColumnDescriptor(tableDetails[i]));
                        }
                        admin.createTable(tableDesc);
                        LOGGER.info("Created table '{}'.", tableDetails[0]);
                    } catch (TableExistsException e) {
                        LOGGER.warn("Table '{}' already exists.", tableDetails[0]);
                    }
                }
                success = true;
            } catch (PleaseHoldException e) {
                LOGGER.warn("Failed to perform operation on HBase. It is not ready yet. " +
                        "Retrying after {} ms ...", RETRY_INTERVAL_MS);
                Thread.sleep(RETRY_INTERVAL_MS);
            }
        }
    }

    private static void initKafkaTopics() throws InterruptedException {

        try (KafkaAdmin kafkaAdmin = new KafkaAdmin(ZOOKEEPER_CONNECTION, "/")) {
            boolean success = false;
            while (!success) {
                try {
                    for (String topic : KAFKA_TOPICS) {
                        if (kafkaAdmin.createTopic(topic, 4, 1, new Properties())) {
                            LOGGER.info("Created Kafka topic '{}'.", topic);
                        } else {
                            LOGGER.warn("Kafka topic '{}' already exists.", topic);
                        }
                    }
                    success = true;
                } catch (AdminOperationException e) {
                    LOGGER.warn("Failed to perform operation on Kafka. " +
                            "Retrying after " + RETRY_INTERVAL_MS + " ms ...", e);
                    Thread.sleep(RETRY_INTERVAL_MS);
                }
            }
        }
    }

    private static void setupWinUtils() throws Exception {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            InputStream winutilsStream = getFileInputStreamFromClasspath(HADOOP_ZIP_FILE);
            LOGGER.info("Extracting Hadoop wintutils to '{}'.", WINUTILS_DIRECTORY);
            try {
                unzip(winutilsStream, WINUTILS_DIRECTORY.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.setProperty("hadoop.home.dir", WINUTILS_DIRECTORY.getAbsolutePath());
        }
    }

    private static void injectEnvironmentVariable(String key, String value)
            throws Exception {

        Class<?> processEnvironment = Class.forName("java.lang.ProcessEnvironment");

        Field unmodifiableMapField = getAccessibleField(processEnvironment, "theUnmodifiableEnvironment");
        Object unmodifiableMap = unmodifiableMapField.get(null);
        injectIntoUnmodifiableMap(key, value, unmodifiableMap);

        Field mapField = getAccessibleField(processEnvironment, "theEnvironment");
        Map<String, String> map = (Map<String, String>) mapField.get(null);
        map.put(key, value);
    }

    private static Field getAccessibleField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    private static void injectIntoUnmodifiableMap(String key, String value, Object map)
            throws ReflectiveOperationException {

        Class unmodifiableMap = Class.forName("java.util.Collections$UnmodifiableMap");
        Field field = getAccessibleField(unmodifiableMap, "m");
        Object obj = field.get(map);
        ((Map<String, String>) obj).put(key, value);
    }

}

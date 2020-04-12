package com.joba.hadoop.commons.kafka;


import java.io.IOException;
import java.util.Properties;

import com.joba.hadoop.utils.fileutils.FileUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerCreator {
    private static Properties props = new Properties();


    public static Producer<Long, String> createProducer() throws IOException {
        props.load(FileUtils.getFileInputStreamFromClasspath("application.properties"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }
}
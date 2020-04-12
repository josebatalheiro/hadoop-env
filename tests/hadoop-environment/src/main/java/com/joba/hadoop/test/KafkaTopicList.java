package com.joba.hadoop.test;

import com.joba.hadoop.commons.kafka.KafkaAdmin;

/**
 *
 * @author jose batalheiro
 * @project hadoop-env
 */
public class KafkaTopicList {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: " + KafkaTopicList.class.getCanonicalName() + " <zookeeper>");
            System.exit(-1);
        }

        KafkaAdmin kafkaAdmin = new KafkaAdmin(args[0], "");

        System.out.println(kafkaAdmin.getAllTopics().mkString("\n"));
    }
}

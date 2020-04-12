package com.joba.hadoop.test;

import kafka.tools.ConsoleProducer$;

/**
 * @author jose batalheiro
 * @project hadoop-env
 */
public class KafkaConsoleProducer {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: " + KafkaConsoleProducer.class.getCanonicalName() + " <broker> <topic>");
            System.exit(-1);
        }

        ConsoleProducer$.MODULE$.main(new String[]{"--broker-list", args[0], "--topic", args[1]});
    }
}

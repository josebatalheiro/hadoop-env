package com.joba.hadoop.test;

import kafka.tools.ConsoleConsumer$;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jose batalheiro
 * @project hadoop-env
 */
public class KafkaConsoleConsumer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: " + KafkaConsoleConsumer.class.getCanonicalName() + " <zookeeper> <topic> [begin]");
            System.exit(-1);
        }

        List<String> arguments = new ArrayList<>(Arrays.asList("--bootstrap-server", args[0], "--topic", args[1]));

        if (args.length == 3) {
            if (!args[2].equals("begin")) {
                throw new IllegalArgumentException("Unknown option '" + args[2] + "'. Expecting 'begin'.");
            }
            arguments.add("--from-beginning");
        }

        ConsoleConsumer$.MODULE$.main(arguments.toArray(new String[arguments.size()]));
    }
}

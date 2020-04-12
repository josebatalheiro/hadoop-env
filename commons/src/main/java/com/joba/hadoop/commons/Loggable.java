package com.joba.hadoop.commons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author jose batalheiro
 * @project hadoop-env
 */
public abstract class Loggable {

    private Logger logger() {
        return LogManager.getLogger(super.getClass());
    }

    protected void INFO(String s, Object... arguments) {
        logger().info(s, arguments);
    }

    protected void DEBUG(String s, Object... arguments) {
        logger().debug(s, arguments);
    }

    protected void TRACE(String s, Object... arguments) {
        logger().trace(s, arguments);
    }

    protected void WARN(String s, Object... arguments) {
        logger().warn(s, arguments);
    }

    protected void ERROR(String s, Object... arguments) {
        logger().error(s, arguments);
    }

}

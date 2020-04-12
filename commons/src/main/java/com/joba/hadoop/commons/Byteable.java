package com.joba.hadoop.commons;

/**
 * @author jose batalheiro
 * @project hadoop-env
 */
public abstract class Byteable {
    public byte[] getBytes() {
        return this.toString().getBytes();
    }
}

package com.joba.hadoop.utils.fileutils;

import com.joba.hadoop.commons.Byteable;
import com.joba.hadoop.commons.Loggable;

import java.io.*;
import java.util.function.Function;

/**
 * @author jose batalheiro
 * @project hadoop-env
 */
public abstract class ChunkableWriter<T extends Byteable> extends Loggable implements Closeable {
    protected String baseFileName;
    private double maxByteSize;
    private Function<T, String> fileSuffixFunction;
    long writtenBytes = 0;

    protected ChunkableWriter(String fileName, double maxByteSize, Function<T, String> fileSuffixFunction) {
        this.baseFileName = fileName;
        this.maxByteSize = maxByteSize;
        this.fileSuffixFunction = fileSuffixFunction;
    }

    public final void write(T writeableObject) throws IOException {
        if ((writtenBytes + writeableObject.getBytes().length) > maxByteSize) {
            if (writtenBytes != 0)
                INFO("Written bytes {}b too near threshold of {}b, creating new file.", writtenBytes, maxByteSize);
            rollToNextFile(writeableObject);
        }
        writeInternal(writeableObject);
        addByteCounter(writeableObject);
    }

    public final void writeBytes(byte[] bytes) throws IOException {
        if ((writtenBytes + bytes.length) > maxByteSize) {
            if (writtenBytes != 0)
                INFO("Written bytes {}b too near threshold of {}b, creating new file.", writtenBytes, maxByteSize);
            rollToNextFile();
        }
        writeInternalBytes(bytes);
        writtenBytes += bytes.length;
    }

    protected abstract void writeInternal(T writeableObject) throws IOException;

    private void addByteCounter(T writeableObject) {
        writtenBytes += writeableObject.getBytes().length;
    }

    public abstract void breakLine() throws IOException;

    public abstract void writeInternalBytes(byte[] bytes) throws IOException;

    void rollToNextFile(T object) throws IOException {
        writtenBytes = 0;
        createNewFile(getNewFileName(object));
    }

    void rollToNextFile() throws IOException {
        writtenBytes = 0;
        createNewFile(getNewFileName());
    }

    String getNewFileName(T object) {
        String suffix = fileSuffixFunction.apply(object);
        return baseFileName + suffix;
    }

    String getNewFileName() {
        String suffix = fileSuffixFunction.apply(null);
        return baseFileName + suffix;
    }

    protected abstract void createNewFile(String fileName) throws IOException;
}

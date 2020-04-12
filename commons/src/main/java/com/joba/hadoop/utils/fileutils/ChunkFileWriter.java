package com.joba.hadoop.utils.fileutils;

import com.joba.hadoop.commons.Byteable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * @author jose batalheiro
 * @project hadoop-env
 */
public class ChunkFileWriter<T extends Byteable> extends ChunkableWriter<T> {
    private OutputStream os;
    private final static byte[] BREAKLINE = "\n".getBytes();
    private final static int BREAKLENGTH = BREAKLINE.length;

    public ChunkFileWriter(String fileName, double maxByteSize, Function<T, String> fileSuffixFunction) {
        super(fileName, maxByteSize, fileSuffixFunction);
    }

    protected void writeInternal(T writeableObject) throws IOException {
        if (os == null)
            rollToNextFile(writeableObject);

        byte[] bytes = writeableObject.getBytes();
        os.write(bytes);
    }

    @Override
    public final void writeInternalBytes(byte[] bytes) throws IOException {
        if (os == null)
            rollToNextFile();

        os.write(bytes);
    }

    public void breakLine() throws IOException {
        if (os == null)
            rollToNextFile(null);

        os.write(BREAKLINE, 0, BREAKLENGTH);
        writtenBytes += BREAKLENGTH;
    }

    @Override
    public void close() throws IOException {
        if (os != null)
            os.close();
    }

    protected void createNewFile(String fileName) throws IOException {
        if (os != null)
            os.close();

        INFO("Creating new file {}", fileName);
        os = new FileOutputStream(fileName);
    }

    @Override
    void rollToNextFile(T object) throws IOException {
        writtenBytes = 0;
        createNewFile(getNewFileName(object));
        writeBytes(("#" + baseFileName.replace('\\', '.').replace('_', '.') + "\ntimestamp;value;status\n").getBytes());
    }

    @Override
    void rollToNextFile() throws IOException {
        writtenBytes = 0;
        createNewFile(getNewFileName());
        writeBytes(("#" + baseFileName.substring(baseFileName.lastIndexOf("\\") + 1).replace('\\', '.').replace('_', '.') + "\ntimestamp;value;status\n").getBytes());
    }
}
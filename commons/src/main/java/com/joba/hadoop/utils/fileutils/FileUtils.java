package com.joba.hadoop.utils.fileutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Utils class for reading or writing files
 *
 * @author jose batalheiro
 * @project hadoop-env
 */
public class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Returns string representation of a file's content.
     * @param filePath  the absolute or relative path to the file
     * @return File's contents as a String
     */
    public static String readFileFromClasspath(String filePath) {
        return readFileFromClasspath(FileUtils.class.getClassLoader(), filePath);
    }

    /**
     * Returns string representation of a file's content.
     * @param classLoader  the super class's ClassLoader
     * @param filePath  the absolute or relative path to the file
     * @return File's contents as a String
     */
    public static String readFileFromClasspath(ClassLoader classLoader, String filePath) {

        try (InputStream input = getFileInputStreamFromClasspath(classLoader, filePath)) {
            return streamToString(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the InputStream from of a file's content.
     * @param filePath  the absolute or relative path to the file
     * @return File's contents as an InputStream
     */
    public static InputStream getFileInputStreamFromClasspath(String filePath) {
        return getFileInputStreamFromClasspath(FileUtils.class.getClassLoader(), filePath);
    }

    /**
     * Returns the InputStream from of a file's content.
     * @param classLoader  the super class's ClassLoader
     * @param filePath  the absolute or relative path to the file
     * @return File's contents as an InputStream
     */
    public static InputStream getFileInputStreamFromClasspath(ClassLoader classLoader, String filePath) {

        InputStream input = classLoader.getResourceAsStream(filePath);
        if (input == null) {
            input = classLoader.getResourceAsStream("/" + filePath);
            if (input == null) {
                throw new RuntimeException("File '" + filePath + "' not found in classpath.");
            }
        }
        return input;
    }

    /**
     * Writes (appends) the contents of an InputStream into a File
     * @param data  the InputStream containing the data to be written
     * @param file  the File that will be written into
     */
    public static void writeStreamToFile(InputStream data, File file) {
        try (OutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = data.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find file.", e);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file.", e);
        }
    }

    /**
     * Converts an InputStream into a String, delimited with breaklines.
     * @param input  the InputStream with the content
     * @return the String representation of the stream's content
     */
    public static String streamToString(InputStream input) {
        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            StringBuilder output = new StringBuilder();
            int read;
            while ((read = reader.read(buffer)) != -1) {
                output.append(buffer, 0, read);
            }
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading input stream.", e);
        }
    }

    /**
     * Deletes an entire directory, including any sub-folders and files
     * @param directory  the File pointing to the directory to be erased
     */
    public static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    boolean success = file.delete();
                    if (!success) {
                        LOGGER.warn("Could not delete file '" + file.getAbsolutePath() + "'.");
                    }
                }
            }
        }
        boolean success = directory.delete();
        if (!success) {
            LOGGER.warn("Could not delete directory '" + directory.getAbsolutePath() + "'.");
        }
    }

    /**
     * Receives an InputStream from a Zip file and extracts it into the destination directory
     * @param zipInputStream  the InputStream with the Zip files contents
     * @param destDirectory  the string URL for the base directory where the files will be extracted
     * @throws IOException when the destination directory is unreachable
     */
    public static void unzip(InputStream zipInputStream, String destDirectory) throws IOException {

        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(zipInputStream);
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    // Extracts ZipInputStream into the filepath
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}

package com.groupdocs.ui.viewer.manager;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class WebAssetsManager {

    /**
     * Update resources (js, css, html) to latest version
     * @param link resources url (GitHub)
     * @throws IOException
     */
    public void update(String link) throws IOException {
        try {
            // Temp directory
            File tempDirectory = new File("temp");

            // Project resources directory
            //String projectAssetsPath = "src/main/resources/assets";
            String targetAssetsPath = "target/classes/assets";

            // Download zip file
            System.out.println("DOWNLOADING FILES...");
            File zipFile = download(link);
            System.out.println("OK!");
            System.out.println();

            // Extract files from zip archive
            System.out.println("EXTRACTING FILES...");
            String inResourcesPath = unzip(zipFile, tempDirectory);
            System.out.println("OK!");
            System.out.println();

            // Clean project's resources directory
            System.out.println("CLEANING RESOURCE DIRECTORY...");
            //clean(projectAssetsPath);
            clean(targetAssetsPath);
            System.out.println("OK!");
            System.out.println();

            // Copy downloaded resources to project directory
            System.out.println("COPYING FILES...");
            // Copy files to project directory (as backup)
            //copy(inResourcesPath, projectAssetsPath);
            // Copy files to target directory to get latest changes without an application restart
            copy(inResourcesPath, targetAssetsPath);
            System.out.println("OK!");
            System.out.println();

            // Remove temp directory
            System.out.println("REMOVING TEMP DIRECTORY...");
            clean(tempDirectory);
            System.out.println("OK!");
            System.out.println();
        } catch (Exception ex){
            System.out.println("THERE IS NO INTERNET CONNECTION");
            System.out.println("BUILT IN RESOURCES WILL BE USED");
        }
    }

    /**
     * Download resources
     * @param link resources url (GitHub)
     * @return downloaded resources zip file
     * @throws IOException
     */
    private File download(String link) throws IOException{
        File zipFile = new File("temp.zip");
        URL url = new URL(link);
        // Open URL stream
        BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
        // Open output stream
        FileOutputStream fileOutputStreams = new FileOutputStream(zipFile);
        // Download file
        copyInputStream(bufferedInputStream, fileOutputStreams);
        return zipFile;
    }

    /**
     * Extract/unzip resources
     * @param inputFile resource zip file
     * @param tempDirectory location where to extract
     * @return path to resource directory (ex: temp/project/resources/)
     * @throws IOException
     */
    private String unzip(File inputFile, File tempDirectory) throws IOException {
        String resourcePath = null;
        // Open zip file
        ZipFile zipFile = new ZipFile(inputFile);
        Enumeration zipEntries = zipFile.entries();
        // Create directory for file decompression
        tempDirectory.mkdirs();
        // Go though all zip files and folders
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
            String entryPath = String.format("%s/%s", tempDirectory, zipEntry.getName());
            // Check if entry is directory
            if (zipEntry.isDirectory()) {
                System.out.println(" - Extracting directory: " + entryPath);
                if(entryPath.endsWith("resources/")){
                    resourcePath = entryPath;
                }
                // Create new directory
                new File(entryPath).mkdir();
                continue;
            }
            // Extract file
            System.out.println(" - Extracting file: " + entryPath);
            InputStream inputStream = zipFile.getInputStream(zipEntry);
            FileOutputStream fileOutputStream = new FileOutputStream(entryPath);
            copyInputStream(inputStream, fileOutputStream);
        }
        // Close zip file
        zipFile.close();
        // Remove zip file
        inputFile.delete();
        return resourcePath;
    }

    /**
     * Clean directory
     * @param directory directory to be cleaned out
     * @throws IOException
     */
    private void clean(String directory) throws IOException {
        clean(new File(directory));
    }

    /**
     * Clean directory
     * @param directory  directory to be cleaned out
     * @throws IOException
     */
    private void clean(File directory) throws IOException {
        FileUtils.deleteDirectory(directory);
    }

    /**
     * Copy resources from one location to another
     * @param fromDir directory where resources are located
     * @param toDir directory where resources will be copied
     */
    private void copy(String fromDir, String toDir) throws IOException{
        File rootDir = new File(fromDir);
        // Loop thought all files and folders
        for(String path : rootDir.list()){

            File file = new File(String.format("%s/%s", rootDir, path));
            String destinationPath = String.format("%s/%s", toDir, path);
            if(file.isDirectory()){
                // Create directories
                System.out.println(" - Creating directory: " + destinationPath);
                new File(destinationPath).mkdirs();
                copy(file.getPath(), destinationPath);
            }else{
                if(!file.isHidden()) {
                    // Copy files
                    System.out.println(" - Copying file: " + destinationPath);
                    FileInputStream fileInputStream = new FileInputStream(file);
                    FileOutputStream fileOutputStreams = new FileOutputStream(destinationPath);
                    copyInputStream(fileInputStream, fileOutputStreams);
                }
            }
        }
    }

    /**
     * Copy input stream to output stream
     * @param in input stream
     * @param out output stream
     * @throws IOException
     */
    private void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        // Copy from input to output stream
        while ((length = in.read(buffer)) >= 0) {
            out.write(buffer, 0, length);
        }
        // Close InputStream
        in.close();
        // Close OutputStream
        out.close();
    }

}

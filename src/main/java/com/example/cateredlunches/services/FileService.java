package com.example.cateredlunches.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to retrieve data from a file.  The file should be a JSON representation.
 */
@Component
@ConfigurationProperties(prefix = "data.access")
public class FileService {
    private final Gson gson = new Gson();

    private String storageDirectory;

    /**
     * Reads the dataFile file and returns a list of entries.
     * @param dataFile - The file to read
     * @param dataClazz - Class of the JSON object.
     * @param <D> - Return class type
     * @return - A lit of D type items found in the dataFile
     * @throws IOException - Thrown when problem with reading the file.
     */
    public <D> List<D> readFile(String dataFile, Class<D> dataClazz) throws IOException {
        File dataStorage = getDataStorage(dataFile);
        Type listType = new TypeToken<ArrayList<D>>() {}.getType();
        ArrayList<D> objects = gson.fromJson(new FileReader(dataStorage), listType);
        ArrayList<D> fileContents = new ArrayList<>();
        if(objects != null) {
            for(Object object : objects) {
                fileContents.add((D) gson.fromJson(gson.toJson(object), dataClazz));
            }
        }
        return fileContents;
    }

    /**
     * Takes a List and saves it off to a dataFile file
     * @param dataFile - The file to save to
     * @param data - Data to save to file.
     * @param <D> - Class of the data
     * @throws IOException - Thrown when problem writing to file
     */
    public <D> void safeToFile(String dataFile, List<D> data) throws IOException {
        File dataStorage = getDataStorage(dataFile);
        FileWriter fw = new FileWriter(dataStorage);
        gson.toJson(data, fw);
        fw.flush();
        fw.close();
    }

    /*
    Will get the File representation of the dataFile.  Uses the common configuration for the base
    directory and will create the file if it does not exist.
     */
    private File getDataStorage(String dataFile) throws IOException {
        File dataStorage = new File(storageDirectory, dataFile);
        if(!dataStorage.exists()) {
            dataStorage.createNewFile();
            dataStorage.setWritable(true, false);
            dataStorage.setReadable(true, false);
        }
        return dataStorage;
    }

    public String getStorageDirectory() {
        return storageDirectory;
    }

    public void setStorageDirectory(String storageDirectory) {
        this.storageDirectory = storageDirectory;
    }
}

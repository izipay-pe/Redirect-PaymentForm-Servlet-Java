package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class McwProperties {
    private Properties properties = new Properties();

    public McwProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}

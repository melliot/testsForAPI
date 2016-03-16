package co.wiseweb.tests.cms;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class ConfigProperties {

    public static Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        URL properties = ClassLoader.getSystemResource("config.properties");
        try {
            PROPERTIES.load(properties.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }
}

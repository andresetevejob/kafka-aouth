package org.andre.strimzi.oauth.common;

import java.util.Properties;

public class ConfigProperties {
    private Properties defaults;
    private Config config;

    public ConfigProperties(Properties defaults) {
        this.defaults = defaults;
        config = new Config(defaults);
    }

    public Properties resolveTo(Properties destination) {
        for (Object key: defaults.keySet()) {
            destination.setProperty(key.toString(), config.getValue(key.toString()));
        }
        return destination;
    }

    public static void resolveAndExportToSystemProperties(Properties defaults) {
        Properties p = new ConfigProperties(defaults).resolveTo(new Properties());
        for (Object key: p.keySet()) {
            System.setProperty(key.toString(), p.getProperty(key.toString()));
        }
    }

    public static Properties resolve(Properties defaults) {
        return new ConfigProperties(defaults).resolveTo(new Properties());
    }
}

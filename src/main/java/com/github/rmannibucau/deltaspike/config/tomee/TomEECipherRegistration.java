package com.github.rmannibucau.deltaspike.config.tomee;

import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.openejb.loader.SystemInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TomEECipherRegistration implements ConfigSource {
    private final Map<String, String> properties;

    public TomEECipherRegistration() {
        properties = new HashMap<>();
        final Properties props = SystemInstance.get().getProperties();
        for (final String key : props.stringPropertyNames()) {
            properties.put(key, props.getProperty(key, ""));
        }

        // we are in the right classloader to add them now
        Ciphers.register();
    }

    @Override
    public int getOrdinal() {
        return 250; // less than env and system props but more than JNDI or app files
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getPropertyValue(final String key) {
        return properties.get(key);
    }

    @Override
    public String getConfigName() {
        return "deltaspike-configuration-tomee";
    }

    @Override
    public boolean isScannable() {
        return true;
    }
}

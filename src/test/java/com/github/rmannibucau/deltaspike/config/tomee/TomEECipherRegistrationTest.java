package com.github.rmannibucau.deltaspike.config.tomee;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.impl.config.ConfigurationExtension;
import org.apache.deltaspike.core.impl.config.DefaultConfigPropertyProducer;
import org.apache.openejb.cipher.StaticDESPasswordCipher;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.CdiExtensions;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@CdiExtensions(ConfigurationExtension.class)
@RunWith(ApplicationComposer.class)
public class TomEECipherRegistrationTest {
    @Module
    @Classes(cdi = true, innerClassesAsBean = true, value = DefaultConfigPropertyProducer.class)
    public WebApp app() {
        return new WebApp();
    }

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
            .p("myconfig", "cipher:Static3DES:qrxDWArrKEU=")
            .p("myconfig.long", "cipher:" + StaticDESPasswordCipher.class.getName() + ":qrxDWArrKEU=")
            .build();
    }

    @Inject
    private IAmConfigured configured;

    @Test
    public void openejb() {
        // @Dependent so ok to access fields
        assertEquals("openejb", configured.openejb1);
        assertEquals("openejb", configured.openejb2);
    }

    public static class IAmConfigured {
        @Inject
        @ConfigProperty(name = "myconfig")
        private String openejb1;

        @Inject
        @ConfigProperty(name = "myconfig.long")
        private String openejb2;
    }
}

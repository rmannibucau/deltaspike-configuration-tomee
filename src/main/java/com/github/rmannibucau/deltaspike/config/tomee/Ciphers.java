package com.github.rmannibucau.deltaspike.config.tomee;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.spi.config.ConfigFilter;
import org.apache.openejb.cipher.PasswordCipher;
import org.apache.xbean.finder.ResourceFinder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Ciphers {
    private static final String CIPHER_PREFIX = "cipher:";

    private Ciphers() {
        // no-op
    }

    public static void register() {
        final ResourceFinder finder = new ResourceFinder("META-INF/");
        try {
            for (final Map.Entry<String, Class<? extends PasswordCipher>> entry : finder.mapAllImplementations(PasswordCipher.class).entrySet()) {
                final String prefix = prefix(entry.getKey());
                final int offset = prefix.length();
                final Class<? extends PasswordCipher> clazz = entry.getValue();
                ConfigResolver.addConfigFilter(new ConfigFilter() {
                    @Override
                    public String filterValue(final String key, final String value) {
                        if (!isFiltered(prefix, value)) {
                            return value;
                        }
                        try {
                            return clazz.newInstance().decrypt(value.substring(offset).toCharArray());
                        } catch (final InstantiationException | IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        }
                    }

                    @Override
                    public String filterValueForLog(final String key, final String value) {
                        return value;
                    }
                });
            }
        } catch (final Throwable ignore) {
            // skip
        }

        // old API, there are still few impl
        try {
            for (final Map.Entry<String, Class<? extends org.apache.openejb.resource.jdbc.cipher.PasswordCipher>> entry :
                finder.mapAllImplementations(org.apache.openejb.resource.jdbc.cipher.PasswordCipher.class).entrySet()) {
                final String prefix = prefix(entry.getKey());
                final int offset = prefix.length();
                final Class<? extends org.apache.openejb.resource.jdbc.cipher.PasswordCipher> clazz = entry.getValue();
                ConfigResolver.addConfigFilter(new ConfigFilter() {
                    @Override
                    public String filterValue(final String key, final String value) {
                        if (!isFiltered(prefix, value)) {
                            return value;
                        }
                        try {
                            return clazz.newInstance().decrypt(value.substring(offset).toCharArray());
                        } catch (final InstantiationException | IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        }
                    }

                    @Override
                    public String filterValueForLog(final String key, final String value) {
                        return value;
                    }
                });
            }
        } catch (final Throwable ignore) {
            // skip
        }

        // fallback when no SPI file is used
        try {
            final ConcurrentMap<String, Class<? extends PasswordCipher>> classCache = new ConcurrentHashMap<>();
            final int classIndexStart = CIPHER_PREFIX.length();
            ConfigResolver.addConfigFilter(new ConfigFilter() {
                @Override
                public String filterValue(final String key, final String value) {
                    if (value == null || !value.startsWith(CIPHER_PREFIX)) {
                        return value;
                    }
                    final int sep = value.indexOf(':', classIndexStart);
                    if (sep <= 0) {
                        return value;
                    }

                    final String classname = value.substring(classIndexStart, sep);
                    Class<? extends PasswordCipher> cached = classCache.get(classname);
                    if (cached == null) {
                        try {
                            cached = Class.class.cast(Thread.currentThread().getContextClassLoader().loadClass(classname));
                        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
                            cached = Skip.class;
                        }
                    }
                    if (cached == Skip.class) {
                        return value;
                    }

                    try {
                        return cached.newInstance().decrypt(value.substring(sep + 1).toCharArray());
                    } catch (final InstantiationException | IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }

                @Override
                public String filterValueForLog(final String key, final String value) {
                    return value;
                }
            });
        } catch (final Throwable ignore) {
            // no-op
        }
    }

    private static String prefix(final String key) {
        return CIPHER_PREFIX + key + ":";
    }

    private static boolean isFiltered(final String prefix, final String value) {
        return value != null && value.startsWith(prefix);
    }

    private static class Skip implements PasswordCipher {
        @Override
        public char[] encrypt(String plainPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String decrypt(final char[] encryptedPassword) {
            throw new UnsupportedOperationException();
        }
    }
}

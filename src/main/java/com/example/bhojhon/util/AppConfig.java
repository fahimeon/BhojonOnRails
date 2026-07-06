package com.example.bhojhon.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central configuration loader for secrets and environment-specific settings.
 *
 * <p>Lookups resolve in this order (first non-blank wins):
 * <ol>
 *   <li>JVM system property ({@code -Dmail.smtp.user=...})</li>
 *   <li>Environment variable (key upper-cased, dots to underscores: {@code MAIL_SMTP_USER})</li>
 *   <li>{@code config.properties} in the working directory (gitignored)</li>
 *   <li>{@code /config.properties} on the classpath (optional bundled defaults)</li>
 *   <li>the supplied default</li>
 * </ol>
 *
 * <p>This keeps credentials (SMTP password, OCR API key) out of source control.
 * See {@code config.properties.example} for the expected keys.
 */
public final class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties PROPS = new Properties();
    private static volatile boolean loaded = false;

    private AppConfig() {
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;

        Path external = Path.of("config.properties");
        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                PROPS.load(in);
                LOGGER.info("Loaded configuration from " + external.toAbsolutePath());
                return;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to read config.properties", e);
            }
        }

        try (InputStream in = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                PROPS.load(in);
                LOGGER.info("Loaded bundled configuration from classpath /config.properties");
            } else {
                LOGGER.info("No config.properties found; relying on env vars / defaults.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read bundled config.properties", e);
        }
    }

    /** Returns the configured value for {@code key}, or {@code defaultValue} if unset. */
    public static String get(String key, String defaultValue) {
        ensureLoaded();

        String sys = System.getProperty(key);
        if (isSet(sys)) {
            return sys;
        }
        String env = System.getenv(toEnvKey(key));
        if (isSet(env)) {
            return env;
        }
        String val = PROPS.getProperty(key);
        if (isSet(val)) {
            return val;
        }
        return defaultValue;
    }

    /** Returns the configured value for {@code key}, or an empty string if unset. */
    public static String get(String key) {
        return get(key, "");
    }

    /** True when {@code key} resolves to a non-blank value. */
    public static boolean has(String key) {
        return isSet(get(key, ""));
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    private static String toEnvKey(String key) {
        return key.toUpperCase().replace('.', '_').replace('-', '_');
    }
}

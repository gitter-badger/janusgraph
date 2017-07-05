// Copyright 2017 JanusGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.janusgraph.diskstorage.cassandra;

import static org.janusgraph.diskstorage.cassandra.AbstractCassandraStoreManager.*;
import static org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.cassandra.service.CassandraDaemon;
import org.apache.commons.io.FileUtils;
import org.janusgraph.diskstorage.configuration.ConfigElement;
import org.janusgraph.diskstorage.configuration.ModifiableConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class CassandraInitialiser {

    public static final Logger LOGGER = LoggerFactory.getLogger(CassandraInitialiser.class);

    public static final String HOSTNAME = System.getProperty(ConfigElement.getPath(STORAGE_HOSTS));

    public static AtomicBoolean INITIALISED = new AtomicBoolean(false);

    public static void initialiseCassandra(Class<?> testClass) {
        initialiseCassandra(testClass, true);
    }

    public static void initialiseCassandra(Class<?> testClass, boolean startCassandra) {
        if (!startCassandra || HOSTNAME != null) {
            return;
        }

        synchronized (CassandraInitialiser.class) {
            if (INITIALISED.get()) {
                return;
            }
            try {
                File cassandraYaml = setupCassandraDirectory(testClass);
                if (startCassandra) {
                    startCassandra(cassandraYaml);
                }
                INITIALISED.set(true);
            } catch (Exception e) {
                throw new RuntimeException("Exception starting Cassandra", e);
            }
        }
    }

    private static File setupCassandraDirectory(Class<?> testClass) throws Exception {
        LOGGER.info("Initialising Cassandra with {} configuration", getCassandraConfiguration());
        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setClassForTemplateLoading(CassandraInitialiser.class, "/templates");
        final Template template = configuration.getTemplate("cassandra.yaml.ftl");

        final File cassandraDir = getCassandraDir(testClass);
        cassandraDir.mkdirs();
        FileUtils.cleanDirectory(cassandraDir);
        new File(cassandraDir, "data").mkdir();
        new File(cassandraDir, "commitlog").mkdir();
        new File(cassandraDir, "saved_caches").mkdir();
        final File cassandraYaml = getCassandraConfFile(cassandraDir);
        Files.createParentDirs(cassandraYaml);

        final StringWriter writer = new StringWriter();
        template.process(getCassandraConfiguration().getConfiguration(cassandraDir), writer);
        Files.write(writer.toString(), cassandraYaml, Charset.forName("UTF-8"));
        return cassandraYaml;
    }

    private static void startCassandra(final File cassandraYaml) {
        System.setProperty("cassandra.jmx.local.port", "7199");
        System.setProperty("cassandra.config", cassandraYaml.toURI().toString());
        System.setProperty("cassandra-foreground", "yes");

        CassandraDaemon.main(new String[0]);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("VM terminating, stopping Cassandra");
            CassandraDaemon.stop(new String[0]);
        }, "Cassandra Terminator"));
    }

    private static File getCassandraConfFile(final File cassandraDir) {
        return Paths.get(cassandraDir.getAbsolutePath(), "conf", "cassandra.yaml").toFile();
    }

    public static File getCassandraDir(Class<?> testClass) throws URISyntaxException {
        return new File(new File(testClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), "cassandra");
    }

    public static CassandraConfiguration getCassandraConfiguration() {
        String config = System.getProperty("CASSANDRA_CONFIG");
        if (config == null) {
            return CassandraConfiguration.UNORDERED;
        }
        return CassandraConfiguration.valueOf(config);
    }

    public enum CassandraConfiguration {
        ORDERED {
            @Override
            public Map<Object, Object> getConfiguration(final File cassandraDir) {
                return ImmutableMap.builder()
                        .put("listenAddress", "127.0.0.1")
                        .put("initialToken", "0000000000000000000000000000000000")
                        .put("partitioner", "org.apache.cassandra.dht.ByteOrderedPartitioner")
                        .put("cassandraHome", cassandraDir.getAbsolutePath())
                        .build();
            }
        },
        UNORDERED {
            @Override
            public Map<Object, Object> getConfiguration(final File cassandraDir) {
                return ImmutableMap.builder()
                        .put("listenAddress", "127.0.0.1")
                        .put("partitioner", "org.apache.cassandra.dht.Murmur3Partitioner")
                        .put("cassandraHome", cassandraDir.getAbsolutePath())
                        .build();
            }
        },
        UNORDERED_SSL {
            @Override
            public Map<Object, Object> getConfiguration(final File cassandraDir) throws IOException {
                final File keyStoreLocation = Paths.get(cassandraDir.getAbsolutePath(), "conf", "test.keystore").toFile();
                try (
                        InputStream is = getClass().getResourceAsStream("/ssl/test.keystore");
                        FileOutputStream os = new FileOutputStream(keyStoreLocation)) {
                    ByteStreams.copy(is, os);
                }

                final File trustStoreLocation = Paths.get(cassandraDir.getAbsolutePath(), "conf", "test.truststore").toFile();
                try (
                        InputStream is = getClass().getResourceAsStream("/ssl/test.truststore");
                        FileOutputStream os = new FileOutputStream(trustStoreLocation)) {
                    ByteStreams.copy(is, os);
                }

                return ImmutableMap.builder()
                        .putAll(UNORDERED.getConfiguration(cassandraDir))
                        .put("enableSSL", "true")
                        .put("keyStoreLocation", keyStoreLocation.getAbsolutePath())
                        .put("keyStorePassword", "cassandra")
                        .build();
            }

            @Override
            public ModifiableConfiguration merge(Class<?> testClass, ModifiableConfiguration configuration) throws Exception {
                super.merge(testClass, configuration);
                configuration.set(SSL_ENABLED, true);
                configuration.set(SSL_TRUSTSTORE_LOCATION, Paths.get(getCassandraDir(testClass).getAbsolutePath(), "conf", "test.truststore").toFile().getAbsolutePath());
                configuration.set(SSL_TRUSTSTORE_PASSWORD, "cassandra");
                return configuration;
            }
        };

        public abstract Map<Object, Object> getConfiguration(File cassandraDir) throws IOException;

        public ModifiableConfiguration merge(Class<?> testClass, ModifiableConfiguration config) throws Exception {
            config.set(STORAGE_CONF_FILE, getCassandraConfFile(getCassandraDir(testClass)).getAbsolutePath());
            config.set(STORAGE_HOSTS, new String[] { HOSTNAME != null ? HOSTNAME : "127.0.0.1" });
            return config;
        }
    }
}

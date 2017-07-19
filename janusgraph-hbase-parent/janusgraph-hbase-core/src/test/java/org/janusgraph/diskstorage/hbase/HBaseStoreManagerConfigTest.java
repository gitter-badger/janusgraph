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

package org.janusgraph.diskstorage.hbase;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.janusgraph.HBaseStorageSetup;
import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.configuration.BasicConfiguration;
import org.janusgraph.diskstorage.configuration.ConfigElement;
import org.janusgraph.diskstorage.configuration.WriteConfiguration;
import org.janusgraph.diskstorage.keycolumnvalue.KeyColumnValueStore;
import org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;

public class HBaseStoreManagerConfigTest {

    @BeforeClass
    public static void startHBase() throws IOException, BackendException {
        HBaseStorageSetup.startHBase();
    }

    @AfterClass
    public static void stopHBase() {
        // No op. HBase stopped by shutdown hook registered by startHBase().

    }

    @Test
    public void testShortCfNames() throws Exception {
        Logger log = (Logger) LoggerFactory.getLogger(HBaseStoreManager.class);
        Level savedLevel = log.getLevel();
        log.setLevel(Level.WARN);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        final OutputStreamAppender<ILoggingEvent> outputStreamAppender = new OutputStreamAppender<ILoggingEvent>();
        outputStreamAppender.setOutputStream(outputStream);

        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%p: %m%n");
        outputStreamAppender.setEncoder(encoder);

        log.addAppender(outputStreamAppender);

        // Open the HBaseStoreManager and store with default SHORT_CF_NAMES true.
        WriteConfiguration config = HBaseStorageSetup.getHBaseGraphConfiguration();
        HBaseStoreManager manager = new HBaseStoreManager(new BasicConfiguration(GraphDatabaseConfiguration.ROOT_NS, config, BasicConfiguration.Restriction.NONE));
        KeyColumnValueStore store = manager.openDatabase(GraphDatabaseConfiguration.SYSTEM_PROPERTIES_STORE_NAME);

        store.close();
        manager.close();

        // Open the HBaseStoreManager and store with SHORT_CF_NAMES false.
        config.set(ConfigElement.getPath(HBaseStoreManager.SHORT_CF_NAMES), false);
        manager = new HBaseStoreManager(new BasicConfiguration(GraphDatabaseConfiguration.ROOT_NS, config, BasicConfiguration.Restriction.NONE));

        outputStream.reset();
        store = manager.openDatabase(GraphDatabaseConfiguration.SYSTEM_PROPERTIES_STORE_NAME);

        // Verify we get WARN.
        String message = new String(outputStream.toByteArray(), Charsets.UTF_8);

        assertTrue(message, message.toString().startsWith("WARN: Configuration"));
        log.detachAppender(outputStreamAppender);
        log.setLevel(savedLevel);

        store.close();
        manager.close();
    }

}

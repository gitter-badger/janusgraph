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

import java.time.Duration;

import org.janusgraph.diskstorage.StandardStoreManager;
import org.janusgraph.diskstorage.cassandra.CassandraInitialiser.CassandraConfiguration;
import org.janusgraph.diskstorage.configuration.ModifiableConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class CassandraGraphConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraGraphConfiguration.class);

    public static ModifiableConfiguration getConfiguration(Class<?> testClass, StandardStoreManager standardStoreManager) {
        return getConfiguration(testClass.getSimpleName(), testClass, standardStoreManager);
    }

    public static ModifiableConfiguration getConfiguration(String graphName, Class<?> testClass, StandardStoreManager standardStoreManager) {
        ModifiableConfiguration config = buildGraphConfiguration();
        config.set(CASSANDRA_KEYSPACE, cleanKeyspaceName(graphName));
        LOGGER.debug("Set keyspace name: {}", config.get(CASSANDRA_KEYSPACE));
        config.set(PAGE_SIZE, 500);
        config.set(CONNECTION_TIMEOUT, Duration.ofSeconds(60L));
        config.set(STORAGE_BACKEND, standardStoreManager.getShorthands().get(0));
        // Set to 3 because we have a 2.1.9 database that only supports version 3, if we let it negotiate then there are spurious errors.
        config.set(PROTOCOL_VERSION, 3);
        CassandraConfiguration cassandraConfiguration = CassandraInitialiser.getCassandraConfiguration();
        try {
            return cassandraConfiguration.merge(testClass, config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String cleanKeyspaceName(String raw) {
        Preconditions.checkNotNull(raw);
        Preconditions.checkArgument(0 < raw.length());

        if (48 < raw.length() || raw.matches("^.*[^a-zA-Z0-9_].*$")) {
            return "strhash" + String.valueOf(Math.abs(raw.hashCode()));
        } else {
            return raw;
        }
    }
}

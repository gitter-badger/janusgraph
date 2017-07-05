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

package org.janusgraph.diskstorage.cassandra.embedded;

import static org.janusgraph.diskstorage.cassandra.CassandraInitialiser.initialiseCassandra;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.StandardStoreManager;
import org.janusgraph.diskstorage.cassandra.AbstractCassandraStoreManager;
import org.janusgraph.diskstorage.cassandra.AbstractCassandraStoreTest;
import org.janusgraph.diskstorage.cassandra.CassandraGraphConfiguration;
import org.janusgraph.diskstorage.configuration.Configuration;
import org.janusgraph.diskstorage.configuration.ModifiableConfiguration;
import org.janusgraph.diskstorage.keycolumnvalue.StoreFeatures;
import org.junit.BeforeClass;
import org.junit.Test;

public class EmbeddedStoreTest extends AbstractCassandraStoreTest {

    @BeforeClass
    public static void startCassandra() {
        initialiseCassandra(EmbeddedStoreTest.class, false);
    }

    @Override
    public ModifiableConfiguration getBaseStorageConfiguration() {
        return CassandraGraphConfiguration.getConfiguration(getClass(), StandardStoreManager.CASSANDRA_EMBEDDED);
    }

    @Override
    public AbstractCassandraStoreManager openStorageManager(Configuration c) throws BackendException {
        return new CassandraEmbeddedStoreManager(c);
    }

    @Test
    public void testConfiguration() {
        assumeTrue("Store is ordered", this.manager.getFeatures().isKeyOrdered());
        StoreFeatures features = manager.getFeatures();
        assertTrue(features.isKeyOrdered());
        assertTrue(features.hasLocalKeyPartition());
    }
}

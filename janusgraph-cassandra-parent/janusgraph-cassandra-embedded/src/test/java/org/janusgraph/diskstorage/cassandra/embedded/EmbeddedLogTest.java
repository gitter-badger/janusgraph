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

import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.StandardStoreManager;
import org.janusgraph.diskstorage.cassandra.CassandraGraphConfiguration;
import org.janusgraph.diskstorage.keycolumnvalue.KeyColumnValueStoreManager;
import org.janusgraph.diskstorage.log.KCVSLogTest;
import org.janusgraph.testcategory.SerialTests;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

@Category(SerialTests.class)
public class EmbeddedLogTest extends KCVSLogTest {

    @BeforeClass
    public static void startCassandra() {
        initialiseCassandra(EmbeddedLogTest.class, false);
    }

    @Override
    public KeyColumnValueStoreManager openStorageManager() throws BackendException {
        return new CassandraEmbeddedStoreManager(CassandraGraphConfiguration.getConfiguration(getClass(), StandardStoreManager.CASSANDRA_EMBEDDED));
    }
}
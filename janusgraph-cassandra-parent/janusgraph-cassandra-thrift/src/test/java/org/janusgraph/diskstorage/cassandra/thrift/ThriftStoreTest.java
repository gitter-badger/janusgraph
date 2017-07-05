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

package org.janusgraph.diskstorage.cassandra.thrift;

import static org.janusgraph.diskstorage.cassandra.CassandraInitialiser.initialiseCassandra;

import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.StandardStoreManager;
import org.janusgraph.diskstorage.cassandra.AbstractCassandraStoreManager;
import org.janusgraph.diskstorage.cassandra.AbstractCassandraStoreTest;
import org.janusgraph.diskstorage.cassandra.CassandraGraphConfiguration;
import org.janusgraph.diskstorage.configuration.Configuration;
import org.janusgraph.diskstorage.configuration.ModifiableConfiguration;
import org.junit.BeforeClass;

public class ThriftStoreTest extends AbstractCassandraStoreTest {

    @BeforeClass
    public static void startCassandra() {
        initialiseCassandra(ThriftStoreTest.class);
    }

    @Override
    public ModifiableConfiguration getBaseStorageConfiguration() {
        return CassandraGraphConfiguration.getConfiguration(getClass(), StandardStoreManager.CASSANDRA_THRIFT);
    }

    @Override
    public AbstractCassandraStoreManager openStorageManager(Configuration c) throws BackendException {
        return new CassandraThriftStoreManager(c);
    }
}

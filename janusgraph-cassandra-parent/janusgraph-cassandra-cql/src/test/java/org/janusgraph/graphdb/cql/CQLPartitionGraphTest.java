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

package org.janusgraph.graphdb.cql;

import static org.janusgraph.diskstorage.cassandra.CassandraInitialiser.initialiseCassandra;

import org.janusgraph.diskstorage.StandardStoreManager;
import org.janusgraph.diskstorage.cassandra.CassandraGraphConfiguration;
import org.janusgraph.diskstorage.configuration.WriteConfiguration;
import org.janusgraph.graphdb.JanusGraphPartitionGraphTest;
import org.junit.BeforeClass;

public class CQLPartitionGraphTest extends JanusGraphPartitionGraphTest {

    @BeforeClass
    public static void beforeClass() {
        initialiseCassandra(CQLPartitionGraphTest.class);
    }

    @Override
    public WriteConfiguration getBaseConfiguration() {
        return CassandraGraphConfiguration.getConfiguration(getClass(), StandardStoreManager.CQL).getConfiguration();
    }
}

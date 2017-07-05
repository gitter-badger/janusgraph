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

package org.janusgraph.graphdb.embedded;

import static org.janusgraph.diskstorage.cassandra.CassandraInitialiser.initialiseCassandra;

import org.janusgraph.diskstorage.StandardStoreManager;
import org.janusgraph.diskstorage.cassandra.CassandraGraphConfiguration;
import org.janusgraph.diskstorage.configuration.WriteConfiguration;
import org.janusgraph.graphdb.JanusGraphConcurrentTest;
import org.janusgraph.testcategory.PerformanceTests;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */

@Category({PerformanceTests.class})
public class EmbeddedGraphConcurrentTest extends JanusGraphConcurrentTest {

    @BeforeClass
    public static void startEmbeddedCassandra() {
        initialiseCassandra(EmbeddedGraphConcurrentTest.class, false);
    }

    @Override
    public WriteConfiguration getConfiguration() {
        return CassandraGraphConfiguration.getConfiguration(getClass(), StandardStoreManager.CASSANDRA_EMBEDDED).getConfiguration();
    }

}

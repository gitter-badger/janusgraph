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

package org.janusgraph.diskstorage.cql;

import static org.janusgraph.diskstorage.cassandra.CassandraInitialiser.*;
import static org.junit.Assert.assertTrue;

import org.janusgraph.diskstorage.cassandra.CassandraInitialiser.CassandraConfiguration;
import org.janusgraph.testcategory.CassandraSSLTests;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

@Category({ CassandraSSLTests.class })
public class CQLSSLStoreTest extends CQLStoreTest {

    @BeforeClass
    public static void startCassandra() {
        initialiseCassandra(CQLSSLStoreTest.class);
        assertTrue("Cassandra is not running with SSL configured", CassandraConfiguration.UNORDERED_SSL == getCassandraConfiguration());
    }
}

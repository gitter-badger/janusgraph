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

import static org.janusgraph.diskstorage.cassandra.CassandraInitialiser.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.DistributedStoreManagerTest;
import org.janusgraph.diskstorage.StandardStoreManager;
import org.janusgraph.diskstorage.cassandra.CassandraGraphConfiguration;
import org.janusgraph.diskstorage.common.DistributedStoreManager.Deployment;
import org.janusgraph.testcategory.OrderedKeyStoreTests;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ThriftDistributedStoreManagerTest extends DistributedStoreManagerTest<CassandraThriftStoreManager> {

    @BeforeClass
    public static void startCassandra() {
        initialiseCassandra(ThriftDistributedStoreManagerTest.class);
    }

    @Before
    public void setUp() throws BackendException {
        manager = new CassandraThriftStoreManager(CassandraGraphConfiguration.getConfiguration(getClass(), StandardStoreManager.CASSANDRA_THRIFT));
        store = manager.openDatabase("distributedcf");
    }

    @After
    public void tearDown() throws BackendException {
        if (null != manager)
            manager.close();
    }

    @Override
    @Test
    @Category({ OrderedKeyStoreTests.class })
    public void testGetDeployment() {
        assumeTrue("Store is ordered", this.manager.getFeatures().isKeyOrdered());
        final Deployment deployment = HOSTNAME == null ? Deployment.LOCAL : Deployment.REMOTE;
        assertEquals(deployment, manager.getDeployment());
    }
}

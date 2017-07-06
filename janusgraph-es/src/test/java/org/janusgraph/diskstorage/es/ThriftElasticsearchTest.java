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

package org.janusgraph.diskstorage.es;


import static org.janusgraph.diskstorage.es.ElasticSearchIndex.*;
import static org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration.INDEX_HOSTS;

import org.janusgraph.diskstorage.StandardStoreManager;
import org.janusgraph.diskstorage.cassandra.CassandraGraphConfiguration;
import org.janusgraph.diskstorage.cassandra.CassandraInitialiser;
import org.janusgraph.diskstorage.configuration.ModifiableConfiguration;
import org.janusgraph.diskstorage.configuration.WriteConfiguration;
import org.janusgraph.graphdb.JanusGraphIndexTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ThriftElasticsearchTest extends JanusGraphIndexTest {

    private static ElasticsearchRunner esr;

    @BeforeClass
    public static void startElasticsearch() {
        CassandraInitialiser.initialiseCassandra(ThriftElasticsearchTest.class);
        if (!ElasticsearchRunner.IS_EXTERNAL) {
            esr = new ElasticsearchRunner();
            esr.start();
        }
    }

    @AfterClass
    public static void stopElasticsearch() {
        if (!ElasticsearchRunner.IS_EXTERNAL) {
            esr.stop();
        }
    }

    public ThriftElasticsearchTest() {
        super(true, true, true);
    }

    @Override
    public WriteConfiguration getConfiguration() {
        ModifiableConfiguration config = CassandraGraphConfiguration.getConfiguration(ThriftElasticsearchTest.class, StandardStoreManager.CASSANDRA_THRIFT);
        //Add index
        config.set(INTERFACE, ElasticSearchSetup.REST_CLIENT.toString(), INDEX);
        config.set(INDEX_HOSTS, new String[]{ "127.0.0.1" }, INDEX);
        config.set(BULK_REFRESH, "wait_for", INDEX);
        return config.getConfiguration();
    }

    @Override
    public boolean supportsLuceneStyleQueries() {
        return true;
    }

    @Override
    public boolean supportsWildcardQuery() {
        return true;
    }
    @Override
    protected boolean supportsCollections() {
        return true;
    }

}

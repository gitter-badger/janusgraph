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

import org.janusgraph.diskstorage.configuration.ConfigNamespace;
import org.janusgraph.diskstorage.configuration.ConfigOption;
import org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration;
import org.janusgraph.graphdb.configuration.PreInitializeConfigOptions;

/**
 * Configuration options for the CQL storage backend. These are managed under the 'cql' namespace in the configuration.
 */
@PreInitializeConfigOptions
public interface CQLConfigOptions {

    public static final ConfigNamespace CQL_NS = new ConfigNamespace(
            GraphDatabaseConfiguration.STORAGE_NS,
            "cql",
            "CQL storage backend options");

    public static final ConfigOption<Boolean> ONLY_USE_LOCAL_CONSISTENCY_FOR_SYSTEM_OPERATIONS =
        new ConfigOption<Boolean>(CQL_NS, "only-use-local-consistency-for-system-operations",
            "True to prevent any system queries from using QUORUM consistency " +
                "and always use LOCAL_QUORUM instead",
            ConfigOption.Type.MASKABLE, false);

    // The number of statements in a batch
    public static final ConfigOption<Integer> BATCH_STATEMENT_SIZE = new ConfigOption<>(
            CQL_NS,
            "batch-statement-size",
            "The number of statements in each batch",
            ConfigOption.Type.MASKABLE,
            20);

    // Other options
    public static final ConfigOption<String> CLUSTER_NAME = new ConfigOption<>(
            CQL_NS,
            "cluster-name",
            "Default name for the Cassandra cluster",
            ConfigOption.Type.MASKABLE,
            "JanusGraph Cluster");

    public static final ConfigOption<String> LOCAL_DATACENTER = new ConfigOption<>(
            CQL_NS,
            "local-datacenter",
            "The name of the local or closest Cassandra datacenter.  When set and not whitespace, " +
                    "this value will be passed into ConnectionPoolConfigurationImpl.setLocalDatacenter. " +
                    "When unset or set to whitespace, setLocalDatacenter will not be invoked.",
            /*
             * It's between either LOCAL or MASKABLE. MASKABLE could be useful for cases where all the JanusGraph instances are closest to
             * the same Cassandra DC.
             */
            ConfigOption.Type.MASKABLE,
            String.class);

}

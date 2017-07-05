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

import java.util.function.Function;

import org.apache.cassandra.thrift.ConsistencyLevel;
import org.janusgraph.diskstorage.BaseTransactionConfig;
import org.janusgraph.diskstorage.cassandra.AbstractCassandraTransaction;

public class CassandraThriftTransaction extends AbstractCassandraTransaction<ConsistencyLevel> {

    public CassandraThriftTransaction(BaseTransactionConfig config) {
        super(config, cl -> ConsistencyLevel.valueOf(cl));
    }
}
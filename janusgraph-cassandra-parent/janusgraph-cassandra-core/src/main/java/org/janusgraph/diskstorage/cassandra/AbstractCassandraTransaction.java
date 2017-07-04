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

package org.janusgraph.diskstorage.cassandra;

import static org.janusgraph.diskstorage.cassandra.AbstractCassandraStoreManager.CASSANDRA_READ_CONSISTENCY;
import static org.janusgraph.diskstorage.cassandra.AbstractCassandraStoreManager.CASSANDRA_WRITE_CONSISTENCY;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import org.janusgraph.diskstorage.BaseTransactionConfig;
import org.janusgraph.diskstorage.common.AbstractStoreTransaction;
import org.janusgraph.diskstorage.keycolumnvalue.StoreTransaction;

public abstract class AbstractCassandraTransaction<T> extends AbstractStoreTransaction {

    private static final Logger log = LoggerFactory.getLogger(AbstractCassandraTransaction.class);

    private final T read;
    private final T write;

    public AbstractCassandraTransaction(BaseTransactionConfig c, Function<String, T> mapper) {
        super(c);
        read = mapper.apply(getConfiguration().getCustomOption(CASSANDRA_READ_CONSISTENCY));
        write = mapper.apply(getConfiguration().getCustomOption(CASSANDRA_WRITE_CONSISTENCY));
        log.debug("Created {}", this.toString());
    }

    public T getReadConsistencyLevel() {
        return read;
    }

    public T getWriteConsistencyLevel() {
        return write;
    }

    public static <T extends AbstractCassandraTransaction> T getTx(StoreTransaction txh) {
        Preconditions.checkArgument(txh != null);
        Preconditions.checkArgument(txh instanceof AbstractCassandraTransaction, "Unexpected transaction type %s", txh.getClass().getName());
        return (T) txh;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("CassandraTransaction@");
        sb.append(Integer.toHexString(hashCode()));
        sb.append("[read=");
        sb.append(read);
        sb.append(",write=");
        sb.append(write);
        sb.append("]");
        return sb.toString();
    }
}

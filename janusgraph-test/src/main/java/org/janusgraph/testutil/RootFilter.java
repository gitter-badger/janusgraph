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

package org.janusgraph.testutil;

import java.util.Collections;

import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.Suite;
import org.junit.runners.model.RunnerBuilder;

public class RootFilter<T> extends Suite {

    private final Description suiteDescription;

    public RootFilter(Class<?> klass, RunnerBuilder builder) throws Exception {
        super(klass, Collections.singletonList(new AnnotatedBuilder(builder).buildRunner(baz(klass), klass)));
        suiteDescription = Description.createSuiteDescription(klass);
    }

    private static Class<? extends Runner> baz(Class<?> klass) {
        return klass.getAnnotation(ReallyRunWith.class).value();
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        if (!filter.shouldRun(suiteDescription)) {
            throw new NoTestsRemainException();
        }
        super.filter(filter);
    }
}

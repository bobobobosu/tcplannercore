/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bo.tc.tcplanner.persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractPersistable implements Serializable, Comparable<AbstractPersistable> {

    @JsonIgnore
    protected Integer id;

    @JsonIgnore
    protected boolean volatileFlag;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractPersistable that = (AbstractPersistable) o;

        return id.equals(that.id);
    }

    protected AbstractPersistable() {
        this.id = genId();
        this.volatileFlag = false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    protected AbstractPersistable(AbstractPersistable abstractPersistable) {
        if (abstractPersistable == null) {
            this.id = genId();
            this.volatileFlag = false;
        }
        this.id = genId();
        this.volatileFlag = abstractPersistable.volatileFlag;
    }

    protected AbstractPersistable(Integer id) {
        this.id = id;
    }

    @PlanningId
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    @Override
    public int compareTo(AbstractPersistable other) {
        return new CompareToBuilder()
                .append(getClass().getName(), other.getClass().getName())
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
    }

    public boolean isVolatileFlag() {
        return volatileFlag;
    }

    public AbstractPersistable setVolatileFlag(boolean volatileFlag) {
        this.volatileFlag = volatileFlag;
        return this;
    }

    abstract public AbstractPersistable removeVolatile();

    private final static AtomicInteger counter = new AtomicInteger();

    private synchronized static int genId() {
        return counter.incrementAndGet();
    }

}

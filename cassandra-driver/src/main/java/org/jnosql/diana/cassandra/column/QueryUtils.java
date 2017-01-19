/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jnosql.diana.cassandra.column;


import com.datastax.driver.core.querybuilder.*;
import org.jnosql.diana.api.Condition;
import org.jnosql.diana.api.Sort;
import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.ValueWriter;
import org.jnosql.diana.api.column.Column;
import org.jnosql.diana.api.column.ColumnCondition;
import org.jnosql.diana.api.column.ColumnEntity;
import org.jnosql.diana.api.column.ColumnQuery;
import org.jnosql.diana.api.writer.ValueWriterDecorator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.jnosql.diana.api.Sort.SortType.ASC;

final class QueryUtils {

    private static final ValueWriter VALUE_WRITER = ValueWriterDecorator.getInstance();

    private static final Function<Sort, Ordering> SORT_ORDERING_FUNCTION = sort -> {
        if (ASC.equals(sort.getType())) {
            return asc(sort.getName());
        } else {
            return desc(sort.getName());
        }
    };

    private QueryUtils() {
    }


    public static Insert insert(ColumnEntity entity, String keyspace) {
        Insert insert = insertInto(keyspace, entity.getName());
        entity.getColumns().forEach(d -> insert.value(d.getName(), convert(d.getValue())));
        return insert;
    }


    private static Object convert(Value value) {
        Object val = value.get();
        if (VALUE_WRITER.isCompatible(val.getClass())) {
            return VALUE_WRITER.write(val);
        }
        return val;
    }

    public static BuiltStatement add(ColumnQuery query, String keySpace) {
        String columnFamily = query.getColumnFamily();

        if (Objects.isNull(query.getCondition())) {
            return QueryBuilder.select().all().from(keySpace, columnFamily);
        }
        Select.Where where = QueryBuilder.select().all().from(keySpace, columnFamily).where();
        if (query.getLimit() > 0) {
            where.limit((int) query.getLimit());
        }
        if (!query.getSorts().isEmpty()) {
            where.orderBy(query.getSorts().stream().map(SORT_ORDERING_FUNCTION).toArray(Ordering[]::new));
        }
        List<Clause> clauses = new ArrayList<>();
        createClause(query.getCondition(), clauses);
        clauses.forEach(where::and);
        return where;
    }

    public static BuiltStatement delete(ColumnQuery query, String keySpace) {
        String columnFamily = query.getColumnFamily();

        if (Objects.isNull(query.getCondition())) {
            return QueryBuilder.delete().all().from(keySpace, query.getColumnFamily());
        }
        Delete.Where where = QueryBuilder.delete().all().from(keySpace, query.getColumnFamily()).where();
        List<Clause> clauses = new ArrayList<>();
        createClause(query.getCondition(), clauses);
        clauses.forEach(where::and);
        return where;
    }

    private static void createClause(ColumnCondition columnCondition, List<Clause> clauses) {
        Column column = columnCondition.getColumn();
        Condition condition = columnCondition.getCondition();
        Object value = column.getValue().get();
        switch (condition) {
            case EQUALS:
                clauses.add(QueryBuilder.eq(column.getName(), value));
                return;
            case GREATER_THAN:
                clauses.add(QueryBuilder.gt(column.getName(), value));
                return;
            case GREATER_EQUALS_THAN:
                clauses.add(QueryBuilder.gte(column.getName(), value));
                return;
            case LESSER_THAN:
                clauses.add(QueryBuilder.lt(column.getName(), value));
                return;
            case LESSER_EQUALS_THAN:
                clauses.add(QueryBuilder.lte(column.getName(), value));
                return;
            case IN:
                clauses.add(QueryBuilder.in(column.getName(), getIinValue(value)));
                return;
            case LIKE:
                clauses.add(QueryBuilder.like(column.getName(), value));
                return;
            case AND:
                for (ColumnCondition cc : column.get(new TypeReference<List<ColumnCondition>>() {
                })) {
                    createClause(cc, clauses);
                }
                return;
            case OR:
            default:
                throw new UnsupportedOperationException("The columnCondition " + condition +
                        " is not supported in cassandra column driver");
        }
    }

    private static Object[] getIinValue(Object value) {
        if (Iterable.class.isInstance(value)) {
            Iterable values = Iterable.class.cast(value);
            return StreamSupport.stream(values.spliterator(), false).toArray(Object[]::new);
        }
        return new Object[]{value};
    }


}

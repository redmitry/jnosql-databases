/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.databases.cassandra.mapping;


import jakarta.data.repository.PageableRepository;
import org.eclipse.jnosql.mapping.Converters;
import org.eclipse.jnosql.mapping.column.JNoSQLColumnTemplate;
import org.eclipse.jnosql.mapping.column.query.AbstractColumnRepositoryProxy;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.repository.DynamicReturn;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.eclipse.jnosql.mapping.repository.DynamicReturn.toSingleResult;

class CassandraRepositoryProxy<T, K> extends AbstractColumnRepositoryProxy<T, K>  {

    private final Class<T> typeClass;

    private final CassandraTemplate template;

    private final PageableRepository<T,?> repository;

    private final Converters converters;

    private final EntityMetadata entityMetadata;

    private final Class<?> repositoryType;

    CassandraRepositoryProxy(CassandraTemplate template, Class<?> repositoryType,
                             PageableRepository<T, ?> repository,
                             Converters converters, EntitiesMetadata entitiesMetadata) {

        this.template = template;
        this.typeClass = Class.class.cast(ParameterizedType.class.cast(repositoryType.getGenericInterfaces()[0])
                .getActualTypeArguments()[0]);
        this.repository = repository;
        this.converters = converters;
        this.entityMetadata = entitiesMetadata.get(typeClass);
        this.repositoryType = repositoryType;
    }

    @Override
    protected PageableRepository getRepository() {
        return repository;
    }

    @Override
    protected Converters getConverters() {
        return converters;
    }

    @Override
    protected Class<?> repositoryType() {
        return repositoryType;
    }

    @Override
    protected EntityMetadata getEntityMetadata() {
        return entityMetadata;
    }

    @Override
    protected JNoSQLColumnTemplate getTemplate() {
        return template;
    }

    @Override
    public Object invoke(Object instance, Method method, Object[] args) throws Throwable {

        CQL cql = method.getAnnotation(CQL.class);
        if (Objects.nonNull(cql)) {

            Stream<T> result;
            Map<String, Object> values = CQLObjectUtil.getValues(args, method);
            if (!values.isEmpty()) {
                result = template.cql(cql.value(), values);
            } else if (args == null || args.length == 0) {
                result = template.cql(cql.value());
            } else {
                result = template.cql(cql.value(), args);
            }
            return DynamicReturn.builder()
                    .withClassSource(typeClass)
                    .withMethodSource(method)
                    .withResult(() -> result)
                    .withSingleResult(toSingleResult(method).apply(() -> result))
                    .build().execute();
        }

        return super.invoke(instance, method, args);
    }

}

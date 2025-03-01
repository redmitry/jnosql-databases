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

package org.eclipse.jnosql.databases.mongodb.communication;


import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.document.Document;
import org.eclipse.jnosql.communication.document.DocumentCondition;
import org.eclipse.jnosql.communication.driver.ValueUtil;

import java.util.List;
import java.util.stream.Collectors;

final class DocumentQueryConversor {

    private DocumentQueryConversor() {
    }

    public static Bson convert(DocumentCondition condition) {
        Document document = condition.document();
        Object value = ValueUtil.convert(document.value());
        switch (condition.condition()) {
            case EQUALS:
                return Filters.eq(document.name(), value);
            case GREATER_THAN:
                return Filters.gt(document.name(), value);
            case GREATER_EQUALS_THAN:
                return Filters.gte(document.name(), value);
            case LESSER_THAN:
                return Filters.lt(document.name(), value);
            case LESSER_EQUALS_THAN:
                return Filters.lte(document.name(), value);
            case IN:
                List<Object> inList = ValueUtil.convertToList(document.value());
                return Filters.in(document.name(), inList.toArray());
            case NOT:
                return Filters.not(convert(document.get(DocumentCondition.class)));
            case LIKE:
                return Filters.regex(document.name(), value.toString());
            case AND:
                List<DocumentCondition> andList = condition.document().value().get(new TypeReference<>() {
                });
                return Filters.and(andList.stream()
                        .map(DocumentQueryConversor::convert).collect(Collectors.toList()));
            case OR:
                List<DocumentCondition> orList = condition.document().value().get(new TypeReference<>() {
                });
                return Filters.or(orList.stream()
                        .map(DocumentQueryConversor::convert).collect(Collectors.toList()));
            default:
                throw new UnsupportedOperationException("The condition " + condition.condition()
                        + " is not supported from mongoDB diana driver");
        }
    }


}

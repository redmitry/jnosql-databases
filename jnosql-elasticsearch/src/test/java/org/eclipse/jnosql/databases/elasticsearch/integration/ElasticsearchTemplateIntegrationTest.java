/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.databases.elasticsearch.integration;


import jakarta.inject.Inject;
import org.eclipse.jnosql.databases.elasticsearch.communication.DocumentDatabase;
import org.eclipse.jnosql.databases.elasticsearch.communication.ElasticsearchConfigurations;
import org.eclipse.jnosql.databases.elasticsearch.mapping.ElasticsearchTemplate;
import org.eclipse.jnosql.mapping.Convert;
import org.eclipse.jnosql.mapping.config.MappingConfigurations;
import org.eclipse.jnosql.mapping.document.DocumentEntityConverter;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
import org.eclipse.jnosql.mapping.reflection.EntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.MATCHES;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.NAMED;

@EnableAutoWeld
@AddPackages(value = {Convert.class, DocumentEntityConverter.class})
@AddPackages(Book.class)
@AddPackages(ElasticsearchTemplate.class)
@AddExtensions({EntityMetadataExtension.class,
        DocumentExtension.class})
@EnabledIfSystemProperty(named = NAMED, matches = MATCHES)
class ElasticsearchTemplateIntegrationTest {

    @Inject
    private ElasticsearchTemplate template;

    static {
        DocumentDatabase.INSTANCE.get("library");
        System.setProperty(ElasticsearchConfigurations.HOST.get() + ".1", DocumentDatabase.INSTANCE.host());
        System.setProperty(MappingConfigurations.DOCUMENT_DATABASE.get(), "library");
    }

    @Test
    public void shouldInsert() {
        Book book = new Book(randomUUID().toString(), "Effective Java", 1);
        template.insert(book);
        Optional<Book> optional = template.find(Book.class, book.id());
        assertThat(optional).isNotNull().isNotEmpty()
                .get().isEqualTo(book);
    }

    @Test
    public void shouldUpdate() {
        Book book = new Book(randomUUID().toString(), "Effective Java", 1);
        assertThat(template.insert(book))
                .isNotNull()
                .isEqualTo(book);

        Book updated = new Book(book.id(), book.title() + " updated", 2);

        assertThat(template.update(updated))
                .isNotNull()
                .isNotEqualTo(book);

        assertThat(template.find(Book.class, book.id()))
                .isNotNull().get().isEqualTo(updated);

    }

    @Test
    public void shouldFindById() {
        Book book = new Book(randomUUID().toString(), "Effective Java", 1);
        assertThat(template.insert(book))
                .isNotNull()
                .isEqualTo(book);

        assertThat(template.find(Book.class, book.id()))
                .isNotNull().get().isEqualTo(book);
    }

    @Test
    public void shouldDelete() {
        Book book = new Book(randomUUID().toString(), "Effective Java", 1);
        assertThat(template.insert(book))
                .isNotNull()
                .isEqualTo(book);

        template.delete(Book.class, book.id());
        assertThat(template.find(Book.class, book.id()))
                .isNotNull().isEmpty();
    }


}

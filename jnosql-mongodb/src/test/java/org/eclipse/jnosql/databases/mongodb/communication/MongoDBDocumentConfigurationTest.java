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

import org.eclipse.jnosql.communication.document.DocumentConfiguration;
import org.eclipse.jnosql.communication.document.DocumentManagerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MongoDBDocumentConfigurationTest {

    @Test
    public void shouldCreateDocumentManagerFactoryByMap() {
        Map<String, String> map = new HashMap<>();
        map.put("mongodb-server-host-1", "172.17.0.2:27017");
        MongoDBDocumentConfiguration configuration = new MongoDBDocumentConfiguration();
        DocumentManagerFactory managerFactory = configuration.get(map);
        assertNotNull(managerFactory);
    }


    @Test
    public void shouldReturnErrorWhendSettingsIsNull() {
        DocumentConfiguration configuration = new MongoDBDocumentConfiguration();
        assertThrows(NullPointerException.class, () -> configuration.apply(null));
    }

    @Test
    public void shouldReturnErrorWhenMapSettingsIsNull() {
        MongoDBDocumentConfiguration configuration = new MongoDBDocumentConfiguration();
        assertThrows(NullPointerException.class, () -> configuration.get((Map) null));
    }

    @Test
    public void shouldReturnFromConfiguration() {
        DocumentConfiguration configuration = DocumentConfiguration.getConfiguration();
        Assertions.assertNotNull(configuration);
        Assertions.assertTrue(configuration instanceof DocumentConfiguration);
    }

    @Test
    public void shouldReturnFromConfigurationQuery() {
        MongoDBDocumentConfiguration configuration = DocumentConfiguration
                .getConfiguration(MongoDBDocumentConfiguration.class);
        Assertions.assertNotNull(configuration);
        Assertions.assertTrue(configuration instanceof MongoDBDocumentConfiguration);
    }

}

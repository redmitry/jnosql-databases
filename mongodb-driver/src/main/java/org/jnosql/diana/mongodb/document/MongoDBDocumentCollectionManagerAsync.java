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
package org.jnosql.diana.mongodb.document;


import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jnosql.diana.api.ExecuteAsyncQueryException;
import org.jnosql.diana.api.document.DocumentCollectionManagerAsync;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.jnosql.diana.api.document.Documents;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import static org.jnosql.diana.mongodb.document.MongoDBUtils.getDocument;

public class MongoDBDocumentCollectionManagerAsync implements DocumentCollectionManagerAsync {

    private final MongoDatabase asyncMongoDatabase;

    MongoDBDocumentCollectionManagerAsync(MongoDatabase asyncMongoDatabase) {
        this.asyncMongoDatabase = asyncMongoDatabase;
    }

    @Override
    public void save(DocumentEntity entity) throws ExecuteAsyncQueryException, UnsupportedOperationException {
        save(entity, v -> {
        });
    }

    @Override
    public void save(DocumentEntity entity, Duration ttl) {
        throw new UnsupportedOperationException("MongoDB does not support saveAsync with TTL");
    }

    @Override
    public void save(DocumentEntity entity, Consumer<DocumentEntity> callBack)
            throws ExecuteAsyncQueryException, UnsupportedOperationException {
        save(entity, (aVoid, throwable) -> callBack.accept(entity));
    }

    @Override
    public void save(DocumentEntity entity, Duration ttl, Consumer<DocumentEntity> callBack) {
        throw new UnsupportedOperationException("MongoDB does not support saveAsync with TTL");
    }

    @Override
    public void update(DocumentEntity entity)
            throws ExecuteAsyncQueryException, UnsupportedOperationException {
        update(entity, (d, throwable) -> {
        });
    }

    @Override
    public void update(DocumentEntity entity, Consumer<DocumentEntity> callBack)
            throws ExecuteAsyncQueryException, UnsupportedOperationException {
        update(entity, (d, throwable) -> {
            callBack.accept(DocumentEntity.of(entity.getName(), Documents.of(d)));
        });
    }

    @Override
    public void delete(DocumentQuery query) {
        delete(query, (deleteResult, throwable) -> {
        });
    }

    @Override
    public void delete(DocumentQuery query, Consumer<Void> callBack)
            throws ExecuteAsyncQueryException, UnsupportedOperationException {
        delete(query, (deleteResult, throwable) -> callBack.accept(null));

    }

    @Override
    public void find(DocumentQuery query, Consumer<List<DocumentEntity>> callBack)
            throws ExecuteAsyncQueryException, UnsupportedOperationException {

    }

    private void save(DocumentEntity entity, SingleResultCallback<Void> callBack) {
        String collectionName = entity.getName();
        com.mongodb.async.client.MongoCollection<Document> collectionAsync =
                asyncMongoDatabase.getCollection(collectionName);
        Document document = getDocument(entity);
        collectionAsync.insertOne(document, callBack);
    }

    private void update(DocumentEntity entity, SingleResultCallback<Document> callBack) {
        String collectionName = entity.getName();
        com.mongodb.async.client.MongoCollection<Document> asyncCollection =
                asyncMongoDatabase.getCollection(collectionName);
        Document id = entity.find("_id").map(d -> new Document(d.getName(), d.getValue().get()))
                .orElseThrow(() -> new UnsupportedOperationException("To update this DocumentEntity " +
                        "the field `id` is required"));

        asyncCollection.findOneAndReplace(id, getDocument(entity), callBack);
    }

    private void delete(DocumentQuery query, SingleResultCallback<DeleteResult> callBack) {
        String collectionName = query.getCollection();
        com.mongodb.async.client.MongoCollection<Document> asyncCollection =
                asyncMongoDatabase.getCollection(collectionName);
        Bson mongoDBQuery = DocumentQueryConversor.convert(query.getCondition());
        asyncCollection.deleteMany(mongoDBQuery, callBack);
    }


    @Override
    public void close() {

    }

}

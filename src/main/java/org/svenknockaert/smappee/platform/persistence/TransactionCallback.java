package org.svenknockaert.smappee.platform.persistence;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionCallback<T> {

    T doInTransaction(Connection connection) throws Exception;
}

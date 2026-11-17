package org.svenknockaert.smappee.platform.persistence;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionAction {

    void doInTransaction(Connection connection) throws Exception;
}

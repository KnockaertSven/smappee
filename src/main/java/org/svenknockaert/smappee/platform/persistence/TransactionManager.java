package org.svenknockaert.smappee.platform.persistence;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component(service = TransactionManager.class)
public class TransactionManager {

    private final DataSource dataSource;

    @Activate
    public TransactionManager(@Reference DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionCallback<T> callback) {
        try (Connection connection = dataSource.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = callback.doInTransaction(connection);
                connection.commit();
                return result;
            } catch (Exception ex) {
                connection.rollback();
                if (ex instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new IllegalStateException("Transaction failed and was rolled back", ex);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to acquire or handle database connection", ex);
        }
    }

    public void run(TransactionAction action) {
        execute(connection -> {
            action.doInTransaction(connection);
            return null;
        });
    }
}

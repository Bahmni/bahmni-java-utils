package org.bahmni.module.common.db;

import java.sql.Connection;

public interface JDBCConnectionProvider {
    public Connection getConnection();

    public void closeConnection();
}

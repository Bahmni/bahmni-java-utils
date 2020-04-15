package org.bahmni.form2.common.db;

import java.sql.Connection;

public interface JDBCConnectionProvider {
    public Connection getConnection();

    public void closeConnection();
}

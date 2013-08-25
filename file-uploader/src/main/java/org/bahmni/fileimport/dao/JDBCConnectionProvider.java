package org.bahmni.fileimport.dao;

import java.sql.Connection;

public interface JDBCConnectionProvider {
    public Connection getConnection();
}

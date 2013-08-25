package org.bahmni.fileimport.dao;

import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ImportStatusDao {
    public static final String IN_PROGRESS = "IN PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String ERROR = "ERROR";
    private JDBCConnectionProvider jdbcConnectionProvider;

    private Logger logger = Logger.getLogger(ImportStatusDao.class);

    public ImportStatusDao(JDBCConnectionProvider jdbcConnectionProvider) {
        this.jdbcConnectionProvider = jdbcConnectionProvider;
    }

    public void saveInProgress(File csvFile, String type) throws SQLException {
        Connection connection = jdbcConnectionProvider.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into import_status (original_file_name, saved_file_name, type, status, user_id) values (?, ?, ?, ?, ?);");
            statement.setString(1, csvFile.getAbsolutePath());
            statement.setString(2, csvFile.getAbsolutePath());
            statement.setString(3, type);
            statement.setString(4, IN_PROGRESS);
            statement.setString(5, "importUser");

            statement.execute();
            connection.commit();
        } finally {
            closeResources(connection, statement);
        }
    }

    public void saveError(File csvFile, String type, Exception e) throws SQLException {
        // TODO : Mujir save the stack trace in db. Change migration to create the stacktrace column
        Connection connection = jdbcConnectionProvider.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("update import_status set type=?, status=?, user_id=? where saved_file_name=?;");
            statement.setString(1, type);
            statement.setString(2, ERROR);
            statement.setString(3, "importUser");
            statement.setString(4, csvFile.getAbsolutePath());

            statement.execute();
            connection.commit();
        } finally {
            closeResources(connection, statement);

        }
    }

    public void saveCompleted(File csvFile, String type, int numberOfSuccessfulRecords, int numberOfFailedRecords) throws SQLException {
        Connection connection = jdbcConnectionProvider.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("update import_status set type=?, status=?, successful_records=?, failed_records=?, user_id=? where saved_file_name=?;");
            statement.setString(1, type);
            statement.setString(2, COMPLETED);
            statement.setInt(3, numberOfSuccessfulRecords);
            statement.setInt(4, numberOfFailedRecords);
            statement.setString(5, "importUser");
            statement.setString(6, csvFile.getAbsolutePath());

            statement.execute();
            connection.commit();
        } finally {
            closeResources(connection, statement);
        }
    }

    private void closeResources(Connection connection, PreparedStatement statement) throws SQLException {
        if (statement != null) statement.close();
        if (connection != null && !connection.isClosed()) connection.close();
    }
}

package org.bahmni.fileimport.dao;

import org.apache.log4j.Logger;
import org.bahmni.csv.MigrateResult;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ImportStatusDao {
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String COMPLETED = "COMPLETED";
    private static final String FATAL_ERROR = "ERROR";
    private static final String COMPLETED_WITH_ERRORS= "COMPLETED_WITH_ERRORS";

    private JDBCConnectionProvider jdbcConnectionProvider;

    private Logger logger = Logger.getLogger(ImportStatusDao.class);

    public ImportStatusDao(JDBCConnectionProvider jdbcConnectionProvider) {
        this.jdbcConnectionProvider = jdbcConnectionProvider;
    }

    public void saveInProgress(String originalFileName, File csvFile, String type, String uploadedBy) throws SQLException {
        Connection connection = jdbcConnectionProvider.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into import_status (original_file_name, saved_file_name, type, status, uploaded_by, start_time) values (?, ?, ?, ?, ?, ?);");
            statement.setString(1, originalFileName);
            statement.setString(2, csvFile.getAbsolutePath());
            statement.setString(3, type);
            statement.setString(4, IN_PROGRESS);
            statement.setString(5, uploadedBy);
            statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            statement.execute();
            connection.commit();
        } finally {
            closeResources(connection, statement);
        }
    }

    public void saveFinished(File csvFile, String simpleName, MigrateResult migrateResult) throws SQLException {
        Connection connection = jdbcConnectionProvider.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("update import_status set type=?, status=?, error_file_name=?, successful_records=?, failed_records=?, stage_name=?, end_time=? where saved_file_name=?;");
            statement.setString(1, simpleName);
            statement.setString(2, migrateResult.hasFailed() ? COMPLETED_WITH_ERRORS : COMPLETED);
            statement.setString(3, migrateResult.getErrorFileName());
            statement.setInt(4, migrateResult.numberOfSuccessfulRecords());
            statement.setInt(5, migrateResult.numberOfFailedRecords());
            statement.setString(6, migrateResult.getStageName());
            statement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            statement.setString(8, csvFile.getAbsolutePath());

            statement.execute();
            connection.commit();
        } finally {
            closeResources(connection, statement);
        }
    }

    public void saveFatalError(File csvFile, String type, Exception exception) throws SQLException {
        Connection connection = jdbcConnectionProvider.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("update import_status set type=?, status=?, stack_trace=?, end_time=? where saved_file_name=?;");
            statement.setString(1, type);
            statement.setString(2, FATAL_ERROR);
            statement.setString(3, getStackTrace(exception));
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            statement.setString(5, csvFile.getAbsolutePath());

            statement.execute();
            connection.commit();
        } finally {
            closeResources(connection, statement);
        }
    }

    private static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    private void closeResources(Connection connection, PreparedStatement statement) throws SQLException {
        if (statement != null) statement.close();
        if (connection != null && !connection.isClosed()) connection.close();
    }
}

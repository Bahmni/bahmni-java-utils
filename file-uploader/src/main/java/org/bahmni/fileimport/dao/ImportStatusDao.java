package org.bahmni.fileimport.dao;

import org.bahmni.csv.CSVFile;
import org.bahmni.csv.MigrateResult;
import org.bahmni.fileimport.ImportStatus;
import org.bahmni.common.db.JDBCConnectionProvider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportStatusDao {
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String COMPLETED = "COMPLETED";
    private static final String FATAL_ERROR = "ERROR";
    private static final String COMPLETED_WITH_ERRORS = "COMPLETED_WITH_ERRORS";

    private JDBCConnectionProvider jdbcConnectionProvider;

    public ImportStatusDao(JDBCConnectionProvider jdbcConnectionProvider) {
        this.jdbcConnectionProvider = jdbcConnectionProvider;
    }

    public void saveInProgress(String originalFileName, CSVFile csvFile, String type, String uploadedBy) throws SQLException {
        Connection connection = jdbcConnectionProvider.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into import_status (original_file_name, saved_file_name, type, status, uploaded_by, start_time) values (?, ?, ?, ?, ?, ?);");
            statement.setString(1, originalFileName);
            statement.setString(2, csvFile.getRelativePath());
            statement.setString(3, type);
            statement.setString(4, IN_PROGRESS);
            statement.setString(5, uploadedBy);
            statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            statement.execute();
            connection.commit();
        } finally {
            closeResources(statement);
        }
    }

    public void saveFinished(CSVFile csvFile, String simpleName, MigrateResult migrateResult) throws SQLException {
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
            statement.setString(8, csvFile.getRelativePath());

            statement.execute();
            connection.commit();
        } finally {
            closeResources(statement);
        }
    }

    public void saveFatalError(CSVFile csvFile, String type, String errorMessage) throws SQLException {
        Connection connection = jdbcConnectionProvider.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("update import_status set type=?, status=?, stack_trace=?, end_time=? where saved_file_name=?;");
            statement.setString(1, type);
            statement.setString(2, FATAL_ERROR);
            statement.setString(3, errorMessage);
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            statement.setString(5, csvFile.getRelativePath());

            statement.execute();
            connection.commit();
        } finally {
            closeResources(statement);
        }
    }

    public void saveFatalError(CSVFile csvFile, String type, Throwable exception) throws SQLException {
        saveFatalError(csvFile, type, getStackTrace(exception));
    }

    public List<ImportStatus> getImportStatusFromDate(Date fromDate) throws SQLException {
        Connection connection = jdbcConnectionProvider.getConnection();
        PreparedStatement statement = null;
        List<ImportStatus> importStatuses = new ArrayList<>();
        try {
            statement = connection.prepareStatement("select id, original_file_name, saved_file_name, error_file_name, type, status, successful_records, failed_records, stage_name, uploaded_by, start_time, end_time, stack_trace from import_status where start_time >= ? order by start_time desc");
            statement.setDate(1, new java.sql.Date(fromDate.getTime()));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                importStatuses.add(new ImportStatus(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                        resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getInt(7), resultSet.getInt(8),
                        resultSet.getString(9), resultSet.getString(10), resultSet.getTimestamp(11), resultSet.getTimestamp(12), resultSet.getString(13)));
            }
        } finally {
            closeResources(statement);
        }
        return importStatuses;
    }

    private static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    private void closeResources(PreparedStatement statement) throws SQLException {
        if (statement != null) statement.close();
        if (jdbcConnectionProvider != null) jdbcConnectionProvider.closeConnection();
    }
}

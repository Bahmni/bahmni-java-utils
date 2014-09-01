package org.bahmni.csv;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.bahmni.csv.column.CSVColumns;
import org.bahmni.csv.exception.MigrationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CSVFile<T extends CSVEntity> {
    public static final char SEPARATOR = ',';

    private String relativePath;
    private String basePath;

    private CSVReader csvReader;
    private CSVWriter csvWriter;

    private String[] headerNames;

    public String getBasePath() {
        return basePath;
    }

    public CSVFile(String basePath, String relativePath) {
        this.basePath = basePath;
        this.relativePath = relativePath;
    }

    public void openForRead() throws IOException {
        File file = new File(basePath, relativePath);
        if (!file.exists())
            throw new MigrationException("Input CSV file does not exist. File - " + file.getAbsolutePath());

        csvReader = new CSVReader(new FileReader(file), SEPARATOR, '"', '\0');
        headerNames = csvReader.readNext();
    }

    public T readEntity(Class<T> entityClass) throws IOException, InstantiationException, IllegalAccessException {
        if (csvReader == null)
            throw new MigrationException("Please open the CSVFile before reading it");
        String[] aRow = csvReader.readNext();
        CSVRow tempCSVRow = new CSVRow<>(getHeaderColumn(), entityClass);
        return (T) tempCSVRow.getEntity(aRow);
    }

    public void writeARecord(RowResult<T> aRow, String[] headerRow) throws IOException {
        if (csvWriter == null) {
            openForWrite();
            csvWriter.writeNext(headerRow);
        }

        csvWriter.writeNext(aRow.getRowWithErrorColumn());
    }

    private void openForWrite() throws IOException {
        File file = new File(basePath, relativePath);
        csvWriter = new CSVWriter(new FileWriter(file));
    }

    public void close() {
        try {
            if (csvReader != null) csvReader.close();
            if (csvWriter != null) csvWriter.close();
        } catch (IOException e) {
            throw new MigrationException("Could not close file. " + e.getMessage(), e);
        }
    }

    private CSVColumns getHeaderColumn() throws IOException {
        return new CSVColumns(headerNames);
    }

    public String[] getHeaderRow() {
        return headerNames;
    }

    public String getAbsolutePath() {
        return basePath + "/" + relativePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void delete() {
        new File(basePath, relativePath).delete();
    }
}

package org.bahmni.csv;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.bahmni.csv.column.CSVColumns;
import org.bahmni.csv.exception.MigrationException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class CSVFile<T extends CSVEntity> {
    public static final char SEPARATOR = ',';

    private String relativePath;
    private String basePath;

    private CSVReader csvReader;
    private CSVWriter csvWriter;

    private String[] headerNames;
    private OutputStreamWriter writer;
    private ByteArrayOutputStream outputStream;

    public CSVFile(ByteArrayOutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        writer = new OutputStreamWriter(outputStream);
        csvWriter = new CSVWriter(writer, SEPARATOR);
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

    private void openForWrite() throws IOException {
        File file = new File(basePath, relativePath);
        csvWriter = new CSVWriter(new FileWriter(file, true));
    }

    public T readEntity(Class<T> entityClass) throws IOException, InstantiationException, IllegalAccessException {
        if (csvReader == null)
            throw new MigrationException("Please open the CSVFile before reading it");
        String[] aRow = csvReader.readNext();
        CSVRow tempCSVRow = new CSVRow<>(getHeaderColumn(), entityClass);
        return (T) tempCSVRow.getEntity(aRow);
    }

    public void writeARecord(RowResult<T> aRow) throws IOException {
        if (csvWriter == null) {
            openForWrite();
        }
        csvWriter.writeNext(aRow.getRowWithErrorColumn());
    }

    public void writeARecord(RowResult<T> aRow, String[] headerRow) throws IOException {
        if (csvWriter == null) {
            openForWrite();
            csvWriter.writeNext(headerRow);
        }
        csvWriter.writeNext(aRow.getRowWithErrorColumn());
    }

    public void writeHeaderRecord(String[] headerRow) throws IOException {
        if (csvWriter == null) {
            openForWrite();
            csvWriter.writeNext(headerRow);
        }
    }

    public String getBasePath() {
        return basePath;
    }

    public void writeARecord(CSVEntity csvEntity) throws IOException {
        List<String> originalRow = csvEntity.getOriginalRow();
        csvWriter.writeNext(originalRow.toArray(new String[originalRow.size()]));
    }

    public ByteArrayOutputStream getOutputStream() throws IOException {
        csvWriter.close();
        return outputStream;
    }

    public void close() {
        try {
            if (csvReader != null)  { csvReader.close(); csvReader = null; };
            if (csvWriter != null)  { csvWriter.close(); csvWriter = null; };
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

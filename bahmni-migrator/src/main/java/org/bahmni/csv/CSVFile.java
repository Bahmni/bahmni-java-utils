package org.bahmni.csv;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.bahmni.csv.column.CSVColumns;
import org.bahmni.csv.exception.MigrationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

        InputStream inputStream = new FileInputStream(file);
        csvReader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8.name()), ',', '\"', '\\');
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

    public void writeARecord(CSVEntity csvEntity) throws IOException {
        List<String> originalRow = csvEntity.getOriginalRow();
        csvWriter.writeNext(originalRow.toArray(new String[originalRow.size()]));
    }

    public ByteArrayOutputStream getOutputStream() throws IOException {
        csvWriter.close();
        return outputStream;
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

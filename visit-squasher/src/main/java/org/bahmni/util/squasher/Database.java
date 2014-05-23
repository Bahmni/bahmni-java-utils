package org.bahmni.util.squasher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
//    Add a user root in mysql with the hostname assigned to your machine on the network.
//    CREATE USER 'root'@'hostname' IDENTIFIED BY 'password'; (to connect to vagrant box from mysqlworkbench)
//    GRANT ALL PRIVILEGES ON *.* TO 'root'@'hostname' WITH GRANT OPTION;
//

    private static final String DB_URL = "jdbc:mysql://192.168.33.10/openmrs";
//    private static final String DB_URL = "jdbc:mysql://192.168.0.152/openmrs";
    private static final String USER = "openmrs-user";
    private static final String PASS = "password";

    public Database() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(String query) throws SQLException {
        Connection connection = getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(query);
        } finally {
            statement.close();
            connection.close();
        }
    }
}

package org.bahmni.util.squasher;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        new Worker().work();
    }
}

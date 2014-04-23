package org.bahmni.util.squasher;

import java.sql.SQLException;

public class Worker {
    private final Database database;

    public Worker() {
        this.database = new Database();
    }

    public void work() throws SQLException {
        dropTableIfExists();
        createTable();
        new VisitUpdater(database).update();
        System.out.println("Done");
    }

    private void createTable() {
        System.out.println("Creating table...");
        database.execute("create table visit_migration as (\n" +
                "select v1.patient_id patient_id, \n" +
                "\tv1.visit_id visit_id1, \n" +
                "\tvt1.name visit_type1, \n" +
                "\tv1.date_started date_started1, \n" +
                "\tv1.date_stopped date_stopped1, \n" +
                "\tv2.visit_id visit_id2, \n" +
                "\tvt2.name visit_type2, \n" +
                "\tv2.date_started date_started2, \n" +
                "\tv2.date_stopped date_stopped2,  \n" +
                "\tdatediff(v2.date_started, v1.date_started), \n" +
                "\tdatediff(v2.date_started, v1.date_stopped)\n" +
                "from visit v1, visit v2, visit_type vt1, visit_type vt2\n" +
                "where v1.visit_id <> v2.visit_id \n" +
                "\tand v1.patient_id = v2.patient_id \n" +
                "\tand not exists(select 1 from visit \n" +
                "\t\t\t\t\twhere patient_id = v1.patient_id \n" +
                "\t\t\t\t\t\tand date_started >= v1.date_started \n" +
                "\t\t\t\t\t\tand date_started <= v2.date_started \n" +
                "\t\t\t\t\t\tand visit_id not in (v1.visit_id, v2.visit_id))\n" +
                "\tand datediff(v2.date_started, v1.date_stopped) < 6\n" +
                "\tand datediff(v2.date_started, v1.date_stopped) > 0\n" +
                "\tand v1.visit_type_id = vt1.visit_type_id\n" +
                "\tand v2.visit_type_id = vt2.visit_type_id\n" +
                "\tand v2.visit_type_id not in (1, 2));");

    }

    private void dropTableIfExists() {
        System.out.println("Dropping table");
        database.execute("drop table if exists visit_migration;");
    }
}

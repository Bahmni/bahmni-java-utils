package org.bahmni.util.squasher.adjacentvisits;

import org.bahmni.util.squasher.Database;

import java.sql.SQLException;

public class AdjacentVisitSquasher {
    private final Database database;

    public AdjacentVisitSquasher() {
        this.database = new Database();
    }

    public void squash() throws SQLException {
        dropVisitChainItemTableIfExists();
        createVisitChainItemTable();
        new VisitUpdater(database).update();
        System.out.println("Done");
    }

    private void createVisitChainItemTable() throws SQLException {
        System.out.println("Creating table...");
        database.execute("create table visit_migration as (\n" +
                "select v1.patient_id patient_id, \n" +
                "  v1.visit_id visit_id1, \n" +
                "  vt1.name visit_type1, \n" +
                "  v1.date_started date_started1, \n" +
                "  v1.date_stopped date_stopped1, \n" +
                "  v2.visit_id visit_id2, \n" +
                "  vt2.name visit_type2, \n" +
                "  v2.date_started date_started2, \n" +
                "  v2.date_stopped date_stopped2,  \n" +
                "  datediff(v2.date_started, v1.date_started), \n" +
                "  datediff(v2.date_started, v1.date_stopped)\n" +
                "from visit v1, visit v2, visit_type vt1, visit_type vt2\n" +
                "where v1.visit_id <> v2.visit_id \n" +
                "  and v1.patient_id = v2.patient_id \n" +
                "  and not exists(select 1 from visit \n" +
                "          where patient_id = v1.patient_id \n" +
                "            and date_started >= v1.date_started \n" +
                "            and date_started <= v2.date_started \n" +
                "            and visit_id not in (v1.visit_id, v2.visit_id)" +
                "            and voided = false)\n" +
                "  and datediff(v2.date_started, v1.date_stopped) < 6\n" +
                "  and timestampdiff(SECOND, v1.date_stopped, v2.date_started) >= 0\n" +
                "  and v1.voided = false and v2.voided = false\n" +
                "  and v1.visit_type_id = vt1.visit_type_id\n" +
                "  and v2.visit_type_id = vt2.visit_type_id\n" +
                "  and vt2.name not in ('OPD', 'OPD - NEW', 'OPD - RETURNING'));");

    }

    private void dropVisitChainItemTableIfExists() throws SQLException {
        System.out.println("Dropping table");
        database.execute("drop table if exists visit_migration;");
    }
}

package org.bahmni.util.squasher.radiology;

import org.bahmni.util.squasher.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RadiologyVisitSquasher {

    private final Database database;

    /*    Do not touch specific visit ids. These seem to have long start and end dates, probably wrongly entered.
        Data obtained from following query
        select v.patient_id, v.visit_id, date_started, date_stopped, v.date_created
        from visit v, encounter e
        where v.visit_id = e.visit_id and
        e.encounter_type = 6 and
        datediff(date_stopped, date_started) > 5 order by v.patient_id ;
    */
    private final String VISITS_TO_BE_SQUASHED_QUERY = "select distinct v.visit_id visit_id, v.date_started date_started, v.date_stopped date_stopped, v.patient_id patient_id\n" +
            "from visit v\n" +
            "inner join encounter e on v.visit_id = e.visit_id\n" +
            "inner join encounter_type et on e.encounter_type = et.encounter_type_id and et.name = 'RADIOLOGY' " +
            "where v.visit_id not in (1505, 60150, 59170, 58597, 58042, 57463, 58612)\n" +
            "and v.voided = false and v.date_stopped is not null";
    private Connection readConnection;
    private Connection writeConnection;

    public RadiologyVisitSquasher() {
        this.database = new Database();
    }

    public void squash() throws SQLException {
        System.out.println("Squashing visits for radiology...");
        getConnections();
        squashVisits();
        closeConnections();
        System.out.println("Done squashing visits for radiology...");
    }

    private void squashVisits() throws SQLException {
        Statement statement = null;
        ResultSet visitResultSet = null;
        try {
            statement = readConnection.createStatement();
            visitResultSet = statement.executeQuery(VISITS_TO_BE_SQUASHED_QUERY);
            while (visitResultSet.next()) {
                Visit visit = new Visit(visitResultSet.getInt("visit_id"),
                        visitResultSet.getTimestamp("date_started"),
                        visitResultSet.getTimestamp("date_stopped"),
                        visitResultSet.getInt("patient_id"));

                List<Visit> overlappingVisits = getOverlappingVisits(visit);
                for (Visit overlappingVisit : overlappingVisits) {
                    squashVisit(visit, overlappingVisit);
                }
                if (overlappingVisits.isEmpty()) {
                    System.out.println("Could not find matching visit for " + visit);

                }
            }
        } finally {
            visitResultSet.close();
            statement.close();
        }
    }

    private void squashVisit(Visit visit, Visit overlappingVisit) throws SQLException {
        if(visitVoided(visit)) {
            System.out.println("Visit" + visit + " is already voided");
        } else {
            System.out.println("Squashing " + overlappingVisit + " into " + visit);
            updateStartAndStopDates(visit, overlappingVisit);
            moveEncountersAndVoidVisit(visit, overlappingVisit);
        }
    }

    private boolean visitVoided(Visit visit) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = readConnection.prepareStatement("select voided as voided from visit where visit_id = ?");
            statement.setInt(1, visit.getVisitId());
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("voided");
            }
        } finally {
            resultSet.close();
            statement.close();
        }
        throw new RuntimeException("Cannot reach here");
    }

    private void moveEncountersAndVoidVisit(Visit visit, Visit overlappingVisit) throws SQLException {
        PreparedStatement encounterUpdateStmt = null;
        PreparedStatement voidStmt = null;
        try {
            encounterUpdateStmt = writeConnection.prepareStatement("update encounter set visit_id = ? where visit_id = ?");
            encounterUpdateStmt.setInt(1, visit.getVisitId());
            encounterUpdateStmt.setInt(2, overlappingVisit.getVisitId());
            encounterUpdateStmt.executeUpdate();
            encounterUpdateStmt.close();

            voidStmt = writeConnection.prepareStatement("update visit set voided = true, voided_by = 1, date_voided = curdate() where visit_id = ?");
            voidStmt.setInt(1, overlappingVisit.getVisitId());
            voidStmt.executeUpdate();
            voidStmt.close();
        } finally {
            encounterUpdateStmt.close();
            voidStmt.close();
        }


    }

    private void updateStartAndStopDates(Visit visit, Visit overlappingVisit) throws SQLException {
        Timestamp dateStarted = earlierOf(visit.getDateStarted(), overlappingVisit.getDateStarted());
        Timestamp dateStopped = laterOf(visit.getDateStopped(), overlappingVisit.getDateStopped());
        PreparedStatement statement = null;
        try {
            statement = writeConnection.prepareStatement("update visit " +
                    "set " +
                    "   date_started = ?, " +
                    "   date_stopped = ? " +
                    "where  visit_id = ?");
            statement.setTimestamp(1, new Timestamp(dateStarted.getTime()));
            statement.setTimestamp(2, dateStopped);
            statement.setInt(3, visit.getVisitId());
            statement.executeUpdate();
        } finally {
            statement.close();
        }
    }

    private Timestamp earlierOf(Timestamp a, Timestamp b) {
        return  a.after(b) ? b : a;
    }

    private Timestamp laterOf(Timestamp a, Timestamp b) {
        return a.after(b) ? a : b;
    }

    private List<Visit> getOverlappingVisits(Visit visit) throws SQLException {
        List<Visit> overlappingVisits = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = readConnection.prepareStatement("select visit_id visit_id, date_started date_started, date_stopped date_stopped, patient_id patient_id from visit v\n" +
                    "where v.visit_id <> ?\n" +
                    "\tand v.patient_id = ?\n" +
                    "\tand v.date_started <= ?\n" +
                    "\tand v.date_stopped >= ?" +
                    "\tand v.voided = false" +
                    "\torder by date_started asc");
            statement.setInt(1, visit.getVisitId());
            statement.setInt(2, visit.getPatientId());
            statement.setTimestamp(3, visit.getDateStopped());
            statement.setTimestamp(4, visit.getDateStarted());
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                overlappingVisits.add(new Visit(resultSet.getInt("visit_id"),
                        resultSet.getTimestamp("date_started"),
                        resultSet.getTimestamp("date_stopped"),
                        resultSet.getInt("patient_id")));
            }
        } finally {
            resultSet.close();
            statement.close();
        }

        return overlappingVisits;
    }

    private void getConnections() {
        this.readConnection = database.getConnection();
        this.writeConnection = database.getConnection();
    }

    private void closeConnections() throws SQLException {
        readConnection.close();
        writeConnection.close();
    }
}

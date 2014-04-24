package org.bahmni.util.squasher.adjacentvisits;

import org.bahmni.util.squasher.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitUpdater {
    private final Connection writeConnection;
    private Database database;

    public VisitUpdater(Database database) {
        this.database = database;
        this.writeConnection = database.getConnection();
    }

    public void update() throws SQLException {
        Connection connection = database.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            List<VisitChainItem> toSquash = new ArrayList<VisitChainItem>();
            VisitChainItem previousVisitChainItem = new VisitChainItem(0, 0, null, null);
            resultSet = statement.executeQuery(
                    "SELECT visit_id1, visit_id2, date_stopped1, date_stopped2 " +
                    "FROM visit_migration " +
                    "ORDER by patient_id asc, date_started1 asc");
            while (resultSet.next()) {
                VisitChainItem visitChainItem = new VisitChainItem(resultSet.getInt("visit_id1"), resultSet.getInt("visit_id2"), resultSet.getTimestamp("date_stopped1"), resultSet.getTimestamp("date_stopped2"));
                try {
                    if (toSquash.size() == 0) {
                        toSquash.add(visitChainItem);
                        continue;
                    }

                    if (visitChainItem.getVisit1Id() != previousVisitChainItem.getVisit2Id()) {
                        squash(toSquash);
                        toSquash.clear();
                    }
                    toSquash.add(visitChainItem);
                } finally {
                    previousVisitChainItem = visitChainItem;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            resultSet.close();
            statement.close();
            connection.close();
            writeConnection.close();
        }
    }

    private void squash(List<VisitChainItem> visitChainItems) {
        for (VisitChainItem visitChainItem : visitChainItems) {
            System.out.println(visitChainItem);
        }
        System.out.println("----------");
        int rootVisitId = visitChainItems.get(0).getVisit1Id();
        Timestamp dateStoppedForRootVisit = visitChainItems.get(visitChainItems.size() - 1).getVisit2DateStopped();

        Connection connection = writeConnection;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("update visit set date_stopped = ? where visit_id = ?");
            statement.setTimestamp(1, dateStoppedForRootVisit);
            statement.setInt(2, rootVisitId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (VisitChainItem visitChainItem : visitChainItems) {
            try {
                statement = connection.prepareStatement("update encounter set visit_id = ? where visit_id = ?");
                statement.setInt(1, rootVisitId);
                statement.setInt(2, visitChainItem.getVisit2Id());
                statement.executeUpdate();
                statement.close();

                statement = connection.prepareStatement("update visit set voided = true, voided_by = 1, date_voided = curdate() where visit_id = ?");
                statement.setInt(1, visitChainItem.getVisit2Id());
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

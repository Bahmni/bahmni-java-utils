package org.bahmni.util.squasher;

import org.bahmni.util.squasher.adjacentvisits.AdjacentVisitSquasher;
import org.bahmni.util.squasher.radiology.RadiologyVisitSquasher;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        new RadiologyVisitSquasher().squash();
        new AdjacentVisitSquasher().squash();
    }
}

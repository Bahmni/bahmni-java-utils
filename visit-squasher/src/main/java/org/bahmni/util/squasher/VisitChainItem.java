package org.bahmni.util.squasher;

import java.sql.Date;

public class VisitChainItem {
    private int visit1Id;
    private int visit2Id;
    private Date visit1DateStopped;
    private Date visit2DateStopped;

    public VisitChainItem(int visit1Id, int visit2Id, Date visit1DateStopped, Date visit2DateStopped) {
        this.visit1Id = visit1Id;
        this.visit2Id = visit2Id;
        this.visit1DateStopped = visit1DateStopped;
        this.visit2DateStopped = visit2DateStopped;
    }

    public int getVisit1Id() {
        return visit1Id;
    }

    public int getVisit2Id() {
        return visit2Id;
    }

    public Date getVisit2DateStopped() {
        return visit2DateStopped;
    }

    @Override
    public String toString() {
        return "org.bahmni.util.squasher.VisitChainItem{" +
                "visit1Id=" + visit1Id +
                ", visit2Id=" + visit2Id +
                ", visit1DateStopped=" + visit1DateStopped +
                ", visit2DateStopped=" + visit2DateStopped +
                '}';
    }
}

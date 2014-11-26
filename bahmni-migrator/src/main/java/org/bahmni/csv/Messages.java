package org.bahmni.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Messages extends ArrayList<String>{
    public Messages() {
        super();
    }

    public Messages(String str) {
        super(Arrays.asList(str));
    }

    public Messages(int initialCapacity) {
        super(initialCapacity);
    }

    public Messages(Collection<? extends String> c) {
        super(c);
    }

    public Messages(Throwable e) {
        this(e.getMessage());
    }

    public void add(Throwable e) {
        this.add(e.getMessage());
    }

    public String asString() {
        StringBuffer messageString = new StringBuffer();
        for (String message : this) {
            messageString.append(message);
        }
        return messageString.toString();
    }
}

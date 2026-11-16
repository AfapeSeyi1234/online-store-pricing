package com.store.batch;

import java.util.List;

// A record that failed validation: where it was, what it said, and every reason it was refused.
public record RejectedRecord(int lineNumber, String rawLine, List<String> reasons) {

    public RejectedRecord {
        reasons = List.copyOf(reasons);
    }
}
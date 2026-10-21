package com.store.analytics;

import java.util.List;

// T is the type of item that was loaded (for example Product).
public record LoadResult<T>(List<T> items, List<String> errors) {

    public LoadResult {
        items = List.copyOf(items);
        errors = List.copyOf(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
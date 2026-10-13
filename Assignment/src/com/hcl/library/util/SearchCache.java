package com.hcl.library.util;

import com.hcl.library.model.LibraryItem;
import java.util.*;

public class SearchCache {
    private static final int MAX_ENTRIES = 5;
    private final LinkedHashMap<String, List<LibraryItem>> cache;

    public SearchCache() {
        this.cache = new LinkedHashMap<String, List<LibraryItem>>(MAX_ENTRIES, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<LibraryItem>> eldest) {
                return size() > MAX_ENTRIES;
            }
        };
    }

    public void put(String query, List<LibraryItem> items) {
        if (query != null && items != null) {
            cache.put(query.trim().toLowerCase(), items);
        }
    }

    public Optional<List<LibraryItem>> get(String query) {
        if (query == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(query.trim().toLowerCase()));
    }
}

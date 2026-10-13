package com.hcl.library.util;

import com.hcl.library.model.LibraryItem;
import java.util.List;

public class CollectionUtils {

    public static <T extends Comparable<? super T>> T maxOf(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List cannot be empty");
        }
        T max = list.get(0);
        for (T item : list) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    public static double totalFees(List<? extends LibraryItem> items, int daysLate) {
        if (items == null || daysLate <= 0) {
            return 0.0;
        }
        double total = 0.0;
        for (LibraryItem item : items) {
            total += item.lateFee(daysLate);
        }
        return total;
    }
}

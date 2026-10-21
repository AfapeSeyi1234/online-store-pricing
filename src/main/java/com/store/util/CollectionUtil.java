package com.store.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Reusable generic methods. "<T>" means "any type": the same method works for
 * products, orders, customers or plain strings.
 */
public final class CollectionUtil {

    private CollectionUtil() {
    }

    // Keeps only the items that pass the rule.
    public static <T> List<T> filter(Collection<T> items, Predicate<T> rule) {
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (rule.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    // Puts items into groups. The keyOf function decides which group each item belongs to.
    // LinkedHashMap keeps the groups in the order they first appeared.
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> items, Function<T, K> keyOf) {
        Map<K, List<T>> groups = new LinkedHashMap<>();
        for (T item : items) {
            groups.computeIfAbsent(keyOf.apply(item), key -> new ArrayList<>()).add(item);
        }
        return groups;
    }

    // Removes items whose key was already seen. The first one is kept.
    public static <T, K> List<T> removeDuplicates(Collection<T> items, Function<T, K> keyOf) {
        Set<K> seen = new HashSet<>();
        List<T> unique = new ArrayList<>();
        for (T item : items) {
            if (seen.add(keyOf.apply(item))) { // add() gives false if the key is already in the set
                unique.add(item);
            }
        }
        return unique;
    }

    // Finds the keys that appear more than once.
    public static <T, K> Set<K> findDuplicateKeys(Collection<T> items, Function<T, K> keyOf) {
        Set<K> seen = new HashSet<>();
        Set<K> duplicates = new LinkedHashSet<>();
        for (T item : items) {
            K key = keyOf.apply(item);
            if (!seen.add(key)) {
                duplicates.add(key);
            }
        }
        return duplicates;
    }

    // Returns a sorted copy. The original collection is not changed.
    public static <T> List<T> sortedCopy(Collection<T> items, Comparator<T> order) {
        List<T> copy = new ArrayList<>(items);
        copy.sort(order);
        return copy;
    }

    // The biggest item, or empty if there are no items.
    public static <T> Optional<T> maxBy(Collection<T> items, Comparator<T> order) {
        T best = null;
        for (T item : items) {
            if (best == null || order.compare(item, best) > 0) {
                best = item;
            }
        }
        return Optional.ofNullable(best);
    }

    // The smallest item, or empty if there are no items.
    public static <T> Optional<T> minBy(Collection<T> items, Comparator<T> order) {
        T best = null;
        for (T item : items) {
            if (best == null || order.compare(item, best) < 0) {
                best = item;
            }
        }
        return Optional.ofNullable(best);
    }
}
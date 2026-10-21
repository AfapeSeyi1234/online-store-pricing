package com.store.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class CollectionUtilTest {

    private final List<String> words = List.of("apple", "kiwi", "pear", "banana");

    @Test
    void filterKeepsOnlyMatchingItems() {
        List<String> result = CollectionUtil.filter(words, w -> w.length() > 4);
        assertEquals(List.of("apple", "banana"), result);
    }

    @Test
    void filterOfEmptyCollectionGivesEmptyList() {
        List<String> empty = List.of();
        assertTrue(CollectionUtil.filter(empty, w -> true).isEmpty());
    }

    @Test
    void filterWithNoMatchGivesEmptyList() {
        assertTrue(CollectionUtil.filter(words, w -> w.length() > 50).isEmpty());
    }

    @Test
    void groupByPutsItemsInGroups() {
        Map<Integer, List<String>> groups = CollectionUtil.groupBy(words, String::length);
        assertEquals(3, groups.size());
        assertEquals(List.of("kiwi", "pear"), groups.get(4));
        assertEquals(List.of("apple"), groups.get(5));
    }

    @Test
    void groupByOfEmptyCollectionGivesEmptyMap() {
        List<String> empty = List.of();
        assertTrue(CollectionUtil.groupBy(empty, String::length).isEmpty());
    }

    @Test
    void removeDuplicatesKeepsTheFirstOne() {
        List<String> names = List.of("Ada", "ada", "Tunde", "ADA");
        List<String> unique = CollectionUtil.removeDuplicates(names, n -> n.toLowerCase());
        assertEquals(List.of("Ada", "Tunde"), unique);
    }

    @Test
    void findDuplicateKeysReturnsTheRepeatedKeys() {
        List<String> names = List.of("Ada", "ada", "Tunde", "ADA");
        Set<String> duplicates = CollectionUtil.findDuplicateKeys(names, n -> n.toLowerCase());
        assertEquals(Set.of("ada"), duplicates);
    }

    @Test
    void noDuplicatesGivesEmptySet() {
        assertTrue(CollectionUtil.findDuplicateKeys(words, w -> w).isEmpty());
    }

    @Test
    void sortedCopyDoesNotChangeTheOriginal() {
        List<Integer> numbers = List.of(3, 1, 2);
        Comparator<Integer> ascending = Comparator.naturalOrder();
        List<Integer> sorted = CollectionUtil.sortedCopy(numbers, ascending);
        assertEquals(List.of(1, 2, 3), sorted);
        assertEquals(List.of(3, 1, 2), numbers);
    }

    @Test
    void maxByAndMinByFindTheExtremes() {
        List<Integer> numbers = List.of(3, 9, 4);
        Comparator<Integer> ascending = Comparator.naturalOrder();
        assertEquals(Optional.of(9), CollectionUtil.maxBy(numbers, ascending));
        assertEquals(Optional.of(3), CollectionUtil.minBy(numbers, ascending));
    }

    @Test
    void maxByOfEmptyCollectionIsEmpty() {
        List<Integer> empty = List.of();
        Comparator<Integer> ascending = Comparator.naturalOrder();
        assertTrue(CollectionUtil.maxBy(empty, ascending).isEmpty());
        assertTrue(CollectionUtil.minBy(empty, ascending).isEmpty());
    }
}
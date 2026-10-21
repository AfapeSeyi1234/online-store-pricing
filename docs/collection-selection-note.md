# Collection selection note

| Collection | Where it is used | Why it was chosen |
|------------|------------------|-------------------|
| `List` | Product and order lists, search and sort results | Items keep their order, duplicates are allowed, and a list can be sorted and looped over easily. |
| `Set` (`HashSet`, `LinkedHashSet`) | Finding duplicate ids and names | A set never holds the same value twice, so `add()` returning false tells us a value was seen before. |
| `Set` (`TreeSet`) | Customers who ordered | Each name appears once and the names come out in alphabetical order. |
| `Map` (`LinkedHashMap`) | Orders grouped by status or customer; products by id | Looking up a value by its key is fast, and LinkedHashMap keeps the groups in the order they first appeared. |
| `Map` (`HashMap`) | Units sold per product | `merge()` adds up counts by key. The order does not matter because the result is sorted later. |
| `Map` (`TreeMap`) | Sales by customer | Keys come out sorted by name, which makes the report easy to read. |

## Why a generic method
`filter`, `groupBy`, `removeDuplicates`, `findDuplicateKeys`, `sortedCopy`, `maxBy` and `minBy` are written once with `<T>`
and reused for products, orders and plain strings. The rule (what to keep, how to group) is passed in as a lambda.

## Why streams
Streams are used where they make the code shorter and clear: searching, summing totals, averaging, counting by status,
grouping sales by customer, and sorting best sellers. Plain loops are kept where they are easier to read (the generic methods).

## Edge cases handled
- Empty collections: totals and averages are 0, searches and sorts give empty results.
- Duplicate records: found and removed by key, the first one is kept.
- Invalid records: bad CSV lines are skipped and reported with the line number.
- Missing search results: an empty list, never an error.
- Boundaries: date and value ranges include both ends; quantities and prices at their limits are tested.
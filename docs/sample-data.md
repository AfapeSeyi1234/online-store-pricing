# Sample data

## Products
| Id | Name | Price | Stock at start |
|----|------|-------|----------------|
| P1 | Notebook | 50.00 | 100 |
| P2 | Pen | 2.50 | 500 |
| P3 | Backpack | 120.00 | 20 |
| P4 | Desk Lamp | 35.00 | 40 |
| P5 | USB Cable | 8.99 | 200 |
| P6 | Headphones | 75.00 | 30 |

## Customers
| Id | Name | Zone | Loyalty member |
|----|------|------|----------------|
| C1 | Ada Obi | local | yes |
| C2 | Tunde Bello | regional | no |
| C3 | Chioma Eze | local | no |
| C4 | John Smith | international | no |

## Orders
| Id | Customer | Date | Items | Status |
|----|----------|------|-------|--------|
| O1 | C1 | 2026-10-01 | Notebook x2, Pen x10 | DELIVERED |
| O2 | C2 | 2026-10-03 | Backpack x1 | SHIPPED |
| O3 | C3 | 2026-10-05 | Headphones x1, USB Cable x2 | PAID |
| O4 | C1 | 2026-10-08 | Desk Lamp x3 | NEW |
| O5 | C4 | 2026-10-10 | Notebook x1 | CANCELLED |
| O6 | C2 | 2026-10-12 | Pen x20, Notebook x3 | DELIVERED |
| O7 | C3 | 2026-10-15 | Backpack x2 | PAID |
| O8 | C1 | 2026-10-18 | USB Cable x5, Headphones x1 | SHIPPED |

## Product CSV lines (with deliberate mistakes)
Used by menu option 9 and the loader tests. Lines 4 to 7 contain a duplicate id, a bad price, a negative stock and a missing column.
# Sample inputs and calculated outputs

| # | Items (price x qty) | Location | Member | Subtotal | Discount | Delivery | Tax | Final |
|---|---------------------|----------|--------|----------|----------|----------|-----|-------|
| 1 | 10.00 x 2 | local | no | 20.00 | 0.00 | 5.00 | 1.50 | 26.50 |
| 2 | 50.00 x 1 | local | no | 50.00 | 2.50 (5%) | 5.00 | 3.56 | 56.06 |
| 3 | 100.00 x 1 | local | no | 100.00 | 10.00 (10%) | 0.00 | 6.75 | 96.75 |
| 4 | 1.00 x 10 | local | no | 10.00 | 0.30 (3%) | 5.00 | 0.73 | 15.43 |
| 5 | 30.00 x 1 | local | yes | 30.00 | 1.50 (5%) | 5.00 | 2.14 | 35.64 |
| 6 | 12.00 x 10 | local | yes | 120.00 | 18.00 (15%, capped) | 0.00 | 7.65 | 109.65 |
| 7 | 20.00 x 1 | regional | no | 20.00 | 0.00 | 12.00 | 1.50 | 33.50 |
| 8 | 15.00 x 2 and 25.00 x 1 | local | no | 55.00 | 2.75 (5%) | 5.00 | 3.92 | 61.17 |

## Invalid input examples

| Input | Message |
|-------|---------|
| Price `abc` | 'abc' is not a valid price. Example: 19.99 |
| Quantity `0` | Please enter a whole number from 1 to 100. |
| Location `mars` | Please type local, regional or international. |
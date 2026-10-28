# Sample log output

Real log lines written by running `ReliabilityDemo`. The same lines are written to `logs/store.log`.
Each line shows: time, level, class, message.

Levels used:
- **DEBUG**: step-by-step details
- **INFO**: normal events
- **WARN**: expected failures (the program handled them)
- **ERROR**: unexpected failures (none happen in this demo)

```
2026-10-28 13:06:07 INFO  ProductFileStore - Saved 2 products to demo-products.csv
2026-10-28 13:06:07 INFO  ProductFileStore - Loaded 2 products from demo-products.csv, 0 bad line(s) skipped
2026-10-28 13:06:07 WARN  ProductFileStore - Product file not found: does-not-exist.csv
2026-10-28 13:06:07 DEBUG OrderService - Creating order O1
2026-10-28 13:06:07 INFO  OrderService - Order O1 created for customer C1
2026-10-28 13:06:07 DEBUG OrderService - Added 2 x P1 to order O1
2026-10-28 13:06:07 INFO  OrderService - Order O1 paid, amount 96.75
2026-10-28 13:06:07 DEBUG OrderService - Checkout finished for order O1
2026-10-28 13:06:07 DEBUG OrderService - Creating order O1
2026-10-28 13:06:07 WARN  OrderService - Duplicate order id rejected: O1
2026-10-28 13:06:07 WARN  OrderService - Order not found: O99
2026-10-28 13:06:07 DEBUG OrderService - Creating order O2
2026-10-28 13:06:07 INFO  OrderService - Order O2 created for customer C1
2026-10-28 13:06:07 DEBUG OrderService - Added 5 x P3 to order O2
2026-10-28 13:06:07 WARN  OrderService - Payment failed for order O2: Payment declined: amount is above the allowed limit.
2026-10-28 13:06:07 DEBUG OrderService - Checkout finished for order O2
2026-10-28 13:06:07 WARN  OrderService - Checkout not allowed for order O1: Only a new order can be paid.
2026-10-28 13:06:07 DEBUG OrderService - Checkout finished for order O1
```

## What the log shows

| Situation | Log line |
|-----------|----------|
| File saved and loaded (try-with-resources) | INFO, `ProductFileStore` |
| Missing file | WARN, `Product file not found` |
| Normal order | INFO, `Order O1 paid, amount 96.75` |
| Duplicate order id | WARN, `Duplicate order id rejected: O1` |
| Unknown order | WARN, `Order not found: O99` |
| Declined payment | WARN, `Payment failed for order O2` |
| Paying a paid order | WARN, `Checkout not allowed for order O1` |
| The `finally` block | DEBUG, `Checkout finished` appears after both success and failure |

## Privacy check

The log contains only order ids, customer ids, product ids, amounts and file names.
It contains no passwords, payment details or payment method, customer names, email addresses, street addresses, or full file paths.
Exceptions are logged by class name only. A test, `checkoutLogsDoNotContainPersonalData`, checks this rule.
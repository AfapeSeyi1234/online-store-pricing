# Sample output

Real output from running `ReportApp`. The repeated menu was removed to keep this short.

## Search products (option 1, text "pen")

```
Choose an option: 1
Search text: pen
P2   Pen              2.50   stock: 470
```

## Sort products by price, low to high (option 3, choice 2)

```
Choose an option: 3

1. Name   2. Price (low to high)   3. Price (high to low)
4. Stock (lowest first)   5. Stock (highest first)
Sort by: '' is not a whole number. Try again.
Sort by: 2
P2   Pen              2.50   stock: 470
P5   USB Cable        8.99   stock: 193
P4   Desk Lamp       35.00   stock: 37
P1   Notebook        50.00   stock: 95
P6   Headphones      75.00   stock: 28
P3   Backpack       120.00   stock: 17
```

The line `'' is not a whole number. Try again.` is the input validation working: an empty entry was rejected and the program asked again.

## Filter orders by status CANCELLED (option 4, choice 5)

```
Choose an option: 4
1. NEW
2. PAID
3. SHIPPED
4. DELIVERED
5. CANCELLED
Status: 5
O5   John Smith   2026-10-10 CANCELLED      81.06
```

## Sales summary (option 10)

```
Choose an option: 10
===== SALES SUMMARY =====
Orders in total: 8
Orders counted as sales: 6
Cancelled orders: 1
Total sales: 866.13
Average order value: 144.35
Customers who ordered: Ada Obi, Chioma Eze, John Smith, Tunde Bello

Orders by status:
  NEW        1
  PAID       2
  SHIPPED    2
  DELIVERED  2
  CANCELLED  1

Sales by customer:
  Ada Obi      223.83
  Chioma Eze   327.15
  Tunde Bello  315.15

Best-selling products:
  1. Pen - 30 units
  2. USB Cable - 7 units
  3. Notebook - 5 units
```

## Why the stock numbers are lower than the starting stock

Pen shows stock 470, not 500. The sample orders reduce stock when items are added to an order (Pen: 10 in O1 and 20 in O6, which is 30 units). That's the Week 2 stock rules working. You can mention it in `docs/sample-data.md`, where the table lists "Stock at start".
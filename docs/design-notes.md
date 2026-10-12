# Design decisions

## Why classes
Customer, Product, Order, Payment and Delivery are classes because their data changes over time.
A product's stock goes down when it is ordered, an order moves through statuses, and a payment
goes from pending to successful. Each class protects its own data with private fields and
methods that check the rules.

## Why records
Address and OrderItem are records because they never change after they are created.
If a customer moves, we create a new Address instead of editing the old one.
OrderItem copies the product price when it is created, so a later price change does not
change an order that was already placed.

## Why enums
Order, payment and delivery statuses and payment methods have a fixed set of values.
Enums stop typing mistakes like "shiped". OrderStatus also holds the rule for which status
can follow which (canMoveTo), so the rule lives in one place.

## Validation and business needs
- Product price must be greater than 0, and stock can never be negative, so we never sell what we do not have.
- addItem checks the stock before changing anything, so a failed add leaves nothing half-done.
- An order can only be shipped after it is paid, and cannot be cancelled after shipping.
- Cancelling gives the stock back and refunds a successful payment.
- A failed payment leaves the order NEW so the customer can try again.

## Relationships
- One Customer places many Orders.
- One Order contains many OrderItems, and each OrderItem refers to one Product.
- One Order has at most one Payment and at most one Delivery (empty until pay() and ship()).
- Customer and Delivery both use an Address.

## Link to Week 1
Order.calculatePricing() reuses the Week 1 OrderCalculator. The Address zone is passed in as the delivery location.
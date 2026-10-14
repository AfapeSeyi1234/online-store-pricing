# Code review checklist (Week 3)

Reviewer: ____________   Date: ________   Pull request: ____________

## Structure
- [ ] Every class is in a sensibly named package
- [ ] `model` does not use `service` or `repository`
- [ ] No unused classes or imports remain

## Interfaces and design
- [ ] Each interface solves a real need (more than one possible implementation)
- [ ] Each interface has at least one implementation, and there are at least two across the project
- [ ] Composition is used where inheritance is not needed
- [ ] Adding a new discount rule does not require editing `PricingService`

## Clean code
- [ ] Class, method and variable names say what they do
- [ ] Methods are short and do one job
- [ ] Duplicated logic has been removed
- [ ] No magic numbers without a named constant

## Tests
- [ ] All previous tests still pass
- [ ] New classes have tests (normal, boundary and invalid cases)
- [ ] Test names describe the behaviour

## Behaviour
- [ ] Prices, discounts, delivery and tax give the same results as Week 2
- [ ] Error messages are clear

## Reviewer comments and fixes
| # | Reviewer comment                                                                                                                                                                                     | My fix | Commit |
|---|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|----|
| 1 | BulkDiscountPolicy.java, line 10 return 3; is a magic number. Could it be a named constant like BULK_PERCENT, the same way BULK_UNITS is named above it? That makes the rule easier to change later. | BulkDiscountPolicy.java: replace the magic number with a constant       | 70c87bc  |
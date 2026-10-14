package com.store.pricing;

import com.store.exception.ValidationException;
import com.store.util.MoneyUtil;
import com.store.validation.OrderInputValidator;

import java.util.List;

public class PricingService {

    public static final double TAX_RATE = 0.075;
    public static final double MAX_DISCOUNT_PERCENT = 15;

    private final List<DiscountPolicy> discountPolicies;
    private final ShippingCalculator shippingCalculator;

    public PricingService(List<DiscountPolicy> discountPolicies, ShippingCalculator shippingCalculator) {
        if (discountPolicies == null || shippingCalculator == null) {
            throw new ValidationException("Discount policies and shipping calculator are required.");
        }
        this.discountPolicies = List.copyOf(discountPolicies);
        this.shippingCalculator = shippingCalculator;
    }

    // The normal store setup: the same rules as Week 1.
    public static PricingService standard() {
        return new PricingService(
                List.of(new TierDiscountPolicy(), new BulkDiscountPolicy(), new MemberDiscountPolicy()),
                new ZoneShippingCalculator());
    }

    public OrderResult calculate(double[] prices, int[] quantities, String location, boolean isMember) {
        checkInput(prices, quantities, location);

        double subtotal = MoneyUtil.round2(sumOfLines(prices, quantities));
        int totalUnits = sumOfUnits(quantities);

        double discountPercent = totalDiscountPercent(subtotal, totalUnits, isMember);
        double discount = MoneyUtil.round2(subtotal * discountPercent / 100);
        double discountedSubtotal = MoneyUtil.round2(subtotal - discount);
        double deliveryFee = shippingCalculator.feeFor(location, subtotal);
        double tax = MoneyUtil.round2(discountedSubtotal * TAX_RATE);
        double total = MoneyUtil.round2(discountedSubtotal + deliveryFee + tax);

        return buildResult(subtotal, discountPercent, discount, discountedSubtotal, deliveryFee, tax, total);
    }

    private void checkInput(double[] prices, int[] quantities, String location) {
        String error = OrderInputValidator.validate(prices, quantities, location);
        if (error != null) {
            throw new ValidationException(error);
        }
    }

    private double sumOfLines(double[] prices, int[] quantities) {
        double sum = 0;
        for (int i = 0; i < prices.length; i++) {
            sum = sum + prices[i] * quantities[i];
        }
        return sum;
    }

    private int sumOfUnits(int[] quantities) {
        int sum = 0;
        for (int quantity : quantities) {
            sum = sum + quantity;
        }
        return sum;
    }

    // Adds up what every discount policy gives, then applies the cap.
    private double totalDiscountPercent(double subtotal, int totalUnits, boolean isMember) {
        double percent = 0;
        for (DiscountPolicy policy : discountPolicies) {
            percent = percent + policy.percentFor(subtotal, totalUnits, isMember);
        }
        return Math.min(percent, MAX_DISCOUNT_PERCENT);
    }

    private OrderResult buildResult(double subtotal, double discountPercent, double discount,
                                    double discountedSubtotal, double deliveryFee, double tax, double total) {
        OrderResult result = new OrderResult();
        result.subtotal = subtotal;
        result.discountPercent = discountPercent;
        result.discount = discount;
        result.discountedSubtotal = discountedSubtotal;
        result.deliveryFee = deliveryFee;
        result.tax = tax;
        result.total = total;
        return result;
    }
}
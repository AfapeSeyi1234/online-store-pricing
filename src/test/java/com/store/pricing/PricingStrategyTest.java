package com.store.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.store.exception.ValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PricingStrategyTest {

    // ---------- DiscountPolicy implementations ----------

    @Test
    void tierPolicyGivesZeroFiveOrTenPercent() {
        DiscountPolicy policy = new TierDiscountPolicy();
        assertEquals(0, policy.percentFor(49.99, 1, false), 0.01);
        assertEquals(5, policy.percentFor(50.00, 1, false), 0.01);
        assertEquals(5, policy.percentFor(99.99, 1, false), 0.01);
        assertEquals(10, policy.percentFor(100.00, 1, false), 0.01);
    }

    @Test
    void bulkPolicyStartsAtTenUnits() {
        DiscountPolicy policy = new BulkDiscountPolicy();
        assertEquals(0, policy.percentFor(10, 9, false), 0.01);
        assertEquals(3, policy.percentFor(10, 10, false), 0.01);
    }

    @Test
    void memberPolicyOnlyHelpsMembers() {
        DiscountPolicy policy = new MemberDiscountPolicy();
        assertEquals(5, policy.percentFor(10, 1, true), 0.01);
        assertEquals(0, policy.percentFor(10, 1, false), 0.01);
    }

    // ---------- ShippingCalculator implementations ----------

    @Test
    void zoneCalculatorFreeShippingThresholds() {
        ShippingCalculator shipping = new ZoneShippingCalculator();
        assertEquals(5, shipping.feeFor("local", 79.99), 0.01);
        assertEquals(0, shipping.feeFor("local", 80.00), 0.01);
        assertEquals(12, shipping.feeFor("regional", 199.99), 0.01);
        assertEquals(0, shipping.feeFor("regional", 200.00), 0.01);
    }

    @Test
    void internationalShippingIsNeverFree() {
        ShippingCalculator shipping = new ZoneShippingCalculator();
        assertEquals(30, shipping.feeFor("international", 5000), 0.01);
    }

    @Test
    void flatRateChargesTheSameEverywhere() {
        ShippingCalculator shipping = new FlatRateShippingCalculator(7.50);
        assertEquals(7.50, shipping.feeFor("local", 1), 0.01);
        assertEquals(7.50, shipping.feeFor("international", 5000), 0.01);
    }

    @Test
    void flatRateRejectsNegativeFee() {
        assertThrows(ValidationException.class, () -> new FlatRateShippingCalculator(-1));
    }

    // ---------- Replaceable behaviour ----------

    @Test
    void customPoliciesCanBePluggedIn() {
        DiscountPolicy tenPercent = (subtotal, units, member) -> 10; // a quick one-line policy
        PricingService service = new PricingService(List.of(tenPercent), new FlatRateShippingCalculator(2.00));

        OrderResult r = service.calculate(new double[]{20.00}, new int[]{1}, "regional", false);

        assertEquals(2.00, r.discount, 0.01);
        assertEquals(2.00, r.deliveryFee, 0.01);
        assertEquals(1.35, r.tax, 0.01);
        assertEquals(21.35, r.total, 0.01);
    }

    @Test
    void noPoliciesMeansNoDiscount() {
        PricingService service = new PricingService(List.of(), new ZoneShippingCalculator());
        OrderResult r = service.calculate(new double[]{100.00}, new int[]{1}, "local", true);
        assertEquals(0, r.discount, 0.01);
    }
}
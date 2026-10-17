package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DamageListenerTest {

    @Test
    void finalDamageIsKeptWithoutRoundingWhenBelowCurrentHealth() {
        assertEquals(8.0, DamageListener.healthDamage(20, 8));
        assertEquals(0.003, DamageListener.healthDamage(20, 0.003));
    }

    @Test
    void overkillIsCappedAtTheTargetsPreHitHealth() {
        assertEquals(20.0, DamageListener.healthDamage(20, 100));
        assertEquals(0.5, DamageListener.healthDamage(0.5, 100));
        assertEquals(20.0, DamageListener.healthDamage(20, 20));
    }

    @Test
    void zeroFinalDamageRemainsZero() {
        assertEquals(0.0, DamageListener.healthDamage(20, 0));
    }

    @Test
    void zeroOrNegativeHealthAndNegativeDamageProduceZero() {
        assertEquals(0.0, DamageListener.healthDamage(0, 8));
        assertEquals(0.0, DamageListener.healthDamage(-1, 8));
        assertEquals(0.0, DamageListener.healthDamage(20, -1));
        assertEquals(0.0, DamageListener.healthDamage(-1, -1));
    }

    @Test
    void nonFiniteHealthOrDamageProducesZero() {
        for (double nonFinite : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0.0, DamageListener.healthDamage(nonFinite, 8));
            assertEquals(0.0, DamageListener.healthDamage(20, nonFinite));
            assertEquals(0.0, DamageListener.healthDamage(nonFinite, nonFinite));
        }
    }
}

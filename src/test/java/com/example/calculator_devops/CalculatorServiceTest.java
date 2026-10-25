package com.example.calculator_devops;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Plain JUnit 5 unit tests for every operation (no Spring context).
 */
class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    @Test
    void addReturnsSum() {
        assertEquals(5.0, service.add(2, 3));
        assertEquals(-1.5, service.add(-3, 1.5));
    }

    @Test
    void subtractReturnsDifference() {
        assertEquals(6.0, service.subtract(10, 4));
        assertEquals(-2.0, service.subtract(3, 5));
    }

    @Test
    void multiplyReturnsProduct() {
        assertEquals(12.0, service.multiply(4, 3));
        assertEquals(0.0, service.multiply(7, 0));
    }

    @Test
    void divideReturnsQuotient() {
        assertEquals(5.0, service.divide(10, 2));
        assertEquals(2.5, service.divide(5, 2));
    }

    @Test
    void divideByZeroThrows() {
        ArithmeticException ex = assertThrows(ArithmeticException.class, () -> service.divide(10, 0));
        assertEquals("Division by zero is not allowed", ex.getMessage());
    }

    @Test
    void nonFiniteInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.add(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> service.multiply(1, Double.POSITIVE_INFINITY));
    }
}

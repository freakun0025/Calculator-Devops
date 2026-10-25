package com.example.calculator_devops;

import org.springframework.stereotype.Service;

/**
 * Pure business logic for the calculator, kept separate from the web layer
 * so it can be unit tested without starting Spring.
 */
@Service
public class CalculatorService {

    public double add(double a, double b) {
        requireFinite(a, b);
        return a + b;
    }

    public double subtract(double a, double b) {
        requireFinite(a, b);
        return a - b;
    }

    public double multiply(double a, double b) {
        requireFinite(a, b);
        return a * b;
    }

    public double divide(double a, double b) {
        requireFinite(a, b);
        if (b == 0) {
            throw new ArithmeticException("Division by zero is not allowed");
        }
        return a / b;
    }

    // Rejects NaN / Infinity, which Java happily parses from "NaN" or "Infinity"
    private static void requireFinite(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            throw new IllegalArgumentException("Parameters 'a' and 'b' must be finite numbers");
        }
    }
}

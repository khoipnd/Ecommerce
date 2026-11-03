import { describe, it, expect } from 'vitest';
import { calculateCartTotal, formatCurrency } from './cartLogic';

describe('cartLogic', () => {
    it('calculateCartTotal should compute correct total', () => {
        const items = [
            { product: { price: 100 }, quantity: 2 },
            { product: { price: 50 }, quantity: 1 }
        ];
        expect(calculateCartTotal(items)).toBe(250);
    });

    it('calculateCartTotal should return 0 for empty cart', () => {
        expect(calculateCartTotal([])).toBe(0);
    });

    it('formatCurrency should format correctly', () => {
        expect(formatCurrency(250.5)).toBe('$250.50');
    });
});

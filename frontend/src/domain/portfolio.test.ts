import { describe, expect, it } from 'vitest';

import { calculatePosition } from './portfolio';

describe('calculatePosition', () => {
  it('calculates a new purchase from an empty baseline', () => {
    expect(
      calculatePosition({ averagePrice: '0', quantity: '0' }, [
        { price: '15000', quantity: '3' },
      ]),
    ).toEqual({ averagePrice: '15000', totalCost: '45000', quantity: '3' });
  });

  it('combines the baseline and multiple managed purchases exactly once', () => {
    expect(
      calculatePosition({ averagePrice: '10000', quantity: '10' }, [
        { price: '8000', quantity: '5' },
        { price: '7000', quantity: '5' },
      ]),
    ).toEqual({ averagePrice: '8750', totalCost: '175000', quantity: '20' });
  });

  it('recalculates from the complete edited or deleted history', () => {
    const baseline = { averagePrice: '10000', quantity: '10' };
    const edited = calculatePosition(baseline, [
      { price: '6000', quantity: '5' },
      { price: '9000', quantity: '5' },
    ]);
    const deleted = calculatePosition(baseline, [{ price: '6000', quantity: '5' }]);

    expect(edited.averagePrice).toBe('8750');
    expect(deleted).toEqual({ averagePrice: '8666.66666667', totalCost: '130000', quantity: '15' });
  });

  it('represents an empty position without dividing by zero', () => {
    expect(calculatePosition({ averagePrice: '0', quantity: '0' }, [])).toEqual({
      averagePrice: null,
      totalCost: '0',
      quantity: '0',
    });
  });

  it('preserves large values and fractional quantities without floating-point drift', () => {
    expect(
      calculatePosition({ averagePrice: '999999999999.99', quantity: '0.1' }, [
        { price: '0.01', quantity: '0.2' },
      ]),
    ).toEqual({
      averagePrice: '333333333333.33666667',
      totalCost: '100000000000.001',
      quantity: '0.3',
    });
  });
});

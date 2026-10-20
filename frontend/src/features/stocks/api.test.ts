import { describe, expect, it } from 'vitest';

import { addPurchase, createStock, deletePurchase, setStockAlert, updatePurchase } from './api';

describe('stock demo API', () => {
  it('recalculates the whole position after adding, editing, and deleting a purchase', async () => {
    const stock = await createStock({
      name: '테스트 종목',
      symbol: 'TEST',
      market: 'KRX',
      averagePrice: '100',
      quantity: '10',
    });

    const added = await addPurchase(stock.id, {
      price: '80',
      quantity: '5',
      purchasedAt: '2026-10-20',
    });
    expect(added.summary).toEqual({ averagePrice: '93.33333333', totalCost: '1400', quantity: '15' });

    const edited = await updatePurchase(stock.id, added.purchases[0].id, {
      price: '70',
      quantity: '10',
      purchasedAt: '2026-10-20',
    });
    expect(edited.summary).toEqual({ averagePrice: '85', totalCost: '1700', quantity: '20' });

    const deleted = await deletePurchase(stock.id, edited.purchases[0].id);
    expect(deleted.summary).toEqual({ averagePrice: '100', totalCost: '1000', quantity: '10' });

    const alerted = await setStockAlert(stock.id, true);
    expect(alerted.alertEnabled).toBe(true);
  });
});

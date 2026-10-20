import { calculatePosition } from '../../domain/portfolio';

import type { CreateStockInput, Purchase, SavePurchaseInput, Stock } from './types';

const wait = (duration = 180) => new Promise((resolve) => setTimeout(resolve, duration));

let nextStockId = 3;
let nextPurchaseId = 4;

const stocks = new Map<string, Stock>([
  [
    '1',
    {
      id: '1',
      name: '삼성전자',
      symbol: '005930',
      market: 'KRX',
      baseline: { averagePrice: '73000', quantity: '12' },
      purchases: [{ id: '1', price: '68000', quantity: '3', purchasedAt: '2026-10-18' }],
      summary: { averagePrice: '72000', quantity: '15', totalCost: '1080000' },
      alertEnabled: true,
    },
  ],
  [
    '2',
    {
      id: '2',
      name: 'TIGER 미국S&P500',
      symbol: '360750',
      market: 'KRX',
      baseline: { averagePrice: '130000', quantity: '5' },
      purchases: [],
      summary: { averagePrice: '130000', quantity: '5', totalCost: '650000' },
      alertEnabled: false,
    },
  ],
]);

function cloneStock(stock: Stock): Stock {
  return {
    ...stock,
    baseline: { ...stock.baseline },
    summary: { ...stock.summary },
    purchases: stock.purchases.map((purchase) => ({ ...purchase })),
  };
}

function requireStock(id: string): Stock {
  const stock = stocks.get(id);
  if (!stock) throw new Error('종목을 찾을 수 없습니다.');
  return stock;
}

function recalculate(stock: Stock): Stock {
  const recalculated = {
    ...stock,
    summary: calculatePosition(stock.baseline, stock.purchases),
  };
  stocks.set(stock.id, recalculated);
  return recalculated;
}

export async function listStocks(): Promise<Stock[]> {
  await wait();
  return [...stocks.values()].map(cloneStock);
}

export async function getStock(id: string): Promise<Stock> {
  await wait(120);
  return cloneStock(requireStock(id));
}

export async function createStock(input: CreateStockInput): Promise<Stock> {
  await wait();
  const name = input.name.trim();
  const symbol = input.symbol.trim().toUpperCase();
  const market = input.market.trim().toUpperCase();
  if (!name || !symbol || !market) throw new Error('종목명, 종목 코드, 시장을 모두 입력해 주세요.');

  const baseline = { averagePrice: input.averagePrice, quantity: input.quantity };
  const summary = calculatePosition(baseline, []);
  if (summary.averagePrice === null) throw new Error('보유 수량은 0보다 커야 합니다.');

  const stock: Stock = {
    id: String(nextStockId++),
    name,
    symbol,
    market,
    baseline,
    purchases: [],
    summary,
    alertEnabled: false,
  };
  stocks.set(stock.id, stock);
  return cloneStock(stock);
}

export async function addPurchase(stockId: string, input: SavePurchaseInput): Promise<Stock> {
  await wait();
  const stock = requireStock(stockId);
  const purchase: Purchase = { id: String(nextPurchaseId++), ...input };
  return cloneStock(recalculate({ ...stock, purchases: [...stock.purchases, purchase] }));
}

export async function updatePurchase(stockId: string, purchaseId: string, input: SavePurchaseInput): Promise<Stock> {
  await wait();
  const stock = requireStock(stockId);
  if (!stock.purchases.some((purchase) => purchase.id === purchaseId)) {
    throw new Error('매수 이력을 찾을 수 없습니다.');
  }
  const purchases = stock.purchases.map((purchase) =>
    purchase.id === purchaseId ? { id: purchase.id, ...input } : purchase,
  );
  return cloneStock(recalculate({ ...stock, purchases }));
}

export async function deletePurchase(stockId: string, purchaseId: string): Promise<Stock> {
  await wait();
  const stock = requireStock(stockId);
  const purchases = stock.purchases.filter((purchase) => purchase.id !== purchaseId);
  if (purchases.length === stock.purchases.length) throw new Error('매수 이력을 찾을 수 없습니다.');
  return cloneStock(recalculate({ ...stock, purchases }));
}

export async function setStockAlert(stockId: string, enabled: boolean): Promise<Stock> {
  await wait(120);
  const stock = requireStock(stockId);
  const updated = { ...stock, alertEnabled: enabled };
  stocks.set(stockId, updated);
  return cloneStock(updated);
}

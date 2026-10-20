import type { PositionBaseline, PositionSummary, PurchaseInput } from '../../domain/portfolio';

export type Purchase = PurchaseInput & {
  id: string;
  purchasedAt: string;
};

export type Stock = {
  id: string;
  name: string;
  symbol: string;
  market: string;
  baseline: PositionBaseline;
  purchases: Purchase[];
  summary: PositionSummary;
  alertEnabled: boolean;
};

export type CreateStockInput = {
  name: string;
  symbol: string;
  market: string;
  averagePrice: string;
  quantity: string;
};

export type SavePurchaseInput = PurchaseInput & {
  purchasedAt: string;
};

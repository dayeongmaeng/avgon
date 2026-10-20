import {
  add,
  decimal,
  divide,
  isPositive,
  isZero,
  multiply,
  toDecimalString,
  type Decimal,
} from './decimal';

export type PositionBaseline = Readonly<{
  averagePrice: string;
  quantity: string;
}>;

export type PurchaseInput = Readonly<{
  price: string;
  quantity: string;
}>;

export type PositionSummary = Readonly<{
  averagePrice: string | null;
  totalCost: string;
  quantity: string;
}>;

const ZERO = decimal('0');

function validateBaseline(baseline: PositionBaseline): {
  averagePrice: Decimal;
  quantity: Decimal;
} {
  const averagePrice = decimal(baseline.averagePrice);
  const quantity = decimal(baseline.quantity);
  if (quantity.unscaled < 0n || averagePrice.unscaled < 0n) {
    throw new Error('기준 보유 정보는 0 이상이어야 합니다.');
  }
  if (isPositive(quantity) && !isPositive(averagePrice)) {
    throw new Error('보유 수량이 있으면 기준 평단가는 0보다 커야 합니다.');
  }
  return { averagePrice, quantity };
}

function validatePurchase(purchase: PurchaseInput): { price: Decimal; quantity: Decimal } {
  const price = decimal(purchase.price);
  const quantity = decimal(purchase.quantity);
  if (!isPositive(price) || !isPositive(quantity)) {
    throw new Error('매수가와 매수 수량은 0보다 커야 합니다.');
  }
  return { price, quantity };
}

export function calculatePosition(
  baseline: PositionBaseline,
  purchases: readonly PurchaseInput[],
): PositionSummary {
  const parsedBaseline = validateBaseline(baseline);
  let totalQuantity = parsedBaseline.quantity;
  let totalCost = multiply(parsedBaseline.averagePrice, parsedBaseline.quantity);

  for (const purchase of purchases) {
    const parsedPurchase = validatePurchase(purchase);
    totalQuantity = add(totalQuantity, parsedPurchase.quantity);
    totalCost = add(totalCost, multiply(parsedPurchase.price, parsedPurchase.quantity));
  }

  return {
    averagePrice: isZero(totalQuantity)
      ? null
      : toDecimalString(divide(totalCost, totalQuantity, 8)),
    totalCost: toDecimalString(totalCost),
    quantity: toDecimalString(totalQuantity),
  };
}

export function calculateAdditionalPurchase(
  baseline: PositionBaseline,
  purchase: PurchaseInput,
): PositionSummary {
  return calculatePosition(baseline, [purchase]);
}

export function emptyPosition(): PositionSummary {
  return { averagePrice: null, totalCost: toDecimalString(ZERO), quantity: '0' };
}

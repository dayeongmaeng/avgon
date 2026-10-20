export type Decimal = Readonly<{
  unscaled: bigint;
  scale: number;
}>;

const DECIMAL_PATTERN = /^\d+(?:\.\d+)?$/;

function powerOfTen(exponent: number): bigint {
  return 10n ** BigInt(exponent);
}

function normalize(value: Decimal): Decimal {
  let { unscaled, scale } = value;
  while (scale > 0 && unscaled % 10n === 0n) {
    unscaled /= 10n;
    scale -= 1;
  }
  return { unscaled, scale };
}

export function decimal(value: string): Decimal {
  const trimmed = value.trim();
  if (!DECIMAL_PATTERN.test(trimmed)) {
    throw new Error('올바른 숫자를 입력해 주세요.');
  }

  const [whole, fraction = ''] = trimmed.split('.');
  return normalize({
    unscaled: BigInt(`${whole}${fraction}`),
    scale: fraction.length,
  });
}

export function add(left: Decimal, right: Decimal): Decimal {
  const scale = Math.max(left.scale, right.scale);
  return normalize({
    unscaled:
      left.unscaled * powerOfTen(scale - left.scale) +
      right.unscaled * powerOfTen(scale - right.scale),
    scale,
  });
}

export function multiply(left: Decimal, right: Decimal): Decimal {
  return normalize({
    unscaled: left.unscaled * right.unscaled,
    scale: left.scale + right.scale,
  });
}

export function isZero(value: Decimal): boolean {
  return value.unscaled === 0n;
}

export function isPositive(value: Decimal): boolean {
  return value.unscaled > 0n;
}

export function divide(left: Decimal, right: Decimal, scale = 8): Decimal {
  if (isZero(right)) {
    throw new Error('0으로 나눌 수 없습니다.');
  }

  const numerator = left.unscaled * powerOfTen(right.scale + scale);
  const denominator = right.unscaled * powerOfTen(left.scale);
  let quotient = numerator / denominator;
  const remainder = numerator % denominator;

  if (remainder * 2n >= denominator) {
    quotient += 1n;
  }

  return normalize({ unscaled: quotient, scale });
}

export function toDecimalString(value: Decimal): string {
  const normalized = normalize(value);
  if (normalized.scale === 0) {
    return normalized.unscaled.toString();
  }

  const digits = normalized.unscaled.toString().padStart(normalized.scale + 1, '0');
  const boundary = digits.length - normalized.scale;
  return `${digits.slice(0, boundary)}.${digits.slice(boundary)}`;
}

export function formatDecimal(value: string | null, maximumFractionDigits = 8): string {
  if (value === null) {
    return '-';
  }

  const parsed = decimal(value);
  const raw = toDecimalString(parsed);
  const [whole, fraction] = raw.split('.');
  const grouped = whole.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  if (!fraction) {
    return grouped;
  }
  return `${grouped}.${fraction.slice(0, maximumFractionDigits)}`;
}

export function sanitizeDecimalInput(value: string): string {
  const sanitized = value.replace(/,/g, '').replace(/[^\d.]/g, '');
  const [whole = '', ...fractionParts] = sanitized.split('.');
  return fractionParts.length > 0 ? `${whole}.${fractionParts.join('')}` : whole;
}

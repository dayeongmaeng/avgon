import { create } from 'zustand';

type CalculatorField = 'currentAveragePrice' | 'currentQuantity' | 'purchasePrice' | 'purchaseQuantity';

type CalculatorState = Record<CalculatorField, string> & {
  setField: (field: CalculatorField, value: string) => void;
  reset: () => void;
};

const initialValues: Record<CalculatorField, string> = {
  currentAveragePrice: '',
  currentQuantity: '',
  purchasePrice: '',
  purchaseQuantity: '',
};

export const useCalculatorStore = create<CalculatorState>((set) => ({
  ...initialValues,
  setField: (field, value) => set({ [field]: value }),
  reset: () => set(initialValues),
}));

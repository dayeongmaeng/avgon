import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { addPurchase, createStock, deletePurchase, getStock, listStocks, setStockAlert, updatePurchase } from './api';
import type { CreateStockInput, SavePurchaseInput } from './types';

export const stockKeys = {
  all: ['stocks'] as const,
  detail: (id: string) => ['stocks', id] as const,
};

export function useStocks() {
  return useQuery({ queryKey: stockKeys.all, queryFn: listStocks });
}

export function useStock(id: string) {
  return useQuery({ queryKey: stockKeys.detail(id), queryFn: () => getStock(id), enabled: Boolean(id) });
}

function useRefreshStock(stockId?: string) {
  const queryClient = useQueryClient();
  return async () => {
    await queryClient.invalidateQueries({ queryKey: stockKeys.all });
    if (stockId) await queryClient.invalidateQueries({ queryKey: stockKeys.detail(stockId) });
  };
}

export function useCreateStock() {
  const refresh = useRefreshStock();
  return useMutation({ mutationFn: (input: CreateStockInput) => createStock(input), onSuccess: refresh });
}

export function useAddPurchase(stockId: string) {
  const refresh = useRefreshStock(stockId);
  return useMutation({ mutationFn: (input: SavePurchaseInput) => addPurchase(stockId, input), onSuccess: refresh });
}

export function useUpdatePurchase(stockId: string) {
  const refresh = useRefreshStock(stockId);
  return useMutation({
    mutationFn: ({ purchaseId, input }: { purchaseId: string; input: SavePurchaseInput }) =>
      updatePurchase(stockId, purchaseId, input),
    onSuccess: refresh,
  });
}

export function useDeletePurchase(stockId: string) {
  const refresh = useRefreshStock(stockId);
  return useMutation({ mutationFn: (purchaseId: string) => deletePurchase(stockId, purchaseId), onSuccess: refresh });
}

export function useSetStockAlert(stockId: string) {
  const refresh = useRefreshStock(stockId);
  return useMutation({ mutationFn: (enabled: boolean) => setStockAlert(stockId, enabled), onSuccess: refresh });
}

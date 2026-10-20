import { useLocalSearchParams } from 'expo-router';

import { StockDetailScreen } from '@/features/stocks/StockDetailScreen';

export default function StockDetailRoute() {
  const { id } = useLocalSearchParams<{ id: string }>();
  return <StockDetailScreen id={id ?? ''} />;
}

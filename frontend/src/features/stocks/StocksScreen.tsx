import { useState } from 'react';
import { Pressable, StyleSheet, Text, View, useWindowDimensions } from 'react-native';
import { router } from 'expo-router';

import { DecimalField, EmptyState, LoadingState, PrimaryButton, Screen, SectionCard, TextField } from '@/components/ui';
import { formatDecimal } from '@/domain/decimal';
import { colors } from '@/theme';

import { useCreateStock, useStocks } from './queries';
import type { CreateStockInput, Stock } from './types';

const EMPTY_FORM: CreateStockInput = { name: '', symbol: '', market: 'KRX', averagePrice: '', quantity: '' };

function StockCard({ stock }: { stock: Stock }) {
  return (
    <Pressable
      accessibilityLabel={`${stock.name} 상세 보기`}
      accessibilityRole="button"
      onPress={() => router.push({ pathname: '/stock/[id]', params: { id: stock.id } })}
      style={({ pressed }) => [styles.stockCard, pressed && styles.pressed]}
    >
      <View style={styles.stockTop}>
        <View style={styles.stockIdentity}>
          <View style={styles.stockAvatar}><Text style={styles.stockAvatarText}>{stock.name.slice(0, 1)}</Text></View>
          <View style={styles.stockNameWrap}>
            <Text numberOfLines={1} style={styles.stockName}>{stock.name}</Text>
            <Text style={styles.stockCode}>{stock.market} · {stock.symbol}</Text>
          </View>
        </View>
        <View style={[styles.alertBadge, stock.alertEnabled && styles.alertBadgeOn]}>
          <Text style={[styles.alertBadgeText, stock.alertEnabled && styles.alertBadgeTextOn]}>{stock.alertEnabled ? '알림 ON' : '알림 OFF'}</Text>
        </View>
      </View>
      <View style={styles.stockValues}>
        <View>
          <Text style={styles.valueLabel}>현재 평단가</Text>
          <Text style={styles.averageValue}>{formatDecimal(stock.summary.averagePrice)}원</Text>
        </View>
        <View style={styles.quantityValueWrap}>
          <Text style={styles.valueLabel}>보유 수량</Text>
          <Text style={styles.quantityValue}>{formatDecimal(stock.summary.quantity)}주</Text>
        </View>
      </View>
      <View style={styles.historyHint}>
        <Text style={styles.historyHintText}>매수 이력 {stock.purchases.length}건</Text>
        <Text style={styles.chevron}>›</Text>
      </View>
    </Pressable>
  );
}

export function StocksScreen() {
  const { width } = useWindowDimensions();
  const compact = width < 620;
  const stocksQuery = useStocks();
  const createMutation = useCreateStock();
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<CreateStockInput>(EMPTY_FORM);

  const submit = () => {
    createMutation.mutate(form, {
      onSuccess: () => {
        setForm(EMPTY_FORM);
        setShowForm(false);
      },
    });
  };

  return (
    <Screen>
      <View style={styles.headerRow}>
        <View style={styles.headerCopy}>
          <Text style={styles.title}>내 종목</Text>
          <Text style={styles.subtitle}>평단가와 매수 이력을 한곳에서 관리하세요.</Text>
        </View>
        <Pressable onPress={() => setShowForm((visible) => !visible)} style={styles.addButton}>
          <Text style={styles.addButtonText}>{showForm ? '닫기' : '+ 종목 추가'}</Text>
        </Pressable>
      </View>

      {showForm ? (
        <SectionCard style={styles.createCard}>
          <View>
            <Text style={styles.formTitle}>새 종목 등록</Text>
            <Text style={styles.formDescription}>현재 보유 정보는 이후 매수 이력과 중복되지 않는 기준값으로 저장돼요.</Text>
          </View>
          <View style={[styles.formRow, compact && styles.formColumn]}>
            <TextField label="종목명" placeholder="예: 삼성전자" value={form.name} onChangeText={(name) => setForm({ ...form, name })} />
            <TextField label="종목 코드" placeholder="예: 005930" autoCapitalize="characters" value={form.symbol} onChangeText={(symbol) => setForm({ ...form, symbol })} />
          </View>
          <View style={[styles.formRow, compact && styles.formColumn]}>
            <TextField label="시장" placeholder="KRX" autoCapitalize="characters" value={form.market} onChangeText={(market) => setForm({ ...form, market })} />
            <DecimalField label="기준 평단가" suffix="원" value={form.averagePrice} onChangeText={(averagePrice) => setForm({ ...form, averagePrice })} />
            <DecimalField label="기준 수량" suffix="주" value={form.quantity} onChangeText={(quantity) => setForm({ ...form, quantity })} />
          </View>
          {createMutation.error ? <Text style={styles.error}>{createMutation.error.message}</Text> : null}
          <PrimaryButton label="종목 저장" onPress={submit} loading={createMutation.isPending} disabled={!form.name || !form.symbol || !form.market || !form.averagePrice || !form.quantity} />
        </SectionCard>
      ) : null}

      {stocksQuery.isLoading ? <LoadingState /> : null}
      {stocksQuery.isError ? (
        <SectionCard><EmptyState icon="!" title="종목을 불러오지 못했어요" description="잠시 후 다시 시도해 주세요." /><PrimaryButton label="다시 시도" variant="secondary" onPress={() => stocksQuery.refetch()} /></SectionCard>
      ) : null}
      {stocksQuery.data?.length === 0 ? (
        <SectionCard><EmptyState icon="＋" title="아직 등록한 종목이 없어요" description="보유 종목을 추가하면 평단가와 매수 이력을 관리할 수 있어요." /></SectionCard>
      ) : null}
      <View style={styles.stockList}>{stocksQuery.data?.map((stock) => <StockCard key={stock.id} stock={stock} />)}</View>

      <View style={styles.demoNotice}>
        <Text style={styles.demoNoticeTitle}>데모 데이터로 실행 중</Text>
        <Text style={styles.demoNoticeText}>현재 종목 데이터는 앱 실행 중에만 유지됩니다. 서버 연결 시 같은 화면에서 실제 계정 데이터로 교체할 수 있어요.</Text>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  headerRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', gap: 14, marginBottom: 22 },
  headerCopy: { flex: 1 },
  title: { color: colors.text, fontSize: 30, fontWeight: '900', letterSpacing: -0.8 },
  subtitle: { color: colors.textMuted, fontSize: 14, lineHeight: 21, marginTop: 7 },
  addButton: { backgroundColor: colors.primary, borderRadius: 13, paddingHorizontal: 15, paddingVertical: 12 },
  addButtonText: { color: '#FFFFFF', fontSize: 13, fontWeight: '800' },
  createCard: { gap: 16, marginBottom: 16 },
  formTitle: { color: colors.text, fontSize: 18, fontWeight: '800' },
  formDescription: { color: colors.textMuted, fontSize: 12, lineHeight: 18, marginTop: 4 },
  formRow: { flexDirection: 'row', gap: 10 },
  formColumn: { flexDirection: 'column' },
  error: { color: colors.danger, fontSize: 13 },
  stockList: { gap: 12 },
  stockCard: { backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.border, borderRadius: 20, padding: 18 },
  pressed: { opacity: 0.72 },
  stockTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 10 },
  stockIdentity: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: 11 },
  stockAvatar: { width: 40, height: 40, borderRadius: 13, backgroundColor: colors.primarySoft, alignItems: 'center', justifyContent: 'center' },
  stockAvatarText: { color: colors.primary, fontSize: 17, fontWeight: '900' },
  stockNameWrap: { flex: 1 },
  stockName: { color: colors.text, fontSize: 16, fontWeight: '800' },
  stockCode: { color: colors.textMuted, fontSize: 11, marginTop: 3 },
  alertBadge: { backgroundColor: '#EFF1F0', borderRadius: 20, paddingHorizontal: 9, paddingVertical: 6 },
  alertBadgeOn: { backgroundColor: colors.primarySoft },
  alertBadgeText: { color: colors.textMuted, fontSize: 10, fontWeight: '800' },
  alertBadgeTextOn: { color: colors.primary },
  stockValues: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-end', marginTop: 20 },
  valueLabel: { color: colors.textMuted, fontSize: 11, marginBottom: 5 },
  averageValue: { color: colors.text, fontSize: 23, fontWeight: '900' },
  quantityValueWrap: { alignItems: 'flex-end' },
  quantityValue: { color: colors.text, fontSize: 15, fontWeight: '800' },
  historyHint: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', borderTopWidth: 1, borderTopColor: colors.border, marginTop: 16, paddingTop: 13 },
  historyHintText: { color: colors.textMuted, fontSize: 12 },
  chevron: { color: colors.primary, fontSize: 24, lineHeight: 20 },
  demoNotice: { backgroundColor: colors.surfaceMuted, borderRadius: 16, padding: 16, marginTop: 18 },
  demoNoticeTitle: { color: colors.primaryDark, fontSize: 12, fontWeight: '800' },
  demoNoticeText: { color: colors.textMuted, fontSize: 11, lineHeight: 17, marginTop: 4 },
});

import { useState } from 'react';
import { Alert, Platform, Pressable, StyleSheet, Switch, Text, View, useWindowDimensions } from 'react-native';

import { DecimalField, EmptyState, LoadingState, PrimaryButton, Screen, SectionCard, TextField } from '@/components/ui';
import { formatDecimal } from '@/domain/decimal';
import { calculatePosition } from '@/domain/portfolio';
import { colors } from '@/theme';

import { useAddPurchase, useDeletePurchase, useSetStockAlert, useStock, useUpdatePurchase } from './queries';
import type { Purchase, SavePurchaseInput } from './types';

const today = () => new Date().toISOString().slice(0, 10);
const emptyPurchase = (): SavePurchaseInput => ({ price: '', quantity: '', purchasedAt: today() });

export function StockDetailScreen({ id }: { id: string }) {
  const { width } = useWindowDimensions();
  const compact = width < 620;
  const stockQuery = useStock(id);
  const addMutation = useAddPurchase(id);
  const updateMutation = useUpdatePurchase(id);
  const deleteMutation = useDeletePurchase(id);
  const alertMutation = useSetStockAlert(id);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState<SavePurchaseInput>(emptyPurchase);

  const resetForm = () => {
    setForm(emptyPurchase());
    setEditingId(null);
    setShowForm(false);
  };

  const startEdit = (purchase: Purchase) => {
    setForm({ price: purchase.price, quantity: purchase.quantity, purchasedAt: purchase.purchasedAt });
    setEditingId(purchase.id);
    setShowForm(true);
  };

  const savePurchase = () => {
    const options = { onSuccess: resetForm };
    if (editingId) updateMutation.mutate({ purchaseId: editingId, input: form }, options);
    else addMutation.mutate(form, options);
  };

  const confirmDelete = (purchaseId: string) => {
    if (Platform.OS === 'web') {
      if (window.confirm('매수 이력을 삭제할까요?\n삭제 후 기준 보유 정보와 남은 이력으로 평단가를 다시 계산합니다.')) {
        deleteMutation.mutate(purchaseId);
      }
      return;
    }
    Alert.alert('매수 이력을 삭제할까요?', '삭제 후 기준 보유 정보와 남은 이력으로 평단가를 다시 계산합니다.', [
      { text: '취소', style: 'cancel' },
      { text: '삭제', style: 'destructive', onPress: () => deleteMutation.mutate(purchaseId) },
    ]);
  };

  if (stockQuery.isLoading) return <LoadingState />;
  if (stockQuery.isError || !stockQuery.data) {
    return (
      <Screen>
        <SectionCard><EmptyState icon="!" title="종목을 찾을 수 없어요" description="목록으로 돌아가 다시 선택해 주세요." /></SectionCard>
      </Screen>
    );
  }

  const stock = stockQuery.data;
  const saving = addMutation.isPending || updateMutation.isPending;
  const mutationError = addMutation.error ?? updateMutation.error ?? deleteMutation.error;

  return (
    <Screen>
      <View style={styles.identity}>
        <View style={styles.avatar}><Text style={styles.avatarText}>{stock.name.slice(0, 1)}</Text></View>
        <View>
          <Text style={styles.name}>{stock.name}</Text>
          <Text style={styles.code}>{stock.market} · {stock.symbol}</Text>
        </View>
      </View>

      <SectionCard style={styles.summaryCard}>
        <Text style={styles.summaryLabel}>현재 평단가</Text>
        <View style={styles.averageRow}>
          <Text style={styles.average}>{formatDecimal(stock.summary.averagePrice)}</Text>
          <Text style={styles.averageUnit}>원</Text>
        </View>
        <View style={styles.summaryGrid}>
          <View style={styles.summaryItem}>
            <Text style={styles.summaryItemLabel}>총 보유 수량</Text>
            <Text style={styles.summaryItemValue}>{formatDecimal(stock.summary.quantity)}주</Text>
          </View>
          <View style={styles.summaryItem}>
            <Text style={styles.summaryItemLabel}>총 매수 원금</Text>
            <Text style={styles.summaryItemValue}>{formatDecimal(stock.summary.totalCost)}원</Text>
          </View>
        </View>
        <View style={styles.baselineBox}>
          <Text style={styles.baselineTitle}>기준 보유분</Text>
          <Text style={styles.baselineValue}>평단 {formatDecimal(stock.baseline.averagePrice)}원 · {formatDecimal(stock.baseline.quantity)}주</Text>
        </View>
      </SectionCard>

      <SectionCard style={styles.alertCard}>
        <View style={styles.alertCopy}>
          <Text style={styles.alertTitle}>평단가 도달 알림</Text>
          <Text style={styles.alertDescription}>실제 서버 연결 전까지 설정 상태만 체험할 수 있어요.</Text>
        </View>
        <Switch
          accessibilityLabel="평단가 도달 알림"
          disabled={alertMutation.isPending}
          onValueChange={(enabled) => alertMutation.mutate(enabled)}
          thumbColor="#FFFFFF"
          trackColor={{ false: '#CDD3D0', true: colors.primary }}
          value={stock.alertEnabled}
        />
      </SectionCard>

      <View style={styles.historyHeader}>
        <View>
          <Text style={styles.historyTitle}>매수 이력</Text>
          <Text style={styles.historyCount}>{stock.purchases.length}건의 추가 매수</Text>
        </View>
        <Pressable
          accessibilityRole="button"
          onPress={() => {
            if (showForm) resetForm();
            else setShowForm(true);
          }}
          style={styles.addButton}
        >
          <Text style={styles.addButtonText}>{showForm ? '취소' : '+ 매수 추가'}</Text>
        </Pressable>
      </View>

      {showForm ? (
        <SectionCard style={styles.purchaseForm}>
          <Text style={styles.formTitle}>{editingId ? '매수 이력 수정' : '새 매수 기록'}</Text>
          <View style={[styles.formRow, compact && styles.formColumn]}>
            <DecimalField label="매수가" suffix="원" value={form.price} onChangeText={(price) => setForm({ ...form, price })} />
            <DecimalField label="매수 수량" suffix="주" value={form.quantity} onChangeText={(quantity) => setForm({ ...form, quantity })} />
            <TextField label="매수일" placeholder="YYYY-MM-DD" value={form.purchasedAt} onChangeText={(purchasedAt) => setForm({ ...form, purchasedAt })} />
          </View>
          {(addMutation.error || updateMutation.error) ? <Text style={styles.error}>{(addMutation.error ?? updateMutation.error)?.message}</Text> : null}
          <PrimaryButton label={editingId ? '수정 내용 저장' : '매수 이력 저장'} onPress={savePurchase} loading={saving} disabled={!form.price || !form.quantity || !form.purchasedAt} />
        </SectionCard>
      ) : null}

      {stock.purchases.length === 0 ? (
        <SectionCard><EmptyState icon="₩" title="추가 매수 이력이 없어요" description="매수를 기록하면 기준 보유분과 합산해 평단가를 다시 계산합니다." /></SectionCard>
      ) : (
        <View style={styles.purchaseList}>
          {[...stock.purchases].reverse().map((purchase) => (
            <SectionCard key={purchase.id} style={styles.purchaseCard}>
              <View style={styles.purchaseMain}>
                <View>
                  <Text style={styles.purchaseDate}>{purchase.purchasedAt}</Text>
                  <Text style={styles.purchasePrice}>{formatDecimal(purchase.price)}원</Text>
                </View>
                <View style={styles.purchaseRight}>
                  <Text style={styles.purchaseQuantity}>{formatDecimal(purchase.quantity)}주</Text>
                  <Text style={styles.purchaseCost}>매수금 {formatDecimal(calculateCost(purchase.price, purchase.quantity))}원</Text>
                </View>
              </View>
              <View style={styles.purchaseActions}>
                <Pressable accessibilityRole="button" onPress={() => startEdit(purchase)} style={styles.actionButton}><Text style={styles.actionText}>수정</Text></Pressable>
                <Pressable accessibilityRole="button" disabled={deleteMutation.isPending} onPress={() => confirmDelete(purchase.id)} style={styles.actionButton}><Text style={styles.deleteText}>삭제</Text></Pressable>
              </View>
            </SectionCard>
          ))}
        </View>
      )}
      {mutationError && !showForm ? <Text style={styles.error}>{mutationError.message}</Text> : null}
      <Text style={styles.disclaimer}>기준 보유분은 과거 거래가 이미 반영된 값이며, 위 매수 이력에 다시 합산하지 않습니다.</Text>
    </Screen>
  );
}

function calculateCost(price: string, quantity: string): string {
  return calculatePosition({ averagePrice: '0', quantity: '0' }, [{ price, quantity }]).totalCost;
}

const styles = StyleSheet.create({
  identity: { flexDirection: 'row', alignItems: 'center', gap: 13, marginBottom: 18 },
  avatar: { width: 48, height: 48, borderRadius: 16, backgroundColor: colors.primarySoft, alignItems: 'center', justifyContent: 'center' },
  avatarText: { color: colors.primary, fontSize: 20, fontWeight: '900' },
  name: { color: colors.text, fontSize: 23, fontWeight: '900' },
  code: { color: colors.textMuted, fontSize: 12, marginTop: 4 },
  summaryCard: { backgroundColor: colors.primaryDark, borderColor: colors.primaryDark },
  summaryLabel: { color: '#BFD7CE', fontSize: 12, fontWeight: '700' },
  averageRow: { flexDirection: 'row', alignItems: 'baseline', gap: 6, marginTop: 5 },
  average: { color: '#FFFFFF', fontSize: 34, fontWeight: '900' },
  averageUnit: { color: '#D5E5DF', fontSize: 16, fontWeight: '700' },
  summaryGrid: { flexDirection: 'row', gap: 10, marginTop: 20 },
  summaryItem: { flex: 1, backgroundColor: '#1D5B49', borderRadius: 13, padding: 12 },
  summaryItemLabel: { color: '#BFD7CE', fontSize: 10 },
  summaryItemValue: { color: '#FFFFFF', fontSize: 14, fontWeight: '800', marginTop: 5 },
  baselineBox: { flexDirection: 'row', justifyContent: 'space-between', flexWrap: 'wrap', gap: 6, borderTopWidth: 1, borderTopColor: '#376759', marginTop: 16, paddingTop: 14 },
  baselineTitle: { color: '#BFD7CE', fontSize: 11 },
  baselineValue: { color: '#FFFFFF', fontSize: 11, fontWeight: '700' },
  alertCard: { flexDirection: 'row', alignItems: 'center', marginTop: 12, paddingVertical: 16 },
  alertCopy: { flex: 1, paddingRight: 12 },
  alertTitle: { color: colors.text, fontSize: 15, fontWeight: '800' },
  alertDescription: { color: colors.textMuted, fontSize: 11, lineHeight: 17, marginTop: 4 },
  historyHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: 25, marginBottom: 12 },
  historyTitle: { color: colors.text, fontSize: 20, fontWeight: '900' },
  historyCount: { color: colors.textMuted, fontSize: 11, marginTop: 3 },
  addButton: { backgroundColor: colors.primarySoft, borderRadius: 12, paddingHorizontal: 13, paddingVertical: 10 },
  addButtonText: { color: colors.primaryDark, fontSize: 12, fontWeight: '800' },
  purchaseForm: { gap: 15, marginBottom: 12 },
  formTitle: { color: colors.text, fontSize: 16, fontWeight: '800' },
  formRow: { flexDirection: 'row', gap: 10 },
  formColumn: { flexDirection: 'column' },
  purchaseList: { gap: 10 },
  purchaseCard: { padding: 16 },
  purchaseMain: { flexDirection: 'row', justifyContent: 'space-between', gap: 12 },
  purchaseDate: { color: colors.textMuted, fontSize: 11 },
  purchasePrice: { color: colors.text, fontSize: 18, fontWeight: '800', marginTop: 5 },
  purchaseRight: { alignItems: 'flex-end' },
  purchaseQuantity: { color: colors.text, fontSize: 14, fontWeight: '800' },
  purchaseCost: { color: colors.textMuted, fontSize: 10, marginTop: 6 },
  purchaseActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 7, borderTopWidth: 1, borderTopColor: colors.border, marginTop: 13, paddingTop: 10 },
  actionButton: { paddingHorizontal: 9, paddingVertical: 5 },
  actionText: { color: colors.primary, fontSize: 12, fontWeight: '700' },
  deleteText: { color: colors.danger, fontSize: 12, fontWeight: '700' },
  error: { color: colors.danger, fontSize: 12, marginTop: 10 },
  disclaimer: { color: colors.textMuted, fontSize: 11, lineHeight: 17, textAlign: 'center', marginTop: 18 },
});

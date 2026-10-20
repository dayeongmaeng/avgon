import { useMemo } from 'react';
import { StyleSheet, Text, View, useWindowDimensions } from 'react-native';

import { DecimalField, PrimaryButton, Screen, SectionCard } from '@/components/ui';
import { formatDecimal } from '@/domain/decimal';
import { calculateAdditionalPurchase } from '@/domain/portfolio';
import { colors } from '@/theme';

import { useCalculatorStore } from './store';

export function CalculatorScreen() {
  const { width } = useWindowDimensions();
  const compact = width < 620;
  const values = useCalculatorStore();

  const calculation = useMemo(() => {
    if (!values.currentAveragePrice || !values.currentQuantity || !values.purchasePrice || !values.purchaseQuantity) {
      return { result: null, error: null };
    }
    try {
      return {
        result: calculateAdditionalPurchase(
          { averagePrice: values.currentAveragePrice, quantity: values.currentQuantity },
          { price: values.purchasePrice, quantity: values.purchaseQuantity },
        ),
        error: null,
      };
    } catch (error) {
      return { result: null, error: error instanceof Error ? error.message : '입력값을 확인해 주세요.' };
    }
  }, [values.currentAveragePrice, values.currentQuantity, values.purchasePrice, values.purchaseQuantity]);

  const currentCost = useMemo(() => {
    if (!values.currentAveragePrice || !values.currentQuantity) return null;
    try {
      return calculateAdditionalPurchase(
        { averagePrice: '0', quantity: '0' },
        { price: values.currentAveragePrice, quantity: values.currentQuantity },
      ).totalCost;
    } catch {
      return null;
    }
  }, [values.currentAveragePrice, values.currentQuantity]);

  return (
    <Screen>
      <View style={styles.header}>
        <View style={styles.eyebrow}><Text style={styles.eyebrowText}>AVERAGE PRICE</Text></View>
        <Text style={styles.title}>물타기 전, 새 평단가를{compact ? '\n' : ' '}미리 확인하세요.</Text>
        <Text style={styles.subtitle}>현재 보유분과 추가 매수 계획을 입력하면 즉시 계산해 드려요.</Text>
      </View>

      <SectionCard style={styles.formCard}>
        <View style={styles.sectionHeading}>
          <View style={styles.step}><Text style={styles.stepText}>1</Text></View>
          <View>
            <Text style={styles.sectionTitle}>현재 보유</Text>
            <Text style={styles.sectionDescription}>지금 보유 중인 종목 정보를 입력하세요.</Text>
          </View>
        </View>
        <View style={[styles.fieldRow, compact && styles.fieldColumn]}>
          <DecimalField label="현재 평단가" suffix="원" value={values.currentAveragePrice} onChangeText={(value) => values.setField('currentAveragePrice', value)} />
          <DecimalField label="보유 수량" suffix="주" value={values.currentQuantity} onChangeText={(value) => values.setField('currentQuantity', value)} />
        </View>
        {currentCost ? (
          <View style={styles.inlineHint}>
            <Text style={styles.inlineHintLabel}>현재 매수 원금</Text>
            <Text style={styles.inlineHintValue}>{formatDecimal(currentCost)}원</Text>
          </View>
        ) : null}

        <View style={styles.divider} />
        <View style={styles.sectionHeading}>
          <View style={[styles.step, styles.stepAccent]}><Text style={styles.stepText}>2</Text></View>
          <View>
            <Text style={styles.sectionTitle}>추가 매수</Text>
            <Text style={styles.sectionDescription}>매수하려는 가격과 수량을 입력하세요.</Text>
          </View>
        </View>
        <View style={[styles.fieldRow, compact && styles.fieldColumn]}>
          <DecimalField label="추가 매수가" suffix="원" value={values.purchasePrice} onChangeText={(value) => values.setField('purchasePrice', value)} />
          <DecimalField label="추가 수량" suffix="주" value={values.purchaseQuantity} onChangeText={(value) => values.setField('purchaseQuantity', value)} />
        </View>
        {calculation.error ? <Text style={styles.error}>{calculation.error}</Text> : null}
      </SectionCard>

      <SectionCard style={[styles.resultCard, !calculation.result && styles.resultPending]}>
        {calculation.result ? (
          <>
            <Text style={styles.resultLabel}>변경 예상 평단가</Text>
            <View style={styles.resultPriceRow}>
              <Text style={styles.resultPrice}>{formatDecimal(calculation.result.averagePrice)}</Text>
              <Text style={styles.resultUnit}>원</Text>
            </View>
            <View style={styles.resultStats}>
              <View style={styles.stat}>
                <Text style={styles.statLabel}>총 보유 수량</Text>
                <Text style={styles.statValue}>{formatDecimal(calculation.result.quantity)}주</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.stat}>
                <Text style={styles.statLabel}>총 매수 원금</Text>
                <Text style={styles.statValue}>{formatDecimal(calculation.result.totalCost)}원</Text>
              </View>
            </View>
          </>
        ) : (
          <View style={styles.pendingContent}>
            <Text style={styles.pendingIcon}>∿</Text>
            <View>
              <Text style={styles.pendingTitle}>숫자를 입력하면 바로 계산해요</Text>
              <Text style={styles.pendingText}>모든 입력값은 0보다 큰 숫자여야 합니다.</Text>
            </View>
          </View>
        )}
      </SectionCard>

      <PrimaryButton label="입력값 초기화" onPress={values.reset} variant="ghost" />
      <Text style={styles.note}>계산 결과는 수수료·세금·환율을 포함하지 않은 단순 평균 매수가입니다.</Text>
    </Screen>
  );
}

const styles = StyleSheet.create({
  header: { marginBottom: 22 },
  eyebrow: { alignSelf: 'flex-start', backgroundColor: colors.primarySoft, borderRadius: 20, paddingHorizontal: 11, paddingVertical: 6, marginBottom: 13 },
  eyebrowText: { color: colors.primary, fontSize: 11, fontWeight: '900', letterSpacing: 1.2 },
  title: { color: colors.text, fontSize: 30, fontWeight: '900', lineHeight: 39, letterSpacing: -0.8 },
  subtitle: { color: colors.textMuted, fontSize: 15, lineHeight: 23, marginTop: 9 },
  formCard: { gap: 18 },
  sectionHeading: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  step: { width: 34, height: 34, borderRadius: 17, backgroundColor: colors.primary, alignItems: 'center', justifyContent: 'center' },
  stepAccent: { backgroundColor: colors.accent },
  stepText: { color: '#FFFFFF', fontWeight: '900' },
  sectionTitle: { color: colors.text, fontSize: 17, fontWeight: '800' },
  sectionDescription: { color: colors.textMuted, fontSize: 12, marginTop: 2 },
  fieldRow: { flexDirection: 'row', gap: 12 },
  fieldColumn: { flexDirection: 'column' },
  inlineHint: { flexDirection: 'row', justifyContent: 'space-between', paddingHorizontal: 14, paddingVertical: 11, backgroundColor: colors.surfaceMuted, borderRadius: 12 },
  inlineHintLabel: { color: colors.textMuted, fontSize: 13 },
  inlineHintValue: { color: colors.primaryDark, fontSize: 13, fontWeight: '800' },
  divider: { height: 1, backgroundColor: colors.border, marginVertical: 2 },
  error: { color: colors.danger, fontSize: 13 },
  resultCard: { marginTop: 16, backgroundColor: colors.primaryDark, borderColor: colors.primaryDark },
  resultPending: { backgroundColor: colors.surface, borderColor: colors.border },
  resultLabel: { color: '#BFD7CE', fontSize: 13, fontWeight: '700' },
  resultPriceRow: { flexDirection: 'row', alignItems: 'baseline', gap: 6, marginTop: 7 },
  resultPrice: { color: '#FFFFFF', fontSize: 36, fontWeight: '900', letterSpacing: -0.8 },
  resultUnit: { color: '#D5E5DF', fontSize: 17, fontWeight: '700' },
  resultStats: { flexDirection: 'row', marginTop: 20, paddingTop: 17, borderTopWidth: 1, borderTopColor: '#376759' },
  stat: { flex: 1, gap: 5 },
  statDivider: { width: 1, backgroundColor: '#376759', marginHorizontal: 16 },
  statLabel: { color: '#BFD7CE', fontSize: 12 },
  statValue: { color: '#FFFFFF', fontSize: 15, fontWeight: '800' },
  pendingContent: { flexDirection: 'row', alignItems: 'center', gap: 14 },
  pendingIcon: { color: colors.primary, fontSize: 35, fontWeight: '300' },
  pendingTitle: { color: colors.text, fontSize: 16, fontWeight: '800' },
  pendingText: { color: colors.textMuted, fontSize: 12, marginTop: 4 },
  note: { color: colors.textMuted, fontSize: 11, lineHeight: 17, textAlign: 'center', marginTop: 4 },
});

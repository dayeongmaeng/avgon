import { StyleSheet, Text } from 'react-native';
import { Tabs } from 'expo-router';

import { colors } from '@/theme';

function TabIcon({ symbol, focused }: { symbol: string; focused: boolean }) {
  return <Text style={[styles.icon, focused && styles.iconFocused]}>{symbol}</Text>;
}

export default function TabLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textMuted,
        tabBarLabelStyle: styles.label,
        tabBarStyle: styles.tabBar,
      }}
    >
      <Tabs.Screen name="index" options={{ title: '물타기 계산기', tabBarIcon: ({ focused }) => <TabIcon symbol="∿" focused={focused} /> }} />
      <Tabs.Screen name="stocks" options={{ title: '내 종목', tabBarIcon: ({ focused }) => <TabIcon symbol="▥" focused={focused} /> }} />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  tabBar: { height: 72, paddingTop: 7, paddingBottom: 9, backgroundColor: '#FFFFFF', borderTopColor: colors.border },
  label: { fontSize: 11, fontWeight: '800' },
  icon: { color: colors.textMuted, fontSize: 21, fontWeight: '700' },
  iconFocused: { color: colors.primary },
});

export const colors = {
  background: '#F5F7F3',
  surface: '#FFFFFF',
  surfaceMuted: '#EDF3EF',
  primary: '#176B52',
  primaryDark: '#0E4938',
  primarySoft: '#DCECE5',
  accent: '#FF8A4C',
  text: '#17221E',
  textMuted: '#66736D',
  border: '#DCE3DE',
  danger: '#C2413A',
  dangerSoft: '#FCEAE8',
  warning: '#9B6812',
} as const;

export const shadows = {
  card: {
    shadowColor: '#10271F',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.06,
    shadowRadius: 16,
    elevation: 2,
  },
} as const;

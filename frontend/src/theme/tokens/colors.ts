export interface ColorTokens {
  background: string;
  surface: string;
  surfaceVariant: string;
  outline: string;
  divider: string;

  textPrimary: string;
  textSecondary: string;
  textMuted: string;

  primary: string;
  onPrimary: string;

  accent: string;
  onAccent: string;

  success: string;
  onSuccess: string;

  warning: string;
  onWarning: string;

  error: string;
  onError: string;

  info: string;
  onInfo: string;
}

export const lightColors: ColorTokens = {
  background: '#FBF9F4',
  surface: '#FFFFFF',
  surfaceVariant: '#F4EFE3',
  outline: '#D8CBAE',
  divider: '#E9E1CD',

  textPrimary: '#131A26',
  textSecondary: '#3B4658',
  textMuted: '#64708A',

  primary: '#1B2A4A',
  onPrimary: '#FBF9F4',

  accent: '#D98E4A',
  onAccent: '#131A26',

  success: '#4C7A5E',
  onSuccess: '#FFFFFF',

  warning: '#C1783A',
  onWarning: '#131A26',

  error: '#B5533F',
  onError: '#FFFFFF',

  info: '#3F6E8C',
  onInfo: '#FFFFFF',
};

export const darkColors: ColorTokens = {
  background: '#0F1526',
  surface: '#131D33',
  surfaceVariant: '#1B2A4A',
  outline: '#24365C',
  divider: '#263A63',

  textPrimary: '#F4EFE3',
  textSecondary: '#B8AF9C',
  textMuted: '#7C8398',

  primary: '#24365C',
  onPrimary: '#F4EFE3',

  accent: '#E4A66E',
  onAccent: '#131A26',

  success: '#6FA080',
  onSuccess: '#0F1526',

  warning: '#D98E4A',
  onWarning: '#131A26',

  error: '#D97862',
  onError: '#0F1526',

  info: '#6B9DBB',
  onInfo: '#0F1526',
};

export const colors = {
  light: lightColors,
  dark: darkColors,
} as const;

export type ThemeMode = keyof typeof colors;
export type ColorToken = keyof ColorTokens;

import { createContext } from 'react';

export const ColorModeContext = createContext({
  themeMode: 'system',
  setThemeMode: () => {}
});

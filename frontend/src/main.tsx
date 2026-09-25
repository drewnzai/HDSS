import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import "./theme/tokens/tokens.css"
import App from './App.tsx'
import { BrowserRouter } from 'react-router-dom'
import { store } from './redux/store.ts'
import { Provider } from 'react-redux'
import { ThemeProvider } from './theme/ThemeProvider.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provider store={store}>
      <ThemeProvider>
      <BrowserRouter>
        <App />
      </BrowserRouter>
      </ThemeProvider>
    </Provider>
  </StrictMode>,
)

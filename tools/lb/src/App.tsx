import React from 'react';
import './App.css';

import { createMuiTheme, ThemeProvider } from '@material-ui/core/styles';
import { orange } from '@material-ui/core/colors';

import Container from './Container';
import Constants from './Constants';

declare module '@material-ui/core/styles/createMuiTheme' {
  interface Theme {
    status: {
      danger: string;
    };
  }
  // allow configuration using `createMuiTheme`
  interface ThemeOptions {
    status?: {
      danger?: string;
    };
  }
}

const theme = createMuiTheme({
  palette: {
    primary: Constants.COLOR_PRIMARY,
    secondary: Constants.COLOR_SECONDARY,
  },
  status: {
    danger: orange[500],
  },
});

function App() {
  return (
    <div className="App">
      <ThemeProvider theme={theme}>
        <Container />
      </ThemeProvider>
    </div>
  );
}

export default App;

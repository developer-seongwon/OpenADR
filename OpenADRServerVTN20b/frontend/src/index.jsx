import React from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App';
import { unregister as unregisterServiceWorker } from './registerServiceWorker';

import { createBrowserRouter, RouterProvider } from 'react-router';
import { Provider } from 'react-redux';
import { ThemeProvider } from '@mui/material/styles';
import theme from './theme';

import configureStore, { publicPath } from './store/configureStore';
import { setRouter } from './store/history';
import * as types from './constants/actionTypes';

const store = configureStore();

// 예전 구조는 <ConnectedRouter> 안에 <App> 을 두고 App 안의 <Switch> 가 화면을 골랐다.
// 라우터 7 에서는 라우터 객체를 먼저 만들고, App 을 모든 경로(*)에 붙인 뒤
// App 안의 <Routes> 가 화면을 고르게 했다. 드로어와 앱바 같은 틀은 App 이 그대로 그린다
const router = createBrowserRouter(
  [ { path: '*', element: <App /> } ],
  // /testvtn/ 끝의 슬래시는 뺀다. 라우터가 경로를 이어 붙일 때 // 가 생긴다
  { basename: ( publicPath || '/' ).replace( /\/$/, '' ) || '/' }
);
setRouter( router );

// 리듀서 11개가 LOCATION_CHANGE 를 받아서 화면을 옮길 때 목록 상태를 비운다.
// 예전에는 connected-react-router 가 이 액션을 보냈다. 라우터를 구독해서 같은 모양으로 보낸다.
// 이 구독은 RouterProvider 보다 먼저 걸려 있어서, 새 화면이 마운트되며 보내는 조회 액션보다
// 초기화가 항상 먼저 들어간다
function dispatchLocationChange( location, action, isFirstRendering ) {
  store.dispatch( {
    type: types.LOCATION_CHANGE,
    payload: { location, action, isFirstRendering },
  } );
}
let lastLocationKey = router.state.location.key;
dispatchLocationChange( router.state.location, router.state.historyAction, true );
router.subscribe( ( state ) => {
  // 구독 콜백은 주소 말고 다른 상태가 바뀔 때도 불린다. 이동이 끝나고 주소가 실제로 바뀐 경우만 보낸다
  if ( state.navigation.state === 'idle' && state.location.key !== lastLocationKey ) {
    lastLocationKey = state.location.key;
    dispatchLocationChange( state.location, state.historyAction, false );
  }
} );

// React 18 부터 ReactDOM.render 대신 createRoot 를 쓴다.
// StrictMode 는 아직 켜지 않았다. 개발 모드에서 생성자와 렌더를 두 번씩 불러 부작용을 드러내는데,
// 클래스 화면 64개 중 생성자에서 props 로 state 를 직접 고치는 곳들이 있어 따로 정리한 뒤에 켠다.
// ThemeProvider 는 MUI 3 기본 모양을 되살리는 테마를 건다(theme.js 참고)
createRoot( document.getElementById( 'root' ) ).render(
  <Provider store={ store }>
    <ThemeProvider theme={ theme }>
      <RouterProvider router={ router } />
    </ThemeProvider>
  </Provider>
);

// CRA 때 깔린 서비스 워커를 걷어낸다. 자세한 건 registerServiceWorker.js 의 unregister 참고
unregisterServiceWorker();

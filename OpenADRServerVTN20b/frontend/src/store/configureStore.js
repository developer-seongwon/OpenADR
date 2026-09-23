// redux 5 는 createStore 를 legacy_createStore 로 이름을 바꿔 두었다(동작은 같다).
// Redux Toolkit 의 configureStore 로 옮기라는 뜻인데 그건 리듀서를 다 고치는 일이라 여기선 이름만 맞춘다
import { legacy_createStore as createStore, compose, applyMiddleware } from 'redux';
import reduxImmutableStateInvariant from 'redux-immutable-state-invariant';
// redux-thunk 3 부터 default export 가 없다
import { thunk } from 'redux-thunk';
import swaggerMiddleware from './swaggerMiddleware'
import swagger from 'swagger-client'
import rootReducer from '../reducers';
// 라우터 basename. CRA 때는 pom 이 REACT_APP_BASENAME 으로 넘겼는데
// Vite 는 vite.config.js 의 base 를 BASE_URL 로 준다. 값은 똑같이 /testvtn/ 이다.
// 예전에 있던 console.log(process.env) 는 환경변수를 통째로 찍어서 지웠다
export const publicPath = import.meta.env.BASE_URL || null;

// connected-react-router 는 걷어냈다. history.push 를 부르는 곳이 많아서
// 같은 이름으로 라우터 어댑터를 내보낸다. 자세한 건 ./history.js
export { history } from './history';

// springfox 를 springdoc 으로 바꾸면서 문서 경로가 v2 에서 v3 로 옮겨졌다.
// 기본값도 vtn.oadr.com 으로 박혀 있어서 localhost 로 열면 못 찾았다.
// 지금은 화면을 띄운 그 서버를 그대로 본다.
var swaggerUrl = import.meta.env.REACT_APP_SWAGGER_URL
  || (window.location.origin + (publicPath || '/') + 'v3/api-docs').replace(/\/\//g, '/').replace(':/', '://');
export var config = {
//		vtnSwaggerUrl: 'https://192.168.1.11:8181/testvtn/v2/api-docs',
//  vtnSwaggerUrl: 'https://192.168.10.42:8181/testvtn/v2/api-docs',
  vtnSwaggerUrl: swaggerUrl,
  isConnectionPending: true,
  isConnected: false
};

function configureSwaggerMiddleware() {

  swagger.http.withCredentials = true
  const swaggerOpts = {
    url: config.vtnSwaggerUrl
  };
  return swaggerMiddleware( swaggerOpts, config );
}

// connected-react-router 의 routerMiddleware 는 dispatch(push(...)) 액션을 처리했는데
// 그 액션을 쓰는 곳이 없었다. connectRouter 가 붙이던 state.router 도 읽는 곳이 없어서 같이 뺐다.
// 리듀서들이 듣는 LOCATION_CHANGE 는 index.jsx 가 라우터를 구독해서 계속 보내 준다
function configureStoreProd( initialState ) {
  const middlewares = [
    thunk,
    configureSwaggerMiddleware(config),
  ];

  return createStore(
    rootReducer,
    initialState,
    compose( applyMiddleware( ...middlewares ) )
  );
}

function configureStoreDev( initialState ) {
  const middlewares = [
    reduxImmutableStateInvariant(),
    thunk,
    configureSwaggerMiddleware(),
  ];

  const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose; // add support for Redux dev tools
  const store = createStore(
    rootReducer,
    initialState,
    composeEnhancers( applyMiddleware( ...middlewares ) )
  );

  // 웹팩의 module.hot 대신 Vite 의 import.meta.hot 을 쓴다.
  // Vite 는 require 가 없어서 바뀐 모듈을 콜백 인자로 받는다
  if ( import.meta.hot ) {
    import.meta.hot.accept( '../reducers', ( nextModule ) => {
      if ( nextModule ) {
        store.replaceReducer( nextModule.default );
      }
    } );
  }

  return store;
}

const configureStore = import.meta.env.PROD ? configureStoreProd : configureStoreDev;

export default configureStore;



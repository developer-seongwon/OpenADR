import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// react-scripts(CRA) 3.2 를 Vite 로 바꾸면서 CRA 가 알아서 해 주던 걸 여기 적는다.
//
// 확장자가 .mjs 인 이유: package.json 에 "type": "module" 을 넣으면 설정 파일은 ESM 으로 읽히지만
// src 까지 Node ESM 취급을 받아서, CommonJS 패키지를 default import 하면 exports.default 가 아니라
// module.exports 객체가 통째로 들어온다. 그래서 첫 렌더부터 React error #130 으로 죽었다.
// type 은 빼고 설정 파일만 .mjs 로 ESM 이라고 알려 준다
export default defineConfig( {
  // JSX 는 react/jsx-runtime 을 쓰는 새 변환(automatic)으로 바꾼다.
  // React 16.10 때는 jsx-runtime 이 없어서 classic 으로 두었는데 18 에서는 있고, 19 는 새 변환을 요구한다
  plugins: [ react() ],

  // 스프링의 server.servlet.context-path 와 같아야 한다.
  // CRA 때는 pom 에서 PUBLIC_URL 환경변수로 넘겼다
  base: '/testvtn/',

  // CRA 는 REACT_APP_ 로 시작하는 환경변수만 번들에 넣었다. 이름을 그대로 쓰려고 접두어를 맞춘다.
  // 코드에서는 process.env.X 대신 import.meta.env.X 로 읽는다
  envPrefix: 'REACT_APP_',

  // 웹팩 4 는 Node 전역 global 을 브라우저용으로 알아서 채워 줬는데 Vite 는 안 채운다.
  // MUI 3 의 스타일 엔진 JSS 9 가 모듈을 읽자마자 global.CSS 를 보므로
  // 이게 없으면 "global is not defined" 로 첫 화면부터 하얗게 죽는다
  define: {
    global: 'globalThis',
  },

  build: {
    // 메이븐 antrun 이 frontend/build 를 src/main/resources/public 으로 복사한다
    outDir: 'build',
    // Vite 기본값은 assets/ 인데, HttpSecurityConfig 가 인증 없이 열어 주는 경로가 /static/** 이다.
    // CRA 때와 같은 자리에 두면 자바 쪽은 손대지 않아도 된다
    assetsDir: 'static',
    // 여러 화면이 같이 쓰는 큰 라이브러리를 따로 떼어 둔다. 앱 코드만 바뀌었을 때 브라우저가 이 조각들은
    // 캐시에서 쓰고, 화면 조각(App.jsx 의 lazy)끼리 같은 라이브러리를 중복으로 싣지 않는다.
    // 어느 그룹에도 안 걸린 것은 rolldown 이 알아서 나눈다. 높은 priority 가 먼저 가져간다
    // swagger-client 는 라이브러리 하나가 약 545KB 라 더 쪼갤 수 없다. 경고 기준을 그 위로 둔다
    chunkSizeWarningLimit: 600,
    rolldownOptions: {
      output: {
        codeSplitting: {
          groups: [
            { name: 'react', test: /node_modules[\\/](react|react-dom|scheduler|react-router|react-redux|redux|redux-thunk)[\\/]/, priority: 3 },
            { name: 'mui', test: /node_modules[\\/](@mui|@emotion|tss-react|@popperjs|react-transition-group)[\\/]/, priority: 2 },
            { name: 'swagger', test: /node_modules[\\/]swagger-client[\\/]/, priority: 1 },
          ],
        },
      },
    },
  },
} );

// 서비스 워커 걷어내기만 남았다.
// CRA 때 쓰던 등록 코드(register, registerValidSW, checkValidServiceWorker)는 Vite 로 바꾸면서
// 부르는 곳이 없어져 2026-10-02 에 지웠다. index.jsx 가 앱이 뜰 때 unregister 를 부른다

// CRA 빌드를 한 번이라도 열어 본 브라우저에는 캐시 우선 워커가 깔려 있다.
// 그대로 두면 새 번들 대신 캐시된 옛 화면이 뜨므로 앱이 뜰 때마다 걷어낸다.
// 옛 워커가 index.html 까지 캐시해서 이 코드조차 못 받는 경우는
// public/service-worker.js 가 스스로를 해제하는 쪽에서 처리한다
export function unregister() {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.ready.then(registration => {
      registration.unregister();
    });
  }
}

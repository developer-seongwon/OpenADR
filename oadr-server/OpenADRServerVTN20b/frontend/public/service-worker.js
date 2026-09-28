// 스스로를 해제하는 서비스 워커.
//
// CRA 빌드는 이 경로에 캐시 우선 워커를 두고 index.html 까지 캐시했다.
// 그 워커가 깔린 브라우저는 새 index.html 을 받지 못하니 앱 코드로는 걷어낼 수가 없다.
// 브라우저는 페이지를 열 때마다 이 파일이 바뀌었는지 확인하므로,
// 같은 경로에 이 파일을 두면 옛 워커가 이걸로 교체된다.
// 교체되면 캐시를 비우고 자신을 해제한 뒤 열린 탭을 새로고침한다.
self.addEventListener( 'install', () => self.skipWaiting() );

self.addEventListener( 'activate', ( event ) => {
  event.waitUntil( ( async () => {
    const keys = await caches.keys();
    await Promise.all( keys.map( ( key ) => caches.delete( key ) ) );
    await self.registration.unregister();
    const clients = await self.clients.matchAll( { type: 'window' } );
    clients.forEach( ( client ) => client.navigate( client.url ) );
  } )() );
} );

// 컴포넌트와 액션 곳곳(48곳)이 스토어에서 받은 history 객체로 history.push 를 부른다.
// 예전에는 history 4 의 createBrowserHistory({ basename }) 였고 connected-react-router 가
// 그 위에서 라우팅과 리덕스를 이어 줬다.
//
// react-router 7 은 history 패키지를 쓰지 않고 라우터가 직접 주소를 관리한다.
// history 5 로 바꿔 끼우는 방법도 있지만 history 5 에는 basename 이 없어서 push('/ven') 가
// /testvtn 을 빼고 /ven 으로 가 버린다. 그래서 같은 모양(push, replace, goBack, location)의
// 얇은 어댑터를 두고 안에서 router.navigate 를 부른다. navigate 는 basename 을 알아서 붙인다.
//
// 라우터는 index.jsx 에서 만들어지고 setRouter 로 여기에 꽂힌다.
// 모듈을 읽는 시점에는 아직 라우터가 없어서 호출할 때 찾는다.

let router = null;

export function setRouter( r ) {
  router = r;
}

function requireRouter() {
  if ( router == null ) {
    throw new Error( 'router 가 아직 준비되지 않았다. index.jsx 의 setRouter 호출 순서를 확인해라' );
  }
  return router;
}

// history 4 의 push( path, state ) 와 push( { pathname, state } ) 두 모양을 모두 받는다
function toNavigateArgs( to, state ) {
  if ( to != null && typeof to === 'object' ) {
    const { state: objectState, ...path } = to;
    return [ path, { state: objectState } ];
  }
  return [ to, state === undefined ? undefined : { state } ];
}

export const history = {
  push( to, state ) {
    const [ target, opts ] = toNavigateArgs( to, state );
    requireRouter().navigate( target, opts );
  },

  replace( to, state ) {
    const [ target, opts ] = toNavigateArgs( to, state );
    requireRouter().navigate( target, { ...opts, replace: true } );
  },

  goBack() {
    requireRouter().navigate( -1 );
  },

  // 라우터가 들고 있는 현재 위치. pathname 에 basename(/testvtn) 은 빠져 있다
  get location() {
    return requireRouter().state.location;
  },
};

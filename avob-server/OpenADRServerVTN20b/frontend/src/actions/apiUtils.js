export function swaggerAction (actionType, getAction, getPayload, getNext) {
  return (dispatch, getState) => {
    dispatch( {
      type: actionType,
      swagger: function ( api ) {
          getAction(api)
          .then( (resp) => {
            var msg = {
              type: actionType + "_SUCCESS"
            }
            if(getPayload) {
              var payload = getPayload(resp);
              if(payload) {
                msg.payload = payload;
              }
            }
            console.log(resp.headers)
            if(resp.headers && resp.headers["x-total-count"] && resp.headers["x-total-page"]) {
            	msg.total = parseInt(resp.headers["x-total-count"]);
            	msg.totalPage = parseInt(resp.headers["x-total-page"]);
            }
            dispatch(msg);
            if(getNext) {
              getNext(dispatch, getState);
            }
          } )
          .catch( err => {
            dispatch( {
              type: actionType + "_ERROR",
              payload: err
            } );
          } )
      }
    } )
  }
}

export var jsonResponseContentType = {
      responseContentType: 'application/json'
}

export var multipartResponseContentType = {
      responseContentType: 'multipart/form-data'
}

export var saveData = ( function () {
  var a = document.createElement( 'a' );
  document.body.appendChild( a );
  a.style = 'display: none';
  return function ( data, fileName ) {
     var url = window.URL.createObjectURL( data );
      a.href = url;
      a.download = fileName;
      a.click();
      window.URL.revokeObjectURL( url );
    
  };
}());

// swagger-client 오류에서 응답 코드와 이유를 꺼낸다. 화면에 오류를 보여 줄 때 쓴다.
// 스프링 기본 오류 응답은 message 없이 error(Bad Request) 만 준다. 자세한 이유는 서버 로그에 남는다
export var errorOf = ( err ) => {
  var response = err && err.response;
  var body = response && response.body;
  return {
    status: ( response && response.status ) || ( err && err.status ) || null,
    detail: ( body && ( body.message || body.detail || body.error ) ) || ( err && err.message ) || '',
  };
}

export var parseJsonData = (data) => {
  return JSON.parse( data.data );
}
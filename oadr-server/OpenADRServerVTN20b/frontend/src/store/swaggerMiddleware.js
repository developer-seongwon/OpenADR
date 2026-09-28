import Swagger from 'swagger-client';
import * as types from '../constants/actionTypes';

import { config, history } from './configureStore';


var swaggerClient = null;

var responseInterceptor=  (res) => {
     console.log(res);
  }


// springfox(Swagger 2) 시절엔 body 파라미터에도 이름이 있어서
// searchVenUsingPOST({ filters, page, size }) 처럼 한 객체에 다 넣어 넘기면 됐다.
// springdoc(OpenAPI 3) 으로 바뀌면서 body 는 이름 없는 requestBody 가 됐고,
// swagger-client 는 스펙에 선언되지 않은 키를 전부 버린다. 그래서 body 가 비어서
// 서버가 400 (Required request body is missing) 을 돌려준다.
// 호출부 수십 군데를 다 고치는 대신, 여기서 선언 안 된 키 하나를 requestBody 로 옮겨준다.
var adaptOperationsToOas3 = function ( client ) {
  var spec = client && client.spec;
  if ( !spec || !spec.paths || !client.apis ) {
    return client;
  }

  // operationId -> { declared: 선언된 파라미터 이름들, hasBody: requestBody 유무 }
  var meta = {};
  Object.keys( spec.paths ).forEach( function ( path ) {
    var pathItem = spec.paths[ path ];
    Object.keys( pathItem ).forEach( function ( method ) {
      var op = pathItem[ method ];
      if ( !op || !op.operationId ) {
        return;
      }
      var declared = {};
      ( pathItem.parameters || [] ).concat( op.parameters || [] ).forEach( function ( p ) {
        if ( p && p.name ) {
          declared[ p.name ] = true;
        }
      } );
      meta[ op.operationId ] = { declared: declared, hasBody: op.requestBody != null };
    } );
  } );

  Object.keys( client.apis ).forEach( function ( tag ) {
    var api = client.apis[ tag ];
    Object.keys( api ).forEach( function ( operationId ) {
      var original = api[ operationId ];
      var info = meta[ operationId ];
      if ( typeof original !== 'function' || !info || !info.hasBody ) {
        return;
      }
      api[ operationId ] = function ( params, opts ) {
        // swagger-client 에서 requestBody 는 첫번째(parameters) 가 아니라
        // 두번째 인자(opts) 로 넘겨야 실제 body 로 직렬화된다
        if ( params && typeof params === 'object' && ( !opts || opts.requestBody === undefined ) ) {
          var declaredParams = {};
          var bodyKeys = [];
          Object.keys( params ).forEach( function ( key ) {
            if ( info.declared[ key ] ) {
              declaredParams[ key ] = params[ key ];
            } else {
              bodyKeys.push( key );
            }
          } );
          // 선언 안 된 키가 정확히 하나일 때만 body 로 본다 (애매하면 건드리지 않는다)
          if ( bodyKeys.length === 1 ) {
            opts = Object.assign( {}, opts, { requestBody: params[ bodyKeys[ 0 ] ] } );
            params = declaredParams;
          }
        }
        return original( params, opts );
      };
    } );
  } );

  return client;
}

var loadClient = function ( url, dispatch) {
  return new Promise( (resolve, reject) => {
    if ( swaggerClient != null ) {
      resolve( swaggerClient );

    } else {
      var params = {url:url, responseInterceptor: responseInterceptor
        , requestInterceptor: req => {
          if(config.username != null && config.password != null) {
            var encoded = btoa(config.username +":"+config.password);
            req.headers["Authorization"] = "Basic "+ encoded
          }
        }};

      
      return Swagger( params ).then( client => {
        config.isConnected = true
        config.isConnectionPending = false
        // dispatch({
        //   type: types.LOGIN_USER_SUCCESS
        // })
        swaggerClient = adaptOperationsToOas3( client );
        resolve( swaggerClient );
      } ).catch(err => {
        config.isConnected = false
        config.isConnectionPending = false
        dispatch({
          type: types.LOGIN_USER_ERROR,
          payload: err
        });
        history.push("/login")
        swaggerClient = null;
      });
    }
  } );
}

export default function swaggerMiddleware( opts ) {
  return store => next => action => {
    
    console.log( action.type )
    if(action.type.indexOf("_ERROR") > -1){
      console.log(action.payload)
    }

    if ( !action.swagger ) {
      return next( action );
    }

    loadClient( opts.url, store.dispatch, config )
      .then( (client) => {
        action.swagger( client );
      } )


  }
}

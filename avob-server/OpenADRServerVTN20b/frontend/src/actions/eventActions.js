import * as types from '../constants/actionTypes';
import { history } from '../store/configureStore';

import { swaggerAction, jsonResponseContentType, parseJsonData } from './apiUtils';

// 목록을 다시 읽는다.
//
// 예전에는 listUsingGET 을 불렀는데 서버에 그런 엔드포인트가 없다.
// GET /DemandResponseEvent/ 는 405 다. 목록은 POST /search 하나뿐이고,
// 리듀서에서 LOAD_EVENT_SUCCESS 와 SEARCH_EVENT_SUCCESS 가 하는 일도 똑같다.
// 그래서 필터 없는 search 로 바꿨다.
export const loadEvent = (start, end) => {
  var params = { filters: [], start, end };
  return swaggerAction(types.LOAD_EVENT, 
    (api) => {
      return api.apis[ 'demand-response-controller' ].searchUsingPOST(params, jsonResponseContentType);
    }, 
    parseJsonData
  );
}

export const searchEvent = (filters, start, end, page, size) => {
  var params = {filters, start, end, page, size};
  return swaggerAction(types.SEARCH_EVENT, 
    (api) => {
      return api.apis[ 'demand-response-controller' ].searchUsingPOST(params, jsonResponseContentType);
    }, 
    parseJsonData
  );
}

// 보내기 전에 CREATE_EVENT_PENDING 을 먼저 보낸다. 화면이 버튼을 잠그고 지난 오류를 지운다
export const createEvent = (dto) => {
  var params = { event: dto }
  var action = swaggerAction(types.CREATE_EVENT, 
    (api) => {
      // 호출 자체가 던져도(오퍼레이션 이름이 없을 때 등) CREATE_EVENT_ERROR 로 가게 Promise 로 감싼다. 안 그러면 버튼이 잠긴 채 남는다
      return Promise.resolve().then(() => api.apis[ 'demand-response-controller' ].createUsingPOST(params, jsonResponseContentType));
    }, 
    (data) => {  history.push("/event/") }

  );
  return (dispatch, getState) => {
    dispatch( { type: types.CREATE_EVENT_PENDING } );
    return action(dispatch, getState);
  }
}

export const updateEvent = (id, dto) => {
  var params = { id:id, event: dto }
  return swaggerAction(types.UPDATE_EVENT, 
    (api) => {
      return api.apis[ 'demand-response-controller' ].updateUsingPUT(params, jsonResponseContentType);
    }, 
    parseJsonData,
  );
}

export const deleteEvent = (id) => {
  return swaggerAction(types.DELETE_EVENT, 
    (api) => {
      var params = { id: id };
      return  api.apis[ 'demand-response-controller' ].deleteUsingDELETE(params, jsonResponseContentType);
    },
    () => { history.push("/event/");  },
    (dispatch, getState) => { loadEvent()(dispatch, getState) }
  );
}

export const loadEventDetail = (id) => {
	var params = { id: id };
  return swaggerAction(types.LOAD_EVENT_DETAIL, 
    (api) => {
      return api.apis[ 'demand-response-controller' ].readUsingGET(params, jsonResponseContentType);
    }, 
    parseJsonData
  );
}

export const publishEvent = (id) => {
  var params = { id: id };
  return swaggerAction(types.PUBLISH_EVENT, 
    (api) => {
      return api.apis[ 'demand-response-controller' ].publishUsingPOST(params, jsonResponseContentType);
    }, 
    null,
    (dispatch, getState) => { loadEventDetail(id)(dispatch, getState) }
  );
}

export const activeEvent = (id) => {
  var params = { id: id };
  return swaggerAction(types.ACTIVE_EVENT, 
    (api) => {
      return api.apis[ 'demand-response-controller' ].activeUsingPOST(params, jsonResponseContentType);
    }, 
    null,
    (dispatch, getState) => { loadEventDetail(id)(dispatch, getState) }
  );
}

export const cancelEvent = (id) => {
  var params = { id: id };
  return swaggerAction(types.CANCEL_EVENT, 
    (api) => {
      return api.apis[ 'demand-response-controller' ].cancelUsingPOST(params, jsonResponseContentType);
    }, 
    null,
    (dispatch, getState) => { loadEventDetail(id)(dispatch, getState) }
  );
}

export const loadEventVenResponse = (id) => {
  var params = { id: id };
  return swaggerAction(types.LOAD_EVENT_VEN_RESPONSE, 
    (api) => {
      return api.apis[ 'demand-response-controller' ].readVenDemandResponseEventUsingGET(params, jsonResponseContentType);
    }, 
    parseJsonData
  );
}

import * as types from '../constants/actionTypes';
import objectAssign from 'object-assign';
import initialState from './initialState';

// IMPORTANT: Note that with Redux, state should NEVER be changed.
// State is considered immutable. Instead,
// create a copy of the state passed and set new values on the copy.
// Note that I'm using Object.assign to create a copy of current state
// and update values on the copy.
export default function venDetailReducer( state = initialState.ven_detail, action ) {
  let newState;

  switch (action.type) {

    // VENS
    case types.LOAD_VEN:
      return state;

    case types.LOAD_VEN_DETAIL_SUCCESS:
      newState = objectAssign( {}, state, {
        ven: action.payload
      } );
      return newState;

    case types.LOAD_VEN_DETAIL_ERROR:
      return state;

    case types.LOAD_VEN_MARKET_CONTEXT:
      return state;

    case types.LOAD_VEN_MARKET_CONTEXT_SUCCESS:
      newState = objectAssign( {}, state, {
        venMarketContext: action.payload
      } );
      return newState;

    case types.LOAD_VEN_MARKET_CONTEXT_ERROR:
      return state;

    case types.LOAD_VEN_GROUP:
      return state;

    case types.LOAD_VEN_GROUP_SUCCESS:
      newState = objectAssign( {}, state, {
        venGroup: action.payload
      } );
      return newState;

    case types.LOAD_VEN_GROUP_ERROR:
      return state;

    case types.ADD_VEN_MARKET_CONTEXT:
      return state;

    case types.ADD_VEN_MARKET_CONTEXT_SUCCESS:
      return state;


    case types.ADD_VEN_MARKET_CONTEXT_ERROR:
      return state;


    case types.REMOVE_VEN_MARKET_CONTEXT:
      return state;

    case types.REMOVE_VEN_MARKET_CONTEXT_SUCCESS:
      return state;


    case types.REMOVE_VEN_MARKET_CONTEXT_ERROR:
      return state;


    case types.ADD_VEN_GROUP:
      return state;

    case types.ADD_VEN_GROUP_SUCCESS:
      return state;


    case types.ADD_VEN_GROUP_ERROR:
      return state;


    case types.REMOVE_VEN_GROUP:
      return state;

    case types.REMOVE_VEN_GROUP_SUCCESS:
      return state;


    case types.LOAD_VEN_OPT:
      return state;

    case types.LOAD_VEN_OPT_SUCCESS:
      newState = objectAssign( {}, state, {
        venOpt: action.payload
      } );
      return newState;

    case types.LOAD_VEN_OPT_ERROR:
      return state;

   

      // REGISTRATION PARTY ACTION
    case types.REQUEST_REREGISTRATION_VEN:
      return state;

    case types.REQUEST_REREGISTRATION_VEN_SUCCESS:
      return state;

    case types.REQUEST_REREGISTRATION_VEN_ERROR:
      return state;

    case types.REQUEST_CANCEL_REGISTRATION_VEN:
      return state;

    case types.REQUEST_CANCEL_REGISTRATION_VEN_SUCCESS:
      return state;

    case types.REQUEST_CANCEL_REGISTRATION_VEN_ERROR:
      return state;

    case types.REQUEST_CLEAN_REGISTRATION_VEN:
      return state;

    case types.REQUEST_CLEAN_REGISTRATION_VEN_SUCCESS:
      return state;

    case types.REQUEST_CLEAN_REGISTRATION_VEN_ERROR:
      return state;



    // MARKET CONTEXT
    case types.LOAD_MARKET_CONTEXT:
      return state;

    case types.LOAD_MARKET_CONTEXT_SUCCESS:
      newState = objectAssign( {}, state, {
        marketContext: action.payload
      } );
      return newState;

    case types.LOAD_MARKET_CONTEXT_ERROR:
      return state;

      // GROUPS
    case types.LOAD_GROUP:
      return state;

    case types.LOAD_GROUP_SUCCESS:
      newState = objectAssign( {}, state, {
        group: action.payload
      } );
      return newState;

    case types.LOAD_GROUP_ERROR:
      return state;

    // REPORTS
    case types.LOAD_VEN_AVAILABLE_REPORT:
      return state;

    case types.LOAD_VEN_AVAILABLE_REPORT_SUCCESS:
    	console.log(action)
      newState = objectAssign( {}, state, {
        availableReport: action.payload,
        totalReport: action.total,
        totalPageReport: action.totalPage
      } );
      return newState;

    case types.LOAD_VEN_AVAILABLE_REPORT_ERROR:
      return state;

    case types.LOAD_VEN_REQUESTED_REPORT:
      return state;

    case types.LOAD_VEN_REQUESTED_REPORT_SUCCESS:
      newState = objectAssign( {}, state, {
        requestedReport: action.payload,
        totalRequest: action.total,
        totalPageRequest: action.totalPage
      } );
      return newState;

    case types.LOAD_VEN_REQUESTED_REPORT_ERROR:
      return state;

    case types.LOCATION_CHANGE:
      if(action.payload.location.pathname.includes("/ven/detail")){
        return state;
      }
      else {
        // 예전에는 여기서 initialState.event 를 돌려줬다. 복사해 온 자리를 안 고친 것이다.
        // 그러면 이 슬라이스의 모양이 event 모양으로 바뀌어서, 원래 있던 키가 사라진다.
        // 화면은 그 키를 배열로 알고 map 을 돌리다가 undefined 로 죽는다.
        // (로그인 리다이렉트가 한 번 끼면 이 경로를 그대로 탄다)
        return initialState.ven_detail;
      }

    default:
      return state;
  }
}

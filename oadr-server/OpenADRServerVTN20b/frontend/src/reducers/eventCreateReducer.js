import * as types from '../constants/actionTypes';
import objectAssign from 'object-assign';
import initialState from './initialState';
import { errorOf } from '../actions/apiUtils';

// IMPORTANT: Note that with Redux, state should NEVER be changed.
// State is considered immutable. Instead,
// create a copy of the state passed and set new values on the copy.
// Note that I'm using Object.assign to create a copy of current state
// and update values on the copy.

export default function eventCreateReducer( state = initialState.event_create, action ) {
  let newState;

  switch (action.type) {

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

      // VEN
    case types.SEARCH_VEN:
      return state;

    case types.SEARCH_VEN_SUCCESS:
      newState = objectAssign( {}, state, {
        ven: action.payload
      } );
      return newState;

    case types.SEARCH_VEN_ERROR:
      return state;

      // EVENT. CREATE_EVENT 는 swagger 미들웨어가 리듀서에 넘기지 않아서 PENDING 으로 받는다
    case types.CREATE_EVENT_PENDING:
      return objectAssign( {}, state, {
        creating: true,
        createError: null
      } );

    case types.CREATE_EVENT_SUCCESS:
      return objectAssign( {}, state, {
        creating: false
      } );

    // 예전에는 처리하지 않아서 서버가 거절해도 화면이 그대로였다
    case types.CREATE_EVENT_ERROR:
      return objectAssign( {}, state, {
        creating: false,
        createError: errorOf( action.payload )
      } );

    case types.LOCATION_CHANGE:
      return initialState.event_create;

    default:
      return state;
  }
}

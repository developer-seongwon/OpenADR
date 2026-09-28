import * as types from '../constants/actionTypes';
import { history } from '../store/configureStore';

import { swaggerAction, jsonResponseContentType, multipartResponseContentType, saveData, parseJsonData } from './apiUtils';


export const loadVen = () => {
  return swaggerAction(types.LOAD_VEN, 
    (api) => {
      return api.apis[ 'ven-controller' ].listVenUsingGET(jsonResponseContentType);
    }, 
    parseJsonData
  );
}

export const searchVen = (filters, page, size) => {
  var params = {filters, page, size};
  return swaggerAction(types.SEARCH_VEN, 
    (api) => {
      return api.apis[ 'ven-controller' ].searchVenUsingPOST(params, jsonResponseContentType);
    }, 
    parseJsonData
  );
}

export const loadVenDetail = (username) => {
  return swaggerAction(types.LOAD_VEN_DETAIL, 
    (api) => {
      var params = { venID: username };
      return api.apis[ 'ven-controller' ].findVenByUsernameUsingGET(params, jsonResponseContentType);
    },
    parseJsonData
  );
}

export const createVen = (ven) => {
  return swaggerAction(types.CREATE_VEN, 
    (api) => {
      var params = { dto: ven };
      return api.apis[ 'ven-controller' ].createVenUsingPOST(params, multipartResponseContentType);
    },
    (data) => { saveData( data.data, ven.commonName + '-credentials.tar' ); history.push("/ven/") }
  );
}

export const deleteVen = (venId) => {
  return swaggerAction(types.DELETE_VEN, 
    (api) => {
      var params = { venID: venId };
      return  api.apis[ 'ven-controller' ].deleteVenByUsernameUsingDELETE(params, jsonResponseContentType);
    },
    () => { history.push("/ven/");  },
    (dispatch, getState) => { loadVen()(dispatch, getState) }
  );
}

export const loadVenGroup = (venId) => {
  return swaggerAction(types.LOAD_VEN_GROUP, 
    (api) => {
      var params = { venID: venId };
      return  api.apis[ 'ven-controller' ].listVenGroupUsingGET(params, jsonResponseContentType);
    },
    parseJsonData
  );
}

export const loadVenMarketContext = (venId) => {
  return swaggerAction(types.LOAD_VEN_MARKET_CONTEXT, 
    (api) => {
      var params = { venID: venId };
      return  api.apis[ 'ven-controller' ].listVenMarketContextUsingGET(params, jsonResponseContentType);
    },
    parseJsonData
  );
}

export const addVenMarketContext = (venId, marketContextId) => {
  return swaggerAction(types.ADD_VEN_MARKET_CONTEXT, 
    (api) => {
      var params = { venID: venId, marketContextId: marketContextId };
      return  api.apis[ 'ven-controller' ].addMarketContextToVenUsingPOST(params, jsonResponseContentType);
    },
    parseJsonData,
    (dispatch, getState) => { loadVenMarketContext( venId )( dispatch, getState ) }
  );
}

export const addVenGroup = (venId, groupId) => {
  return swaggerAction(types.ADD_VEN_GROUP, 
    (api) => {
      var params = { venID: venId, groupId: groupId };
      return  api.apis[ 'ven-controller' ].addGroupToVenUsingPOST(params, jsonResponseContentType);
    },
    parseJsonData,
    (dispatch, getState) => { loadVenGroup( venId )( dispatch, getState ) }
  );
}

export const removeVenMarketContext = (venId, marketContextId) => {
  return swaggerAction(types.REMOVE_VEN_MARKET_CONTEXT, 
    (api) => {
      var params = { venID: venId, marketContextId: marketContextId };
      return  api.apis[ 'ven-controller' ].deleteVenMarketContextUsingPOST(params, jsonResponseContentType);
    },
    null,
    (dispatch, getState) => { loadVenMarketContext( venId )( dispatch, getState ) }
  );
}

export const removeVenGroup = (venId, groupId) => {
  return swaggerAction(types.REMOVE_VEN_GROUP, 
    (api) => {
      var params = { venID: venId, groupId: groupId };
      return  api.apis[ 'ven-controller' ].deleteVenGroupUsingPOST(params, jsonResponseContentType);
    },
    null,
    (dispatch, getState) => { loadVenGroup( venId )( dispatch, getState ) }
  );
}

export const registerPartyRequestReregistration = (venId) => {
  return swaggerAction(types.REQUEST_REREGISTRATION_VEN, 
    (api) => {
      var params = { venID: venId };
      return  api.apis[ 'oadr-20b-ven-controller' ].registerPartyRequestReregistrationUsingPOST(params, jsonResponseContentType);
    },
  );
}

export const registerPartyCancelPartyRegistration = (venId) => {
  return swaggerAction(types.REQUEST_CANCEL_REGISTRATION_VEN, 
    (api) => {
      var params = { venID: venId };
      return  api.apis[ 'oadr-20b-ven-controller' ].registerPartyCancelPartyRegistrationUsingPOST(params, jsonResponseContentType);
    },
  );
}

export const cleanRegistration = (venId) => {
  return swaggerAction(types.REQUEST_CLEAN_REGISTRATION_VEN, 
    (api) => {
      var params = { venID: venId };
      return  api.apis[ 'ven-controller' ].cleanRegistrationUsingPOST(params, jsonResponseContentType);
    },
  );
}

export const loadVenAvailableReport = (venId, reportSpecifierId) => {
  return swaggerAction(types.LOAD_VEN_AVAILABLE_REPORT, 
    (api) => {
      var params = { venID: venId, reportSpecifierId:reportSpecifierId };
      return  api.apis[ 'oadr-20b-ven-controller' ].viewOtherReportCapabilityUsingGET(params, jsonResponseContentType);
    },
    parseJsonData
  );
}

export const loadVenAvailableReportDescription = (venId, reportSpecifierId) => {
  return swaggerAction(types.LOAD_VEN_AVAILABLE_REPORT_DESCRIPTION, 
    (api) => {
      var params = { venID: venId, reportSpecifierId: reportSpecifierId };
      return  api.apis[ 'oadr-20b-ven-controller' ].viewOtherReportCapabilityDescriptionUsingGET(params, jsonResponseContentType);
    },
    parseJsonData
  );
}

export const loadVenRequestedReport = (venId, reportRequestId) => {
  return swaggerAction(types.LOAD_VEN_REQUESTED_REPORT, 
    (api) => {
      var params = { venID: venId, reportRequestId: reportRequestId };
      return  api.apis[ 'oadr-20b-ven-controller' ].viewReportRequestUsingGET(params, jsonResponseContentType);
    },
    parseJsonData
  );
}

export const loadVenRequestedReportSpecifier = (venId, reportRequestId) => {
	  return swaggerAction(types.LOAD_VEN_REQUESTED_REPORT_SPECIFIER, 
	    (api) => {
	      var params = {venID: venId, criteria: { reportRequestId: [reportRequestId] }};
	      return  api.apis[ 'oadr-20b-ven-controller' ].viewReportRequestSpecifierUsingPOST(params, jsonResponseContentType);
	    },
	    parseJsonData
	  );
	}

export const requestRegisterReport  = (venId) => {
  return swaggerAction(types.REQUEST_VEN_REGISTER_REPORT, 
    (api) => {
      var params = { venID: venId };
      return  api.apis[ 'oadr-20b-ven-controller' ].requestRegisterReportUsingPOST(params, jsonResponseContentType);
    },
  );
}

export const sendRegisterReport  = (venId) => {
  return swaggerAction(types.SEND_VTN_REGISTER_REPORT, 
    (api) => {
      var params = { venID: venId };
      return  api.apis[ 'oadr-20b-ven-controller' ].sendRegisterReportUsingPOST(params, jsonResponseContentType);
    },
  );
}

// 리포트를 한 번만 받아온다.
//
// 서버는 POST /Ven/{venID}/report/available/description/request 하나뿐이고
// 본문으로 배열을 받는다. rid 를 빼면 그 명세의 전부를 뜻한다.
//
// 예전에는 rid 가 null 일 때 requestAllOtherReportCapabilityDescriptionRid 라는
// 없는 오퍼레이션을 불렀고, rid 를 줄 때도 평평한 쿼리 파라미터로 보내서 400 이 났다.
// 화면에서 rid 를 전부 고르면 null 이 되므로 사실상 항상 깨져 있었다.
export const createRequestedReport  = (venId, reportSpecifierId, start, end, rid) => {
  var request = { reportSpecifierId: reportSpecifierId, start: start, end: end };
  if(rid != null) {
    request.rid = rid;
  }
  return swaggerAction(types.CREATE_REQUESTED_REPORT, 
    (api) => {
      var params = { venID: venId, requests: [ request ] };
      return  api.apis[ 'oadr-20b-ven-controller' ].requestOtherReportCapabilityDescriptionRidUsingPOST(params, jsonResponseContentType);
    },
    () => { history.push("/ven/detail/"+venId+"/reports");  },
  );
}


// 리포트를 계속 받아온다.
//
// request 와 마찬가지로 서버는 본문에 배열을 받는다.
// rid 는 Map<이름, 이력을 남길지> 이고, 빼면 그 명세의 전부를 남기는 것으로 친다.
//
// 예전에는 rid 를 줄 때만 평평한 쿼리 파라미터로 보내서 400 이 났다.
// rid 를 안 줄 때는 제대로 된 본문을 보내고 있어서 그쪽만 동작했다.
export const createRequestedReportSubscription  = (venId, reportSpecifierId, granularity, reportBackDuration, rid) => {
  var subscription = { reportSpecifierId: reportSpecifierId
    , granularity: granularity
    , reportBackDuration: reportBackDuration
  };
  if(rid != null) {
    var ridMap = {};
    for(var i in rid) {
      ridMap[rid[i]] = true;
    }
    subscription.rid = ridMap;
  }
  return swaggerAction(types.CREATE_REQUESTED_REPORT, 
    (api) => {
      var params = { venID: venId, subscriptions: [ subscription ] };
      return  api.apis[ 'oadr-20b-ven-controller' ].subscribeOtherReportCapabilityDescriptionRidUsingPOST(params, jsonResponseContentType);
    },
    () => { history.push("/ven/detail/"+venId+"/reports");  },
  );
}

export const cancelRequestReportSubscription  = (venId, reportRequestId) => {
  return swaggerAction(types.CANCEL_REQUESTED_REPORT, 
    (api) => {
      var params = { venID: venId, reportRequestId:reportRequestId };
      return  api.apis[ 'oadr-20b-ven-controller' ].cancelSubscriptionReportRequestUsingPOST(params, jsonResponseContentType);
    },
    null,
    (dispatch, getState) => { loadVenRequestedReport( venId )( dispatch, getState ) }
  );
}


export const loadVenOpt = (venId) => {
  return swaggerAction(types.LOAD_VEN_OPT, 
    (api) => {
      var params = { venID: venId };
      return  api.apis[ 'oadr-20b-ven-controller' ].venOptUsingGET(params, jsonResponseContentType);
    },
    parseJsonData
  );
}

export const pageVenAvailableReport = (venId, page, size) => {
	  return swaggerAction(types.LOAD_VEN_AVAILABLE_REPORT, 
	    (api) => {
	      var params = { venID: venId, page:page, size: size };
	      return  api.apis[ 'oadr-20b-ven-controller' ].pageOtherReportCapabilityUsingGET(params, jsonResponseContentType);
	    },
	    parseJsonData
	  );
	}

export const pageVenRequestedReport = (venId, page, size) => {
	  return swaggerAction(types.LOAD_VEN_REQUESTED_REPORT, 
	    (api) => {
	      var params = { venID: venId, page:page, size: size };
	      return  api.apis[ 'oadr-20b-ven-controller' ].pageReportRequestUsingGET(params, jsonResponseContentType);
	    },
	    parseJsonData
	  );
	}



package com.avob.openadr.server.common.vtn.service;

import com.avob.openadr.server.common.vtn.models.ven.Ven;

/**
 * VEN 을 지우기 직전에 불린다.
 *
 * VTNCommon 은 자기가 아는 테이블(venresource, vendemandresponseevent)만 비울 수 있다.
 * 그런데 VEN 을 참조하는 테이블은 프로파일별 모듈에도 있다. 예를 들어 VTN20b 의
 * otherreportcapability, otherreportrequest, selfreportrequest, venopt 가 그렇다.
 * 이것들이 남아 있으면 VEN 삭제가 외래키 제약에 걸려 500 으로 끝난다.
 *
 * VTNCommon 이 그 테이블들을 알 수는 없으므로, 각 모듈이 자기 뒷정리를 스스로
 * 등록하게 한다. 구현체를 빈으로 올려 두기만 하면 VenService 가 알아서 부른다.
 *
 * 구현체는 VEN 이 참조하는 자기 쪽 행을 전부 지워야 하고,
 * 같은 VEN 으로 두 번 불려도 문제가 없어야 한다.
 */
public interface VenDeleteHandler {

	void onVenDelete(Ven ven);

}

package com.avob.openadr.server.oadr20b.ven.exception;

import com.avob.openadr.model.oadr20b.oadr.OadrCanceledPartyRegistrationType;

/**
 * 아무 데서도 던지지도 잡지도 않는다.
 *
 * VEN 의 등록 해지 경로는 Oadr20bApplicationLayerException 으로 통일돼 있다. 지워도 된다.
 */
@Deprecated
public class Oadr20bCancelPartyRegistrationApplicationLayerException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3093433293866944676L;

    private final OadrCanceledPartyRegistrationType response;

    public Oadr20bCancelPartyRegistrationApplicationLayerException(Exception e,
            OadrCanceledPartyRegistrationType response) {
        super(e);
        this.response = response;
    }

    public Oadr20bCancelPartyRegistrationApplicationLayerException(String message,
            OadrCanceledPartyRegistrationType response) {
        super(message);
        this.response = response;
    }

    public OadrCanceledPartyRegistrationType getResponse() {
        return response;
    }
}

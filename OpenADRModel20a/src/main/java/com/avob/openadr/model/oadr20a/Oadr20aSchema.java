package com.avob.openadr.model.oadr20a;

/**
 * 아무 데서도 쓰지 않는다.
 *
 * 2.0b 쪽 Oadr20bSchema 와 짝을 맞추려고 만든 것으로 보이는데, 20a 코드에서는
 * schemaVersion 을 이 상수 대신 리터럴로 넣고 있다. 지워도 된다.
 */
@Deprecated
public class Oadr20aSchema {
    public static final String SCHEMA_VERSION = "2.0a";
    
    private Oadr20aSchema(){
    }
}

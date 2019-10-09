package dev.xframe.http.service.rest;

import dev.xframe.http.decode.HttpBody;
import dev.xframe.http.decode.MultiPart;
import dev.xframe.http.decode.QueryString;
import dev.xframe.http.service.Request.Params;
import dev.xframe.utils.XStrings;

public interface BodyDecoder {
	
    default Object decode(Class<?> type, HttpBody body) {
        if(type.equals(MultiPart.class)) {
            return body.asMultiPart();
        } else if(type.equals(byte[].class)) {
            return body.asBytes();
        } else if(type.equals(Params.class) || type.equals(QueryString.class)) {
            return body.asParams();
        } else if(type.equals(HttpBody.class)) {
            return body;
        } else if(type.equals(String.class)) {
            return XStrings.newStringUtf8(body.asBytes());
        }
        return decode(type, body.asBytes());
    }
    
	public Object decode(Class<?> type, byte[] body);

}

package dev.xframe.http.service.config;

import dev.xframe.http.decode.HttpBody;
import dev.xframe.http.decode.MultiPart;
import dev.xframe.http.decode.QueryString;
import dev.xframe.utils.XStrings;

@FunctionalInterface
public interface BodyDecoder {
	
    default Object decode(Class<?> type, HttpBody body) {
        if(type.equals(MultiPart.class)) {
            return body.toMultiPart();
        } else if(type.equals(byte[].class)) {
            return body.toBytes();
        } else if(type.equals(QueryString.class)) {
            return body.toQueryString();
        } else if(type.equals(HttpBody.class)) {
            return body;
        } else if(type.equals(String.class)) {
            return XStrings.newStringUtf8(body.toBytes());
        }
        return decode(type, body.toBytes());
    }
    
	public Object decode(Class<?> type, byte[] body);

}

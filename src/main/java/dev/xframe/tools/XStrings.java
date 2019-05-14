package dev.xframe.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class XStrings {
    
    private static final Charset UTF8 = Charset.forName("utf-8");
    private static final byte[] EMPTY_BYTES = new byte[0];
    
    public static boolean isEmpty(String x) {
        int len;
        if (x == null || (len = x.length()) == 0)
            return true;

        while (len-- > 0) {
            if (!Character.isWhitespace(x.charAt(len)))
                return false;
        }

        return true;
    }
    
    public static byte[] getBytes(final String string, final Charset charset) {
        if (string == null) return EMPTY_BYTES;
        return string.getBytes(charset);
    }
    public static byte[] getBytesUtf8(final String string) {
        return getBytes(string, UTF8);
    }
    
    public static String newStringUtf8(final byte[] bytes) {
        return newString(bytes, UTF8);
    }
    public static String newString(final byte[] bytes, final Charset charset) {
        return bytes == null ? "" : new String(bytes, charset);
    }
    
    public static String trim(final String src, char c) {
        return trim(src, 0, src.length(), c);
    }
    public static String trim(final String src, int start, int end, char c) {
        if(isEmpty(src)) return "";
        
        while ((start < end) && src.charAt(start) == c) {
            ++ start;
        }
        
        while ((start < end) && src.charAt(end-1) == c) {
            -- end;
        }
        return end > start ? src.substring(start, end) : "";
    }
    
    public static String getStackTrace(Throwable t) {
        try {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        } catch (Exception ex){
            return t.getMessage();
        }
    }
    
}

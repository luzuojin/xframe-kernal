package dev.xframe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class XStrings {
    
    private static final byte[] EMPTY_BYTES = new byte[0];
    
    public static String orElse(String b, String t) {
    	return isEmpty(b) ? t : b;
    }
    public static int orElse(String val, int def) {
		return isEmpty(val) ? def : Integer.parseInt(val);
	}
    public static long orElse(String val, long def) {
		return isEmpty(val) ? def : Long.parseLong(val);
	}
    public static boolean orElse(String val, boolean def) {
		return isEmpty(val) ? def : Boolean.parseBoolean(val);
	}

    public static boolean isPresent(String x) {
        return !isEmpty(x);
    }
    public static boolean isEmpty(String x) {
        return x == null || x.length() == 0;
    }
    public static boolean isBlank(String x) {
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
        if (string == null)
            return EMPTY_BYTES;
        return string.getBytes(charset);
    }
    public static byte[] getBytesUtf8(final String string) {
        return getBytes(string, StandardCharsets.UTF_8);
    }
    
    public static String newStringUtf8(final byte[] bytes) {
        return newString(bytes, StandardCharsets.UTF_8);
    }
    public static String newString(final byte[] bytes, final Charset charset) {
        return bytes == null ? "" : new String(bytes, charset);
    }
    
    public static String trim(final String src, char c) {
        return trim(src, 0, src.length(), c);
    }
    public static String trim(final String src, int start, int end, char c) {
        if(isEmpty(src))
            return "";
        
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

    public static String readFrom(InputStream in) {
        return readFrom(in, StandardCharsets.UTF_8);
    }
    public static String readFrom(InputStream in, final Charset charset) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while(((b = in.read()) != -1)) {
                out.write(b);
            }
            return out.toString(charset.name());
        } catch (IOException e) {
            throw XCaught.throwException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw XCaught.throwException(e);
            }
        }
    }
    
}

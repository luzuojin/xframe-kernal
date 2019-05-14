package dev.xframe.http.decode;

import java.nio.charset.Charset;

import dev.xframe.http.service.Request.Params;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.CharsetUtil;

public class HttpBody {
    
    private static final int MAX_BODY_BYTES = 1 * 1024 * 1024;
    
    private HttpRequest request;
    private ByteBuf byteBuf;
    
    public HttpBody(HttpRequest request) {
        this.request = request;
    }
    
    public void setByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public void offer(HttpContent content) {
        if(byteBuf == null) {
            byteBuf = Unpooled.buffer();
        }
        
        byteBuf.writeBytes(content.content());
        
        if(byteBuf.readableBytes() > MAX_BODY_BYTES) {
            throw new IllegalArgumentException("Http server max body size is 1M");
        }
    }
    
    public byte[] asBytes() {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }
    
    public Params asParams() {
        return new Params(byteBuf.toString(CharsetUtil.UTF_8));
    }
    
    public MultiPart asMultiPart() {
        return new MultiPart(request, byteBuf);
    }
    
    
    
    
    public static final int chunkSize = 8096;
    /**
     * HTTP content disposition header name.
     */
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String NAME = "name";

    public static final String FILENAME = "filename";

    /**
     * Content-disposition value for form data.
     */
    public static final String FORM_DATA = "form-data";

    /**
     * Content-disposition value for file attachment.
     */
    public static final String ATTACHMENT = "attachment";

    /**
     * Content-disposition value for file attachment.
     */
    public static final String FILE = "file";

    /**
     * HTTP content type body attribute for multiple uploads.
     */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    /**
     * Charset for 8BIT
     */
    public static final Charset ISO_8859_1 = CharsetUtil.ISO_8859_1;

    /**
     * Charset for 7BIT
     */
    public static final Charset US_ASCII = CharsetUtil.US_ASCII;

    /**
     * Default Content-Type in binary form
     */
    public static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";

    /**
     * Default Content-Type in Text form
     */
    public static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

    /**
     * Allowed mechanism for multipart
     * mechanism := "7bit"
                  / "8bit"
                  / "binary"
       Not allowed: "quoted-printable"
                  / "base64"
     */
    public enum TransferEncodingMechanism {
        /**
         * Default encoding
         */
        BIT7("7bit"),
        /**
         * Short lines but not in ASCII - no encoding
         */
        BIT8("8bit"),
        /**
         * Could be long text not in ASCII - no encoding
         */
        BINARY("binary");

        private final String value;

        TransferEncodingMechanism(String value) {
            this.value = value;
        }

        TransferEncodingMechanism() {
            value = name();
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
    * Exception when NO Backend Array is found
    */
    static class SeekAheadNoBackArrayException extends Exception {
        private static final long serialVersionUID = -630418804938699495L;
    }

    /**
    * This class intends to decrease the CPU in seeking ahead some bytes in
    * HttpPostRequestDecoder
    */
    static class SeekAheadOptimize {
        byte[] bytes;
        int readerIndex;
        int pos;
        int origPos;
        int limit;
        ByteBuf buffer;

        SeekAheadOptimize(ByteBuf buffer) throws SeekAheadNoBackArrayException {
            if (!buffer.hasArray()) {
                throw new SeekAheadNoBackArrayException();
            }
            this.buffer = buffer;
            bytes = buffer.array();
            readerIndex = buffer.readerIndex();
            origPos = pos = buffer.arrayOffset() + readerIndex;
            limit = buffer.arrayOffset() + buffer.writerIndex();
        }

        /**
        *
        * @param minus this value will be used as (currentPos - minus) to set
        * the current readerIndex in the buffer.
        */
        void setReadPosition(int minus) {
            pos -= minus;
            readerIndex = getReadPosition(pos);
            buffer.readerIndex(readerIndex);
        }

        /**
        *
        * @param index raw index of the array (pos in general)
        * @return the value equivalent of raw index to be used in readerIndex(value)
        */
        int getReadPosition(int index) {
            return index - origPos + readerIndex;
        }

        void clear() {
            buffer = null;
            bytes = null;
            limit = 0;
            pos = 0;
            readerIndex = 0;
        }
    }

    /**
     * Find the first non whitespace
     * @return the rank of the first non whitespace
     */
    static int findNonWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    /**
     * Find the first whitespace
     * @return the rank of the first whitespace
     */
    static int findWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    /**
     * Find the end of String
     * @return the rank of the end of string
     */
    static int findEndOfString(String sb) {
        int result;
        for (result = sb.length(); result > 0; result --) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }
    

}

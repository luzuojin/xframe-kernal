package dev.xframe.utils;

public class XCaught {
    
    /**
     * 编译期Exception包装成RuntimeException
     * @param cause
     * @return
     */
    public static RuntimeException wrapException(String message, Throwable cause) {
        return new WrappedException(message, cause);
    }
    
    /**
     * 编译期Exception包装成RuntimeException
     * @param cause
     * @return
     */
    public static RuntimeException wrapException(Throwable cause) {
        return new WrappedException(cause);//can`t be here
    }
    
    /**
     * 绕过编译检查 直接抛出原始Exception
     * @param e
     * @return 只是为了通过编译, 不会真实执行到return
     */
    public static <T> T throwException(Throwable e) {
        XCaught.<RuntimeException>throwException0(e);
        return null;
    }
    
    @SuppressWarnings("unchecked")
    static <E extends Throwable> void throwException0(Throwable e) throws E {
        throw (E) e;
    }
    
    public static class WrappedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public WrappedException(Throwable e) {
            super(e);
        }
        public WrappedException(String message, Throwable e) {
            super(message, e);
        }
    }

}

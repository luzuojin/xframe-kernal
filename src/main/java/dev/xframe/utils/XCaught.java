package dev.xframe.utils;

public class XCaught {
    
    /**
     * 绕过编译检查 直接抛出原始Exception
     * @param e
     * @return 只是为了通过编译, 不会真实执行到return
     */
    public static RuntimeException throwException(Throwable e) {
        XCaught.<RuntimeException>throwException0(e);
        return null;
    }
    
    @SuppressWarnings("unchecked")
    static <E extends Throwable> void throwException0(Throwable e) throws E {
        throw (E) e;
    }

}

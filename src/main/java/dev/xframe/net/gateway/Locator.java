package dev.xframe.net.gateway;

import java.util.LinkedList;
import java.util.List;

import dev.xframe.net.codec.IMessage;

/**
 * 在协议号范围内
 * 有过滤器的情况下匹配到一个过滤器
 * @author luzj
 */
public class Locator {
    
    private List<Range> ranges = new LinkedList<>();
    
    private boolean frozen = false;
    private List<Filter> filters = new LinkedList<>();
    
    /**
     * @param start @see IMessage.getCode()
     * @param end   @see IMessage.getCode()
     * @return
     */
    public Locator range(int start, int end) {
        ranges.add(new Range(start, end));
        return this;
    }
    
    public Locator filter(Filter filter) {
        if(frozen)
            throw new UnsupportedOperationException("Locator has been frozen");
        this.filters.add(filter);
        return this;
    }
    
    public Locator freeze() {
        this.frozen = true;
        return this;
    }
    
    boolean test(IMessage message) {
        return testRange(message) && testFilter(message);
    }

    private boolean testRange(IMessage message) {
        final int code = message.getCode();
        for (Range range : ranges) {
            if(range.test(code)) return true;
        }
        return false;
    }
    
    private boolean testFilter(IMessage message) {
        if(filters.isEmpty()) {
            return true;
        }
        for (Filter filter : filters) {
            if(filter.test(message)) return true;
        }
        return false;
    }

    static class Range {
        int start;
        int end;
        Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
        boolean test(int code) {
            return start <= code && code <= end;
        }
    }
    
    public Locator copy() {
        Locator locator = new Locator();
        locator.ranges.addAll(this.ranges);
        locator.filters.addAll(this.filters);
        return locator;
    }
    
    @FunctionalInterface
    public interface Filter {
        public boolean test(IMessage message);
    }

}

package dev.xframe.dev;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;
import com.sun.management.OperatingSystemMXBean;

import dev.xframe.http.Response;
import dev.xframe.http.service.Rest;
import dev.xframe.http.service.rest.HttpMethods;

@Rest("dev/jstat")
@SuppressWarnings("restriction")
public class JStatService {

	@HttpMethods.GET("memory")
	public Object memory() {
		return Response.of(formatMemory());
	}
	
	@HttpMethods.GET("stack")
	public Object stack() {
		return Response.of(formatStack());
	}
	
	@HttpMethods.GET("gc")
	public Object gc() {
		return Response.of(formatGc());
	}
	
	@HttpMethods.GET("uptime")
	public Object uptime() {
		return Response.of(formatUptime());
	}
	
	public static String formatGc() {
        StringBuilder ret = new StringBuilder("--------------------------------").append("\n");
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getPlatformMXBeans(GarbageCollectorMXBean.class);
        for (GarbageCollectorMXBean gc : gcs) {
            ret.append(String.format("name: %s, gccount: %s, gctime: %sms", gc.getName(), gc.getCollectionCount(), gc.getCollectionTime())).append("\n");
            GcInfo gcInfo = gc.getLastGcInfo();
            if(gcInfo != null) {
//                long duration = gcInfo.getDuration();
//                Map<String, MemoryUsage> memoryUsageBeforeGc = gcInfo.getMemoryUsageBeforeGc();
//                Map<String, MemoryUsage> memoryUsageAfterGc = gcInfo.getMemoryUsageAfterGc();
//                CompositeType compositeType = gcInfo.getCompositeType();
            }
        }
        return ret.append("--------------------------------").append("\n").toString();
    }
	
    public static String formatStack() {
        StringBuilder ret = new StringBuilder("--------------------------------").append("\n");
        ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        for (ThreadInfo threadInfo : threads) {
            ret.append(threadInfo);
        }
        return ret.append("--------------------------------").append("\n").toString();
    }
	
	public static String formatMemory() {
        StringBuilder ret = new StringBuilder("--------------------------------").append("\n");
        List<MemoryPoolMXBean> mps = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean mp : mps) {
            ret.append(formatUsage(mp.getName(), mp.getUsage())).append("\n");
        }
        List<BufferPoolMXBean> dircts = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        for (BufferPoolMXBean direct : dircts) {
        	ret.append(formatDirect(direct)).append("\n");
		}
        return ret.append("--------------------------------").append("\n").toString();
    }

	public static String formatDirect(BufferPoolMXBean direct) {
		return String.format("%18s, count:%4d, capacity:%4dM, used:%4dM",
				direct.getName(), direct.getCount(), tomb(direct.getTotalCapacity()), tomb(direct.getMemoryUsed()));
	}

    public static String formatUsage(String name, MemoryUsage usage) {
        return String.format("%18s, init:%4dM, used:%4dM, commited:%4dM, max:%4dM",
                name, tomb(usage.getInit()), tomb(usage.getUsed()), tomb(usage.getCommitted()), tomb(usage.getMax()));
    }

    public static long tomb(long init) {
        return init >> 10 >> 10;
    }
	
    public static String formatUptime() {
        StringBuilder ret = new StringBuilder("--------------------------------").append("\n");
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        ret.append(runtime.getName()).append(": ").append(runtime.getVmName()).append("\n");
        ret.append("uptime: ").append(formatUptime0(runtime.getUptime())).append("\n");
        OperatingSystemMXBean opsys = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        ret.append("loadav: ").append(String.format("%.2f/%d", opsys.getSystemLoadAverage(), opsys.getAvailableProcessors())).append("\n");
        return ret.append("--------------------------------").append("\n").toString();
    }
    
    private static String formatUptime0(long uptime) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime);
        return String.format("%dd:%dh:%dm", minutes/60/24, minutes/60%24, minutes%60);
    }
	
}

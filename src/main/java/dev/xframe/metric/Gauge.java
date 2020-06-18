package dev.xframe.metric;

/**
 * 瞬时值
 * @author luzj
 */
public class Gauge {
	
	public static final Gauge NIL = new Gauge() {
		public Gauge creating(long createTime) {return this;}
		public Gauge creating() {return this;}
		public Gauge beginning() {return this;}
		public Gauge ending() {return this;}
        public void apply() {}
	};
	
	Class<?> ident;
	
	long createTime;
	
	long beginTime;
	
	long endTime;
	
	public Gauge() {
	}
	
	public Gauge(Class<?> ident) {
		this.ident = ident;
	}

	public static Gauge of(Class<?> ident) {
		return Metric.watching ? new Gauge(ident) : NIL;
	}
	
	public Gauge creating() {
		creating(System.currentTimeMillis());
		return this;
	}
	
	public Gauge creating(long createTime) {
		this.createTime = createTime;
		return this;
	}

	public Gauge beginning() {
		this.beginTime = System.currentTimeMillis();
		return this;
	}
	
	public Gauge ending() {
		this.endTime = System.currentTimeMillis();
		return this;
	}
	
	public long used() {
		return this.endTime - beginTime;
	}
	
	public long waited() {
		return createTime == 0 ? 0 : beginTime - createTime;
	}

	public String name() {
		return ident.getName();
	}
	
	public void apply() {
		Metric.apply(this);
	}

}

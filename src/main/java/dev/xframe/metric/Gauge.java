package dev.xframe.metric;

/**
 * 瞬时值
 * @author luzj
 */
public class Gauge {
	
	public static final Gauge NIL = new Gauge() {
		public Gauge create(long createTime) {return this;}
		public Gauge create() {return this;}
		public Gauge begin() {return this;}
		public Gauge end() {return this;}
        public void apply() {}
	};
	
	String ident;
	
	long createTime;
	
	long beginTime;
	
	long endTime;
	
	public Gauge() {
	}
	
	public Gauge(String ident) {
		this.ident = ident;
	}

	public Gauge create() {
		create(System.currentTimeMillis());
		return this;
	}
	
	public Gauge create(long createTime) {
		this.createTime = createTime;
		return this;
	}

	public Gauge begin() {
		this.beginTime = System.currentTimeMillis();
		return this;
	}
	
	public Gauge end() {
		this.endTime = System.currentTimeMillis();
		return this;
	}
	
	public long used() {
		return this.endTime - beginTime;
	}
	
	public long waited() {
		return createTime == 0 ? 0 : beginTime - createTime;
	}

	public String ident() {
		return ident;
	}
	
	public void apply() {
		Metric.apply(this);
	}
	
	public static Gauge of(String ident) {
		return Metric.gauge(ident);
	}

}

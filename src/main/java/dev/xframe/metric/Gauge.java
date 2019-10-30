package dev.xframe.metric;

public class Gauge {
	
	public static final Gauge NIL = new Gauge() {
		public Gauge creating(long createTime) {return this;}
		public Gauge creating() {return this;}
		public Gauge beginning() {return this;}
		public Gauge ending() {return this;}
	};
	
	Class<?> ident;
	
	long createTime;
	
	long beginTime;
	
	long endTime;
	
	Scriber scriber = Scriber.NIL;
	
	public Gauge() {
	}
	
	public Gauge(Class<?> ident) {
		this.ident = ident;
	}

	public static Gauge of(Class<?> ident) {
		return Metrics.watching ? new Gauge(ident) : NIL;
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
	
	public Gauge with(Scriber scriber) {
		this.scriber = scriber;
		return this;
	}
	
	public long used() {
		return this.endTime - beginTime;
	}
	
	public long waited() {
		return this.beginTime - createTime;
	}

	public String name() {
		return ident.getName();
	}
	
	public void apply() {
		Metrics.apply(this);
	}

}
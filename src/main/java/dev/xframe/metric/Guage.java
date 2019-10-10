package dev.xframe.metric;

public class Guage {
	
	public static final Guage NIL = new Guage() {
		public Guage creating(long createTime) {return this;}
		public Guage creating() {return this;}
		public Guage beginning() {return this;}
		public Guage ending() {return this;}
	};
	
	Class<?> ident;
	
	long createTime;
	
	long beginTime;
	
	long endTime;
	
	Scriber scriber = Scriber.NIL;
	
	public Guage() {
	}
	
	public Guage(Class<?> ident) {
		this.ident = ident;
	}

	public static Guage of(Class<?> ident) {
		return Metrics.watching ? new Guage(ident) : NIL;
	}
	
	public Guage creating() {
		creating(System.currentTimeMillis());
		return this;
	}
	
	public Guage creating(long createTime) {
		this.createTime = createTime;
		return this;
	}

	public Guage beginning() {
		this.beginTime = System.currentTimeMillis();
		return this;
	}
	
	public Guage ending() {
		this.endTime = System.currentTimeMillis();
		return this;
	}
	
	public Guage with(Scriber scriber) {
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

package dev.xframe.utils.proto;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;

public class FieldSerializer {
	
	protected FieldInvoker jField;
	protected FieldDescriptor pField;
	protected MessageHandler mHandler;
	
	public FieldSerializer(FieldInvoker jField, FieldDescriptor pField, MessageHandler mHandler) {
		this.jField = jField;
		this.pField = pField;
		this.mHandler = mHandler;
	}
	
	public void parse(Object obj, DynamicMessage dynMessage) {
		jField.set(obj, getDataFromProto(dynMessage));
	}

	Object getDataFromProto(DynamicMessage dynMessage) {
		return mHandler.fromProto(dynMessage.getField(pField));
	}
	
	public void build(DynamicMessage.Builder dynBuilder, Object obj) {
		dynBuilder.setField(pField, getDataFromJava(obj));
	}

	Object getDataFromJava(Object obj) {
		return mHandler.fromJava(jField.get(obj));
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static class Repeated extends FieldSerializer {
		RepeatedHandler rHandler;
		public Repeated(FieldInvoker jField, FieldDescriptor pField, MessageHandler mHandler, RepeatedHandler<?> rHandler) {
			super(jField, pField, mHandler);
			this.rHandler = rHandler;
		}
		public void parse(Object obj, DynamicMessage dynMessage) {
			int len = dynMessage.getRepeatedFieldCount(pField);
			Object data = rHandler.make(len);
			for (int i = 0; i < len; i++) {
				rHandler.add(data, i, mHandler.fromProto(dynMessage.getRepeatedField(pField, i)));
			}
			jField.set(obj, data);
		}
		public void build(DynamicMessage.Builder dynBuilder, Object obj) {
			Object data = jField.get(obj);
			rHandler.each(data, e->dynBuilder.addRepeatedField(pField, mHandler.fromJava(e)));
		}
	}

}

package dev.xframe.utils.proto;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.FieldDescriptor;

public class FieldSchema {

	public static enum Label {
		OPTIONAL(FieldDescriptorProto.Label.LABEL_OPTIONAL),
		REQUIRED(FieldDescriptorProto.Label.LABEL_REQUIRED),
		REPEATED(FieldDescriptorProto.Label.LABEL_REPEATED);
		public final FieldDescriptorProto.Label protoLabel;
		private Label(FieldDescriptorProto.Label protoLabel) {
			this.protoLabel = protoLabel;
		}
	}
	
	public static enum Type {
		DOUBLE(FieldDescriptorProto.Type.TYPE_DOUBLE, FieldDescriptor.Type.DOUBLE),
		FLOAT(FieldDescriptorProto.Type.TYPE_FLOAT, FieldDescriptor.Type.FLOAT),
		INT64(FieldDescriptorProto.Type.TYPE_INT64, FieldDescriptor.Type.INT64),
		UINT64(FieldDescriptorProto.Type.TYPE_UINT64, FieldDescriptor.Type.UINT64),
		INT32(FieldDescriptorProto.Type.TYPE_INT32, FieldDescriptor.Type.INT32),
		FIXED64(FieldDescriptorProto.Type.TYPE_FIXED64, FieldDescriptor.Type.FIXED64),
		FIXED32(FieldDescriptorProto.Type.TYPE_FIXED32, FieldDescriptor.Type.FIXED32),
		BOOL(FieldDescriptorProto.Type.TYPE_BOOL, FieldDescriptor.Type.BOOL),
		STRING(FieldDescriptorProto.Type.TYPE_STRING, FieldDescriptor.Type.STRING),
		GROUP(FieldDescriptorProto.Type.TYPE_GROUP, FieldDescriptor.Type.GROUP),
		MESSAGE(FieldDescriptorProto.Type.TYPE_MESSAGE, FieldDescriptor.Type.MESSAGE),
		BYTES(FieldDescriptorProto.Type.TYPE_BYTES, FieldDescriptor.Type.BYTES),
		UINT32(FieldDescriptorProto.Type.TYPE_UINT32, FieldDescriptor.Type.UINT32),
		ENUM(FieldDescriptorProto.Type.TYPE_ENUM, FieldDescriptor.Type.ENUM),
		SFIXED32(FieldDescriptorProto.Type.TYPE_SFIXED32, FieldDescriptor.Type.SFIXED32),
		SFIXED64(FieldDescriptorProto.Type.TYPE_SFIXED64, FieldDescriptor.Type.SFIXED64),
		SINT32(FieldDescriptorProto.Type.TYPE_SINT32, FieldDescriptor.Type.SINT32),
		SINT64(FieldDescriptorProto.Type.TYPE_SINT64, FieldDescriptor.Type.SINT64);
		
		public final FieldDescriptorProto.Type protoType;
		public final FieldDescriptor.Type descrType;
		private Type(FieldDescriptorProto.Type protoType, FieldDescriptor.Type descrType) {
			this.protoType = protoType;
			this.descrType = descrType;
		}
	}
	
	public static enum JavaType {
		BYTES  (Type.BYTES, byte[].class),
		INT    (Type.INT32, int.class, Integer.class),
		FLOAT  (Type.FLOAT, float.class, Float.class),
		LONG   (Type.INT64, long.class, Long.class),
		DOUBLE (Type.DOUBLE, double.class, Double.class),
		BOOLEAN(Type.BOOL, boolean.class, Boolean.class),
		STRING (Type.STRING, String.class),
		MESSAGE(Type.MESSAGE);
		public final Type pType;
		private final Class<?>[] mapped;
		private JavaType(Type pType, Class<?>... mapped) {
			this.pType = pType;
			this.mapped = mapped;
		}
		public boolean matching(Class<?> c) {
			return Arrays.stream(mapped).filter(m->m.equals(c)).findAny().isPresent();
		}
	}
	
	
	public final FieldInvoker invoker;
	public final Label label;
	public final Type type;
	public final String name;
	public final String tName;
	public final int num;
	public final Class<?> cType;
	public final RepeatedHandler<?> rHandler;
	
	public FieldSchema(FieldInvoker invoker, Label label, String name, Class<?> cType, int num, RepeatedHandler<?> rHandler) {
		this.invoker = invoker;
		this.label = label;
		this.type = getSchemaType(cType);
		this.name = name;
		this.cType = cType;
		this.tName = TypeSerializer.Builder.naming(cType);
		this.num = num;
		this.rHandler = rHandler;
	}
	
	public boolean isMessage() {
		return type == Type.MESSAGE;
	}
	
	@Override
	public String toString() {
		return String.join(" ", label.name().toLowerCase(), type.name().toLowerCase(), name, "=", String.valueOf(num));
	}

	public static FieldSchema of(Field f, int num) {
		return of(FieldInvoker.of(f), f.getGenericType(), f.getName(), num);
	}
	
	/**
	 * using for Object[]
	 * @param type
	 * @param index
	 * @return
	 */
	public static FieldSchema of(java.lang.reflect.Type type, int index) {
	    int num = index+1;
        String name = "val_" + num;
        return of(FieldInvoker.of(index), type, name, num);
	}
	
	public static FieldSchema of(FieldInvoker invoker, java.lang.reflect.Type type, String name, int num) {
		Class<?> c = getRawType(type);
		RepeatedHandler<?> h = null;
		Label label = Label.OPTIONAL;
		if(c.isArray() && !byte[].class.equals(c)) {
			label = Label.REPEATED;
			c = c.getComponentType();
			h = RepeatedHandler.get4Array(c);
		} else if(Collection.class.isAssignableFrom(c)) {
			label = Label.REPEATED;
			h = RepeatedHandler.get4Collection(c);
			c = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
		}
		return new FieldSchema(invoker, label, name, c, num, h);
	}
	
	public static Class<?> getRawType(java.lang.reflect.Type type) {
		return (Class<?>) ((type instanceof Class) ? type : ((ParameterizedType) type).getRawType());
	}
	
	public static Type getSchemaType(Class<?> c) {
		return Arrays.stream(JavaType.values()).filter(j->j.matching(c)).findAny().orElse(JavaType.MESSAGE).pType;
	}
	
}

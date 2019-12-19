package dev.xframe.utils.proto;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;

import dev.xframe.utils.XCaught;
import dev.xframe.utils.XLambda;
import dev.xframe.utils.proto.FieldSchema.Type;

/**
 * match protobuf message
 * @author luzj
 */
public class TypeSerializer {
	
	protected Descriptor typeDescriptor;
	protected FieldSerializer[] fieldSerializers;
	protected Supplier<Object> factory;
	
	public String name() {
		return typeDescriptor.getName();
	}
	
	TypeSerializer(Descriptor typeDescriptor, FieldSerializer[] fieldSerializers, Supplier<Object> factory) {
		this.typeDescriptor = typeDescriptor;
		this.fieldSerializers = fieldSerializers;
		this.factory = factory;
	}

	public <T> T parseFrom(byte[] bytes) {
		try {
			return parseFrom(DynamicMessage.parseFrom(typeDescriptor, bytes));
		} catch (Exception e) {
			return XCaught.throwException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T parseFrom(DynamicMessage dynMessage) {
		Object obj = factory.get();
		for (FieldSerializer fs : fieldSerializers) {
			fs.parse(obj, dynMessage);
		}
		return (T) obj;
	}
	
	public byte[] toByteArray(Object obj) {
		return buildFrom(obj).toByteArray();
	}

	public DynamicMessage buildFrom(Object obj) {
		DynamicMessage.Builder builder = DynamicMessage.newBuilder(typeDescriptor);
		for (FieldSerializer fs : fieldSerializers) {
			fs.build(builder, obj);
		}
		return builder.build();
	}
	
	static class Primitive extends TypeSerializer {
		Primitive(Descriptor typeDescriptor, FieldSerializer[] fieldSerializers, Supplier<Object> factory) {
			super(typeDescriptor, fieldSerializers, factory);
		}
		@Override
		public <T> T parseFrom(DynamicMessage dynMessage) {
			return (T) fieldSerializers[0].getDataFromProto(dynMessage);
		}
	}
	
	static class Builder {
		static Supplier<Object> FACTORY_NIL = ()->null;
		
		String name;
		Supplier<Object> factory;
		FieldSchema[] fields;
		
		Descriptor descr;
		TypeSerializer serializer;
		public Builder(String name, Supplier<Object> factory, FieldSchema[] fields) {
			this.name = name;
			this.factory = factory;
			this.fields = fields;
		}
		
		public DescriptorProto buildMessageProto() {
			DescriptorProto.Builder typeProto = DescriptorProto.newBuilder();
			typeProto.setName(name);
			for (FieldSchema schema : fields) {
				typeProto.addField(buildFieldProto(schema));
			}
			return typeProto.build();
		}
		
		private FieldDescriptorProto buildFieldProto(FieldSchema schema) {
			FieldDescriptorProto.Builder fp = FieldDescriptorProto.newBuilder();
			fp.setLabel(schema.label.protoLabel);
			fp.setName(schema.name);
			fp.setType(schema.type.protoType);
			if(schema.type == Type.MESSAGE)
				fp.setTypeName(schema.tName);
			fp.setNumber(schema.num);
			return fp.build();
		}
		
		public Builder setDescriptor(Descriptor descr) {
			this.descr = descr;
			return this;
		}
		
		public TypeSerializer buildSerializer(Function<Descriptor, Builder> probable) {
			if(serializer == null) {
				serializer = factory == FACTORY_NIL ?
						new TypeSerializer.Primitive(descr, buildFieldSerializers(probable), factory)	:
						new TypeSerializer(descr, buildFieldSerializers(probable), factory);
			}
			return serializer;
		}
		
		private FieldSerializer[] buildFieldSerializers(Function<Descriptor, Builder> probable) {
			return descr.getFields().stream().map(f->buildFieldSerializer(f, probable)).toArray(FieldSerializer[]::new);
		}
		
		private FieldSerializer buildFieldSerializer(FieldDescriptor pField, Function<Descriptor, Builder> probable) {
			FieldSchema schema = fields[pField.getNumber()-1];
			return pField.isRepeated() ?
				new FieldSerializer.Repeated(schema.invoker, pField, buildMessageHandler(pField, schema, probable), schema.rHandler) :
				new FieldSerializer(schema.invoker, pField, buildMessageHandler(pField, schema, probable));
		}

		private MessageHandler buildMessageHandler(FieldDescriptor pField, FieldSchema schema, Function<Descriptor, Builder> probable) {
			return MessageHandler.of(schema, pField, ()->probable.apply(pField.getMessageType()).buildSerializer(probable));
		}
		
		
		/*---pojo---*/
		public static String naming(Class<?> c) {
			return c.getName().replace('.', '_').replace('$', '_');
		}
		public static Builder from(java.lang.reflect.Type type) {
			Class<?> c = FieldSchema.getRawType(type);
			if(FieldSchema.getSchemaType(type) == Type.MESSAGE) {
				Field[] fields = getFields(c);
				FieldSchema[] schemas = IntStream.range(0, fields.length).mapToObj(i->FieldSchema.of(fields[i], i+1)).toArray(FieldSchema[]::new);
				return new Builder(naming(c), XLambda.createByConstructor(c), schemas);
			} else {//primitive
				FieldSchema[] schemas = new FieldSchema[]{FieldSchema.of(FieldInvoker.Primitive, type, "value", 1)};
				return new Builder(naming(c), FACTORY_NIL, schemas);
			}
		}
		private static Field[] getFields(Class<?> c) {
			return getFields0(c, new LinkedHashMap<>()).values().stream().peek(f->f.setAccessible(true)).toArray(Field[]::new);
		}
		private static Map<String, Field> getFields0(Class<?> c, Map<String, Field> m) {
			if(!c.equals(Object.class)) {
				getFields0(c.getSuperclass(), m);
				Arrays.stream(c.getDeclaredFields()).forEach(f->m.putIfAbsent(f.getName(), f));
			}
			return m;
		}
		
		/*---method parameters---*/
		public static String naming(Method m) {
			return naming(m.getDeclaringClass()) + "_" + m.getName();
		}
		public static Builder from(Method m) {
			return null;
		}
	}

}

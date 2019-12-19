package dev.xframe.utils.proto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;

import dev.xframe.utils.XCaught;
import dev.xframe.utils.proto.FieldSchema.Type;

/**
 * match protobuf .proto file
 * @author luzj
 */
public class TypeSerializerSet implements Iterable<TypeSerializer> {
	
	FileDescriptor fileDescriptor;
	
	Map<String, TypeSerializer> serializers = new LinkedHashMap<>();
	
	TypeSerializerSet(FileDescriptor fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}
	
	void put(TypeSerializer serializer) {
		serializers.put(serializer.getName(), serializer);
	}
	
	public TypeSerializer get(Class<?> c) {
		return serializers.get(TypeSerializer.Builder.naming(c));
	}
	
	public TypeSerializer get(String name) {
	    return serializers.get(name);
	}
	
	public String getPackage() {
		return fileDescriptor.getPackage();
	}
	
	public String getName() {
		return fileDescriptor.getName();
	}
	
	@Override
	public Iterator<TypeSerializer> iterator() {
		return serializers.values().iterator();
	}
	
	public static class Builder {
		String packag = "dev.xframe";
		public Builder setPackage(String packag) {
			this.packag = packag;
			return this;
		}
		
		String name;
		public Builder setName(String name) {
			this.name = name;
			return this;
		}
		
		private Map<String, TypeSerializer.Builder> tsBuilders = new HashMap<>();
		private void add0(TypeSerializer.Builder builder) {
			tsBuilders.put(builder.name, builder);
			//adding refs
			Arrays.stream(builder.fields)
				.filter(f->f.type==Type.MESSAGE)
				.filter(f->FieldSchema.getSchemaType(f.cType)==Type.MESSAGE)
				.filter(f->!tsBuilders.containsKey(TypeSerializer.Builder.naming(f.cType)))
				.forEach(f->add0(TypeSerializer.Builder.of(f.cType)));
		}
		
		private TypeSerializer.Builder getBuilder(Descriptor d) {
			return tsBuilders.get(d.getName()).setDescriptor(d);
		}

		public TypeSerializerSet build() {
			try {
				FileDescriptorProto.Builder fileProto = FileDescriptorProto.newBuilder();
				fileProto.setPackage(packag);
				fileProto.setName(name);
				
				tsBuilders.values().forEach(b->fileProto.addMessageType(b.buildMessageProto()));
				
				FileDescriptor fileDescriptor = FileDescriptor.buildFrom(fileProto.build(), new FileDescriptor[0]);
				
				fileDescriptor.getMessageTypes().forEach(d->getBuilder(d).buildSerializer(this::getBuilder));
				
				TypeSerializerSet tss = new TypeSerializerSet(fileDescriptor);
				
				outer.forEach(n->tss.put(tsBuilders.get(n).serializer));
				return tss;
			} catch (Throwable e) {
				return XCaught.throwException(e);
			}
		}
		
		private List<String> outer = new LinkedList<>();
		public Builder add(TypeSerializer.Builder builder) {
			outer.add(builder.name);
			add0(builder);
			return this;
		}
		public Builder add(Class<?> c) {
			return add(TypeSerializer.Builder.of(c));
		}
		public Builder add(String name, FieldSchema... fields) {
		    return add(TypeSerializer.Builder.of(name, fields));
		}
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
	
	public static TypeSerializerSet buildFrom(String name, Class<?>... clazzes) {
		Builder builder = TypeSerializerSet.newBuilder().setName(name);
		Arrays.stream(clazzes).forEach(builder::add);
		return builder.build();
	}

}

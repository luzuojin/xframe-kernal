package dev.xframe.utils.proto;

import java.util.function.Supplier;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;

/**
 * single message <--> object
 * @author luzj
 */
interface MessageHandler {
	
	Object fromProto(Object proto);
	Object fromJava(Object obj);
	
	static class Typed implements MessageHandler {
		TypeSerializer serializer;
		public Typed(TypeSerializer serializer) {
			this.serializer = serializer;
		}
		public Object fromProto(Object proto) {
			return serializer.parseFrom((DynamicMessage) proto);
		}
		public Object fromJava(Object obj) {
			return serializer.buildFrom(obj);
		}
	}
	
	MessageHandler IDENTITY = new MessageHandler() {
		public Object fromProto(Object proto) {
			return proto;
		}
		public Object fromJava(Object obj) {
			return obj;
		}
	};
	
	static MessageHandler of(FieldDescriptor pField, Supplier<TypeSerializer> probable) {
		return pField.getType() == FieldDescriptor.Type.MESSAGE ? new Typed(probable.get()) : IDENTITY;
	}

}

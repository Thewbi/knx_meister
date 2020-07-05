package api.data.serializer;

public interface DataSerializer<T> {

	short[] serialize(T data);

	byte[] serializeToBytes(Object data);

	double deserialize(short[] data);

	double deserializeFromBytes(byte[] data);

}

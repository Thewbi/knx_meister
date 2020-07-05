package object_server.requests;

public class ErrorResponse extends BaseResponse {

	@Override
	public void fromBytes(final byte[] bytes) {
		throw new RuntimeException("Not implemented yet!");
	}

}

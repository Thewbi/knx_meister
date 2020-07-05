package object_server.requests;

public class GetServerItemResponse extends BaseResponse {

	public GetServerItemResponse() {
		setSubService(0x81);
	}

	@Override
	public void fromBytes(final byte[] bytes) {
		throw new RuntimeException("Not implemented yet!");
	}

}

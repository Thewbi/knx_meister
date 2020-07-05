package object_server.requests;

public class GetDescriptionStringRequest extends BaseRequest {

	private static final int MESSAGE_LENGTH = 16;

	private static final int GET_DESCRIPTION_STRING_REQUEST_CODE = 0x04;

	public GetDescriptionStringRequest() {
		setSubService(GET_DESCRIPTION_STRING_REQUEST_CODE);
	}

	@Override
	public void fromBytes(final byte[] bytes) {
		throw new RuntimeException("Not implemented yet!");
	}

	@Override
	public int getMessageLength() {
		return MESSAGE_LENGTH;
	}

}

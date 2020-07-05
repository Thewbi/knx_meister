package object_server.requests;

import java.util.ArrayList;
import java.util.List;

public class GetDatapointDescriptionResponse extends BaseResponse {

	private final List<GetDatapointDescriptionResponseEntry> entryList = new ArrayList<>();

	public GetDatapointDescriptionResponse() {
		setSubService(0x83);
	}

	public List<GetDatapointDescriptionResponseEntry> getEntryList() {
		return entryList;
	}

	@Override
	public int getMessageLength() {
		return RESPONSE_PRELUDE_LENGTH + entryList.size() * 5;
	}

	@Override
	public byte[] getPayload() {

		final byte[] data = new byte[entryList.size() * 5];

		for (int i = 0; i < entryList.size(); i++) {
			System.arraycopy(entryList.get(i).getBytes(), 0, data, i * 5, 5);
		}

		return data;
	}

	@Override
	public void fromBytes(final byte[] bytes) {
		throw new RuntimeException("Not implemented yet!");
	}

}

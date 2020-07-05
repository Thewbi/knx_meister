package object_server.requests.processors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.utils.Utils;
import object_server.api.facade.ErrorCode;
import object_server.requests.BaseRequest;
import object_server.requests.BaseResponse;
import object_server.requests.ErrorResponse;
import object_server.requests.GetServerItemRequest;
import object_server.requests.GetServerItemResponse;

/**
 * <ul>
 * <li>2.1. GetServerItem.Req - Request SubService: 0x01</li>
 * <li>2.2. GetServerItem.Res - Response SubService: 0x81</li>
 * </ul>
 */
public class GetServerItemRequestProcessor extends BaseRequestProcessor {

	private static final Logger LOG = LogManager.getLogger(GetServerItemRequestProcessor.class);

	@Override
	public BaseResponse process(final BaseRequest baseRequest) {

		final GetServerItemResponse getServerItemResponse = new GetServerItemResponse();
		getServerItemResponse.setStart(baseRequest.getStart());
		getServerItemResponse.setMaxAmount(1);
		getServerItemResponse.setPayload(new byte[] { 0x00, 0x09, 0x04, 0x00, 0x04, 0x0E, 0x35 });

		LOG.trace(Utils.integerToStringNoPrefix(getServerItemResponse.getBytes()));

		return getServerItemResponse;
	}

	@SuppressWarnings("unused")
	private BaseResponse sendError(final ErrorCode errorCode) {

		final byte errorCodeAsByte = (byte) (errorCode.getValue() & 0xFF);

		final ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setSubService(BaseRequest.GET_SERVER_ITEM_RESPONSE_CODE);
		errorResponse.setPayload(new byte[] { errorCodeAsByte });

		return errorResponse;
	}

	@Override
	public boolean accept(final BaseRequest baseRequest) {
		return baseRequest instanceof GetServerItemRequest;
	}

}

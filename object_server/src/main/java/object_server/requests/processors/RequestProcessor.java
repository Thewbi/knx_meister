package object_server.requests.processors;

import api.exception.ObjectServerException;
import object_server.requests.BaseRequest;
import object_server.requests.BaseResponse;

public interface RequestProcessor {

	boolean accept(BaseRequest baseRequest);

	BaseResponse process(final BaseRequest baseRequest) throws ObjectServerException;

}

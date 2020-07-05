package object_server.requests.processors;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.data.serializer.DataSerializer;
import api.exception.ObjectServerException;
import api.project.KNXDatapointSubtype;
import api.project.KNXProject;
import common.utils.KNXProjectUtils;
import object_server.requests.BaseRequest;
import object_server.requests.BaseResponse;
import object_server.requests.ErrorResponse;
import object_server.requests.SetDatapointValueRequest;
import object_server.requests.SetDatapointValueRequestEntry;

public class SetDatapointValueRequestProcessor extends BaseRequestProcessor {

	private static final Logger LOG = LogManager.getLogger(SetDatapointValueRequestProcessor.class);

	private final Map<String, DataSerializer<Object>> dataSerializerMap = new HashMap<>();

	@Override
	public BaseResponse process(final BaseRequest baseRequest) throws ObjectServerException {

		final SetDatapointValueRequest setDatapointValueRequest = (SetDatapointValueRequest) baseRequest;

		LOG.info(setDatapointValueRequest);

		final int start = baseRequest.getStart();
		final int maxAmount = baseRequest.getMaxAmount();

		if (maxAmount != 1) {
			final String msg = "maxAmount is not 1! Not implemented yet!";
			LOG.error(msg);
			throw new RuntimeException(msg);
		}

		final SetDatapointValueRequestEntry setDatapointValueRequestEntry = setDatapointValueRequest.getEntries()
				.get(0);

		final KNXProject knxProject = getKnxProject();
		knxProject.getDeviceInstances().get(0);

//		final KNXGroupAddress knxGroupAddress = KNXProjectUtils.retrieveGroupAddress(knxProject, datapointId);
		final KNXDatapointSubtype knxDatapointSubtype = KNXProjectUtils.retrieveDataPointSubType(knxProject,
				setDatapointValueRequestEntry.getDatapointId());

		// TODO: find converter for knxDatapointSubtype
		final DataSerializer<Object> dataSerializer = dataSerializerMap.get(knxDatapointSubtype.getFormat());

		final double deserializeFromBytes = dataSerializer
				.deserializeFromBytes(setDatapointValueRequestEntry.getValueAsBytes());

		final double oldValue;
		if (knxProject.getValueMap().containsKey(setDatapointValueRequestEntry.getDatapointId())) {
			oldValue = (double) knxProject.getValueMap().get(setDatapointValueRequestEntry.getDatapointId());
			LOG.info("OldValue=" + oldValue + " NewValue=" + deserializeFromBytes);
		} else {
			LOG.info("OldValue=n/a NewValue=" + deserializeFromBytes);
		}

		// TODO: write the new value into the value map of the KNXComObject
		knxProject.getValueMap().put(setDatapointValueRequestEntry.getDatapointId(), deserializeFromBytes);

		final ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setSubService(SetDatapointValueRequest.SET_DATAPOINT_VALUE_RESPONSE_CODE);
		errorResponse.setStart(start);
		errorResponse.setMaxAmount(maxAmount);
		errorResponse.setPayload(new byte[] { 0x00 });

		return errorResponse;
	}

	@Override
	public boolean accept(final BaseRequest baseRequest) {
		return baseRequest instanceof SetDatapointValueRequest;
	}

	public Map<String, DataSerializer<Object>> getDataSerializerMap() {
		return dataSerializerMap;
	}

}

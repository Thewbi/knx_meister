package object_server.requests.processors;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.data.serializer.DataSerializer;
import api.exception.ObjectServerException;
import api.project.KNXComObject;
import api.project.KNXDatapointSubtype;
import api.project.KNXProject;
import common.utils.KNXProjectUtils;
import object_server.requests.BaseRequest;
import object_server.requests.BaseResponse;
import object_server.requests.ErrorResponse;
import object_server.requests.SetDatapointValueRequest;

/**
 * SetDatapointValue.Req - 0x06<br />
 * <br />
 * SetDatapointValue.Res - 0x86
 */
public class SetDatapointValueRequestProcessor extends BaseRequestProcessor {

	private static final int ERROR_CODE_SUCCESS = 0x00;

	private static final Logger LOG = LogManager.getLogger(SetDatapointValueRequestProcessor.class);

	private Map<String, DataSerializer<Object>> dataSerializerMap;

	@Override
	public BaseResponse process(final BaseRequest baseRequest) throws ObjectServerException {

		final SetDatapointValueRequest setDatapointValueRequest = (SetDatapointValueRequest) baseRequest;

		final int start = baseRequest.getStart();
		final int maxAmount = baseRequest.getMaxAmount();

		if (maxAmount != 1) {

			final String msg = "maxAmount is not 1! Not implemented yet!";
			LOG.error(msg);
			throw new RuntimeException(msg);
		}

		// over all entries in the request
		setDatapointValueRequest.getEntries().stream().forEach(entry -> {

			boolean skip = false;
			boolean setValue = false;
			boolean sendValue = false;
			boolean readNewValue = false;
			boolean clearDatapointTransmissionState = false;

			// first decode the command byte which determines which actions are requested by
			// the client
			final int datapointCommand = entry.getCommand() & 0x0F;
			switch (datapointCommand) {

			case 0:
				// no command;
				skip = true;
				break;

			case 1:
				setValue = true;
				break;

			case 2:
				sendValue = true;
				break;

			case 3:
				setValue = true;
				sendValue = true;
				break;

			case 4:
				readNewValue = true;
				break;

			case 5:
				clearDatapointTransmissionState = true;
				break;

			default:
				throw new RuntimeException("Unknown datapoint command value: " + datapointCommand);
			}

			LOG.info("skip: {}, setValue: {}, sendValue: {}, readNewValue: {}, clearDatapointTransmissionState: {}",
					skip, setValue, sendValue, readNewValue, clearDatapointTransmissionState);

			// abort and execute no actions at all if the client decided to perform no
			// actions
			if (skip) {
				return;
			}

			// unknown operations!!!! learn about those!!!!
			if (readNewValue) {
				throw new RuntimeException("I do not know what 'readNewValue' means!");
			}
			if (clearDatapointTransmissionState) {
				throw new RuntimeException("I do not know what 'clearDatapointTransmissionState' means!");
			}

			final KNXProject knxProject = getKnxProject();

			final int deviceIndex = 0;
			final KNXDatapointSubtype knxDatapointSubtype = KNXProjectUtils.retrieveDataPointSubType(knxProject,
					deviceIndex, entry.getDatapointId());

			final Optional<KNXComObject> comObject = KNXProjectUtils.retrieveComObjectByDatapointId(knxProject,
					deviceIndex, entry.getDatapointId());

			// find converter for knxDatapointSubtype
			final DataSerializer<Object> dataSerializer = dataSerializerMap.get(knxDatapointSubtype.getFormat());

			final double deserializeFromBytes = dataSerializer.deserializeFromBytes(entry.getValueAsBytes());

			// DEBUG - print old and new value
			final Object oldValue;
			if (knxProject.getValueMap().containsKey(entry.getDatapointId())) {
				final Object object = knxProject.getValueMap().get(entry.getDatapointId());
				if (object == null) {
					LOG.info("DP: " + entry.getDatapointId() + " '" + comObject.get().getText()
							+ "' OldValue=n/a NewValue=" + deserializeFromBytes);
				} else {
					oldValue = object;
					LOG.info("DP: " + entry.getDatapointId() + " '" + comObject.get().getText() + "' OldValue="
							+ oldValue + " NewValue=" + deserializeFromBytes);
				}
			} else {
				LOG.info("DP: " + entry.getDatapointId() + " '" + comObject.get().getText() + "' OldValue=n/a NewValue="
						+ deserializeFromBytes);
			}

			if (setValue) {
				// write the new value into the value map of the KNXComObject
				knxProject.getValueMap().put(entry.getDatapointId(), deserializeFromBytes);
			}

			if (sendValue) {
				// TODO: send value to the KNX bus!!!!!!
			}
		});

		// return response
		//
		// The response is always defined as an error.
		// A error with ErrorCode (= payload) of 0 means NoError and hence signals
		// success!
		final ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setSubService(SetDatapointValueRequest.SET_DATAPOINT_VALUE_RESPONSE_CODE);
		errorResponse.setStart(start);
		errorResponse.setMaxAmount(maxAmount);
		errorResponse.setPayload(new byte[] { ERROR_CODE_SUCCESS });

		return errorResponse;
	}

	@Override
	public boolean accept(final BaseRequest baseRequest) {
		return baseRequest instanceof SetDatapointValueRequest;
	}

	public Map<String, DataSerializer<Object>> getDataSerializerMap() {
		return dataSerializerMap;
	}

	public void setDataSerializerMap(final Map<String, DataSerializer<Object>> dataSerializerMap) {
		this.dataSerializerMap = dataSerializerMap;
	}

}

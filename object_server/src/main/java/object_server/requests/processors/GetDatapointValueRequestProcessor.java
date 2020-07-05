package object_server.requests.processors;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.conversion.Converter;
import api.exception.ObjectServerException;
import api.project.KNXComObject;
import common.utils.KNXProjectUtils;
import object_server.conversion.ComObjectValueConverter;
import object_server.requests.BaseRequest;
import object_server.requests.BaseResponse;
import object_server.requests.GetDatapointValueRequest;
import object_server.requests.GetDatapointValueResponse;

public class GetDatapointValueRequestProcessor extends BaseRequestProcessor {

	private static final Logger LOG = LogManager.getLogger(GetDatapointValueRequestProcessor.class);

	private final Converter<KNXComObject, byte[]> objectServerValueConverter = new ComObjectValueConverter();

	@Override
	public BaseResponse process(final BaseRequest baseRequest) throws ObjectServerException {

		final GetDatapointValueRequest getDatapointValueRequest = (GetDatapointValueRequest) baseRequest;

		LOG.info(getDatapointValueRequest);

		final int start = baseRequest.getStart();
		final int maxAmount = baseRequest.getMaxAmount();

		if (maxAmount != 1) {
			final String msg = "maxAmount is not 1! Not implemented yet!";
			LOG.error(msg);
			throw new RuntimeException(msg);
		}

		final int dataPointId = start;

		// TODO: how do I know which device to use?
//		final KNXDeviceInstance knxDeviceInstance = getKnxProject().getDeviceInstances().get(0);
		final Optional<KNXComObject> knxComObjectOptional = KNXProjectUtils
				.retrieveComObjectByDatapointId(getKnxProject(), dataPointId);

		if (!knxComObjectOptional.isPresent()) {
			final String msg = "Could not find ComObject for dataPointId = " + dataPointId;
			LOG.error(msg);
			throw new RuntimeException(msg);
		}

		final KNXComObject knxComObject = knxComObjectOptional.get();

		((ComObjectValueConverter) objectServerValueConverter).setKnxProject(getKnxProject());
		final byte[] data = objectServerValueConverter.convert(knxComObject);

		final GetDatapointValueResponse getDatapointValueResponse = new GetDatapointValueResponse();
		getDatapointValueResponse.setStart(start);
		getDatapointValueResponse.setMaxAmount(maxAmount);

		// 2 byte datapoint id
		getDatapointValueResponse.setDataPointId(knxComObject.getNumber());

		// 1 byte state
		getDatapointValueResponse.setState(1);

		// 1 length
		getDatapointValueResponse.setLength(data.length);

		// 1 - 14 byte data point value. value length has to be stored in the previous
		// value length.
//		getDatapointValueResponse.setValue(new byte[] { 50 });
		getDatapointValueResponse.setValue(data);

		return getDatapointValueResponse;
	}

	@Override
	public boolean accept(final BaseRequest baseRequest) {
		return baseRequest instanceof GetDatapointValueRequest;
	}

}

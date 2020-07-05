package object_server.conversion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.conversion.Converter;
import api.exception.ObjectServerException;
import api.project.KNXComObject;
import api.project.KNXDatapointType;
import api.project.KNXProject;
import common.utils.Utils;

public class ComObjectValueConverter implements Converter<KNXComObject, byte[]> {

	private static final Logger LOG = LogManager.getLogger(ComObjectValueConverter.class);

	private KNXProject knxProject;

	@Override
	public byte[] convert(final KNXComObject knxComObject) throws ObjectServerException {

		if (knxProject == null) {
			throw new ObjectServerException("knxProject is null!");
		}

		Object value = null;
		if (!knxProject.getValueMap().containsKey(knxComObject.getNumber())) {
			knxProject.getValueMap().put(knxComObject.getNumber(), 0);
		}
		value = knxProject.getValueMap().get(knxComObject.getNumber());

		final KNXDatapointType dataPointType = knxComObject.getDataPointType(knxProject);
		switch (dataPointType.getId()) {

		// 1-Bit
		case "DPT-1":
			return new byte[] { (byte) (int) value };

		// unsigned byte
		case "DPT-5":
			return new byte[] { (byte) (int) value };

		// 2-Bit Octet Float
		case "DPT-9":
			final Integer valueAsInteger = (Integer) value;
			return Utils.shortToByteArray(valueAsInteger.shortValue());

		default:
			final String msg = "Unknown knxDatapointType: " + dataPointType.getId();
			LOG.error(msg);
			throw new ObjectServerException(msg);
		}

	}

	public KNXProject getKnxProject() {
		return knxProject;
	}

	public void setKnxProject(final KNXProject knxProject) {
		this.knxProject = knxProject;
	}

}

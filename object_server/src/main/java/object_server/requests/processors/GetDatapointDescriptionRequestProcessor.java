package object_server.requests.processors;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.exception.ObjectServerException;
import api.factory.exception.FactoryException;
import api.project.KNXComObject;
import api.project.KNXDatapointType;
import api.project.KNXProject;
import common.utils.KNXProjectUtils;
import object_server.requests.BaseRequest;
import object_server.requests.BaseResponse;
import object_server.requests.DefaultDatapointConfigFactory;
import object_server.requests.GetDatapointDescriptionRequest;
import object_server.requests.GetDatapointDescriptionResponse;
import object_server.requests.GetDatapointDescriptionResponseEntry;

public class GetDatapointDescriptionRequestProcessor extends BaseRequestProcessor {

	private static final Logger LOG = LogManager.getLogger(GetDatapointDescriptionRequestProcessor.class);

	private final DefaultDatapointConfigFactory defaultDatapointConfigFactory = new DefaultDatapointConfigFactory();

	@Override
	public BaseResponse process(final BaseRequest baseRequest) throws ObjectServerException {

		final GetDatapointDescriptionRequest getDatapointDescriptionRequest = (GetDatapointDescriptionRequest) baseRequest;

		LOG.info("getDatapointDescriptionRequest " + getDatapointDescriptionRequest);

		final GetDatapointDescriptionResponse getDatapointDescriptionResponse = new GetDatapointDescriptionResponse();

		final int start = baseRequest.getStart();
		final int maxAmount = baseRequest.getMaxAmount();

		getDatapointDescriptionResponse.setStart(start);
		getDatapointDescriptionResponse.setMaxAmount(maxAmount);

		if (maxAmount > 50) {
			final String msg = "maxAmount = " + maxAmount + "! Too many devices requested! Not answering request!";
			LOG.info(msg);
			throw new ObjectServerException(msg);
		}

		for (int dataPointId = start; dataPointId < start + maxAmount; dataPointId++) {

			LOG.info("dataPointId = " + dataPointId);
			addEntry(getDatapointDescriptionResponse, dataPointId, getKnxProject());
		}

		return getDatapointDescriptionResponse;
	}

	/**
	 * https://stackoverflow.com/questions/9321553/java-convert-integer-to-hex-integer
	 *
	 * reinterpret the integer to a integer that encoded in hex has the same
	 * representation as the original integer.
	 *
	 * e.g. 20d becomes 32d because 32d in hex is 0x20 and looks like the initial
	 * value.<br />
	 * <br />
	 *
	 * e.g. 59d becomes 89d because 89d = 0x59<br />
	 * <br />
	 *
	 * e.g. 133d becomes 307d because 0x133 = 307d
	 *
	 * @param n
	 * @return
	 */
	public static int convert(final int n) {
		return Integer.valueOf(String.valueOf(n), 16);
	}

	private void addEntry(final GetDatapointDescriptionResponse getDatapointDescriptionResponse, final int dataPointId,
			final KNXProject knxProject) throws ObjectServerException {

		try {

			// I do not know what is going on, but the dataPointId is a BCD representation
			// of a hex number!
			// e.g. dataPointId 59 actually means 0x59 which is the datapoint decimal id 89
//			final int dataPointAsInt = convert(dataPointId);
			final int dataPointAsInt = dataPointId;

			final GetDatapointDescriptionResponseEntry entry = new GetDatapointDescriptionResponseEntry();

			// 2 byte datapoint id
			entry.setDatapointId(dataPointAsInt);

			// 1 byte value type
			final List<KNXComObject> knxComObjects = KNXProjectUtils.retrieveComObjectListByDatapointId(getKnxProject(),
					dataPointAsInt);

			if (CollectionUtils.isEmpty(knxComObjects)) {
				final String msg = "No KNXComObject with number " + dataPointAsInt + " available!";
				LOG.error(msg);
				return;
			}

			if (knxComObjects.size() > 1) {
				final String msg = "more than 1 knxComObject";
				LOG.error(msg);
				throw new ObjectServerException(msg);
			}

			final KNXComObject knxComObject = knxComObjects.get(0);
			entry.setValueType(getValueType(knxComObject, knxProject));

			// set 1 byte config flags
			entry.setConfigFlags(retrieveConfigFlags());

			// 1 byte data point type
			entry.setDataPointType(getDataPointType(knxComObject, knxProject));

			getDatapointDescriptionResponse.getEntryList().add(entry);

		} catch (final FactoryException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * <pre>
	 * Type Point Type Code	Value size
	 *	0						Datapoint disabled
	 *	1						DPT 1 (1 Bit, Boolean)
	 *  2						DPT 2 (2 Bit, Control)
	 *  3						DPT 3 (4 Bit, Dimming, Blinds)
	 *  4						DPT 4 (8 Bit, Character Set)
	 *  5						DPT 5 (8 Bit, Unsigned Value)
	 *  6						DPT 6 (8 Bit, Signed Value)
	 *  7						DPT 7 (2 Byte, Unsigned Value)
	 *  8						DPT 8 (2 Byte, Signed Value)
	 *  9						DPT 9 (2 Byte, Float Value)
	 *  10						DPT 10 (3 Byte, Time)
	 *  11						DPT 11 (3 Byte, Date)
	 *  12						DPT 12 (4 Byte, Unsigned Value)
	 *  13						DPT 13 (4 Byte, Signed Value)
	 *  14						DPT 14 (4 Byte, Float Value)
	 *  15						DPT 15 (4 Byte, Access)
	 *  16						DPT 16 (14 Byte, String)
	 *  17						DPT 17 (1 Byte, Scene Number)
	 *  18						DPT 18 (1 Byte, Scene Control)
	 *  19..254					Reserved
	 *  255						Unknown DPT
	 * </pre>
	 *
	 * @param knxComObject
	 * @param knxProject
	 * @return
	 * @throws ObjectServerException
	 */
	private int getDataPointType(final KNXComObject knxComObject, final KNXProject knxProject)
			throws ObjectServerException {

		final KNXDatapointType dataPointType = knxComObject.getDataPointType(knxProject);
		if (dataPointType == null) {
			return -1;
		}

		switch (dataPointType.getId()) {

		case "DPT-1":
			return 1;

		case "DPT-2":
			return 2;

		case "DPT-3":
			return 3;

		case "DPT-4":
			return 4;

		case "DPT-5":
			return 5;

		case "DPT-6":
			return 6;

		case "DPT-7":
			return 7;

		case "DPT-8":
			return 8;

		case "DPT-9":
			return 9;

		case "DPT-10":
			return 10;

		case "DPT-11":
			return 11;

		case "DPT-12":
			return 12;

		case "DPT-13":
			return 13;

		case "DPT-14":
			return 14;

		case "DPT-15":
			return 15;

		case "DPT-16":
			return 16;

		case "DPT-17":
			return 17;

		case "DPT-18":
			return 18;

		default:
			final String msg = "Unknown knxDatapointType: " + dataPointType.getId();
			throw new ObjectServerException(msg);
		}
	}

	/**
	 * <pre>
	 * Type code, 	Value size
	 * 0			1 bit
	 * 1			2 bits
	 * 2			3 bits
	 * 3			4 bits
	 * 4			5 bits
	 * 5			6 bits
	 * 6			7 bits
	 * 7			1 byte
	 * 8			2 bytes
	 * 9			3 bytes
	 * 10			4 bytes
	 * 11			6 bytes
	 * 12			8 bytes
	 * 13			10 bytes
	 * 14			14 bytes
	 * </pre>
	 *
	 * @param knxComObject
	 * @param knxProject
	 * @return
	 * @throws ObjectServerException
	 */
	private int getValueType(final KNXComObject knxComObject, final KNXProject knxProject)
			throws ObjectServerException {

		final KNXDatapointType dataPointType = knxComObject.getDataPointType(knxProject);
		if (dataPointType == null) {
			final String msg = "KNXComObject " + knxComObject.toString() + " does not have a datapoint type!";
			LOG.error(msg);

			return -1;
		}

		switch (dataPointType.getId()) {

		case "DPT-1":
			return 0;

		case "DPT-5":
			return 7;

		case "DPT-9":
			return 8;

		default:
			final String msg = "Unknown knxDatapointType: " + dataPointType.getId();
			LOG.error(msg);
			throw new ObjectServerException(msg);
		}
	}

	private int retrieveConfigFlags() throws FactoryException {

		final int transmitPriority = 3;
		final int datapointCommunication = 1;
		final int readFromBus = 1;
		final int writeFromBus = 0;
		final int readOnInit = 1;
		final int transmitToBus = 1;
		final int updateOnResponse = 1;

		return defaultDatapointConfigFactory.create(transmitPriority, datapointCommunication, readFromBus, writeFromBus,
				readOnInit, transmitToBus, updateOnResponse);
	}

	@Override
	public boolean accept(final BaseRequest baseRequest) {
		return baseRequest instanceof GetDatapointDescriptionRequest;
	}

}

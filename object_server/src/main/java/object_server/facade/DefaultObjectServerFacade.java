package object_server.facade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.exception.ObjectServerException;
import api.project.KNXProject;
import common.packets.KNXConnectionHeader;
import common.packets.KNXHeader;
import common.utils.Utils;
import object_server.api.facade.Datapoint;
import object_server.api.facade.ErrorCode;
import object_server.api.facade.ObjectServerFacade;
import object_server.api.service.ObjectServerConnectionService;
import object_server.conversion.ComObjectValueConverter;
import object_server.requests.GetDatapointDescriptionRequest;
import object_server.requests.GetDescriptionStringRequest;
import object_server.requests.GetServerItemRequest;

public class DefaultObjectServerFacade implements ObjectServerFacade {

	private static final Logger LOG = LogManager.getLogger(ComObjectValueConverter.class);

	private static final int BUFFER_SIZE = 1024;

	private ObjectServerConnectionService objectServerConnectionService;

	private KNXProject knxProject;

	@Override
	public void getServerItemRequest(final int startDataPoint, final int maxNumberOfDataPoints)
			throws IOException, ObjectServerException {

		if (startDataPoint < 1) {
			throw new IllegalArgumentException("Parameter startDataPoint has to be greater than or equal to 1!");
		}
		if (objectServerConnectionService.isNotConnected()) {
			throw new IllegalStateException("The connection service is not connected!");
		}

		final GetServerItemRequest getServerItemRequest = new GetServerItemRequest();
		getServerItemRequest.setStart(startDataPoint);
		getServerItemRequest.setMaxAmount(maxNumberOfDataPoints);
		getServerItemRequest.setKnxProject(knxProject);

		final byte[] requestBytes = getServerItemRequest.getBytes();

		LOG.info(Utils.integerToStringNoPrefix(requestBytes));

		objectServerConnectionService.send(requestBytes);

		final int knxHeaderLength = 6;
		final int knxConnectionHeaderLength = 4;
		final int responsePreludeLength = 6;

		final byte buffer[] = new byte[BUFFER_SIZE];
		objectServerConnectionService.read(buffer);

		LOG.info(Utils.integerToStringNoPrefix(buffer));

		final KNXHeader knxHeader = new KNXHeader();
		knxHeader.fromBytes(buffer, 0);

		final KNXConnectionHeader knxConnectionHeader = new KNXConnectionHeader();
		knxConnectionHeader.fromBytes(buffer, knxHeaderLength);

		final boolean bigEndian = true;
		final int startItem = Utils.bytesToUnsignedShort(buffer[12 + 0], buffer[12 + 1], bigEndian);
		final int numberOfItems = Utils.bytesToUnsignedShort(buffer[12 + 2], buffer[12 + 3], bigEndian);

		if ((startItem == 0) && (numberOfItems == 0)) {

			final int errorCode = buffer[12 + 4];
			LOG.info(ErrorCode.fromInt(errorCode));

			throw new ObjectServerException("ErrorCode " + errorCode + " == " + ErrorCode.fromInt(errorCode).name());
		}

		final int payloadOffset = knxHeaderLength + knxConnectionHeaderLength + responsePreludeLength;
		final int sizeOfDatapointDescriptor = 5;

		int numberOfDatapointDescriptors = knxHeader.getTotalLength() - payloadOffset;
		numberOfDatapointDescriptors = numberOfDatapointDescriptors / sizeOfDatapointDescriptor;

		final List<Datapoint> dataPoints = new ArrayList<>();
		for (int i = 0; i < numberOfDatapointDescriptors; i++) {

			final Datapoint dataPoint = new Datapoint();
			dataPoints.add(dataPoint);

			dataPoint.fromBytes(buffer, payloadOffset + i * sizeOfDatapointDescriptor);
		}
	}

	@Override
	public List<Datapoint> getDatapointDescriptions(final int startDataPoint, final int maxNumberOfDataPoints)
			throws IOException, ObjectServerException {

		if (startDataPoint < 1) {
			throw new IllegalArgumentException("Parameter startDataPoint has to be greater than or equal to 1!");
		}
		if (objectServerConnectionService.isNotConnected()) {
			throw new IllegalStateException("The connection service is not connected!");
		}

		final GetDatapointDescriptionRequest getDatapointDescriptionRequest = new GetDatapointDescriptionRequest();
		getDatapointDescriptionRequest.setStart(startDataPoint);
		getDatapointDescriptionRequest.setMaxAmount(maxNumberOfDataPoints);
		getDatapointDescriptionRequest.setKnxProject(knxProject);

		objectServerConnectionService.send(getDatapointDescriptionRequest.getBytes());

		final int knxHeaderLength = 6;
		final int knxConnectionHeaderLength = 4;
		final int responsePreludeLength = 6;

		final byte buffer[] = new byte[BUFFER_SIZE];
		objectServerConnectionService.read(buffer);

		final KNXHeader knxHeader = new KNXHeader();
		knxHeader.fromBytes(buffer, 0);

		final KNXConnectionHeader knxConnectionHeader = new KNXConnectionHeader();
		knxConnectionHeader.fromBytes(buffer, knxHeaderLength);

		final boolean bigEndian = true;
		final int startItem = Utils.bytesToUnsignedShort(buffer[12 + 0], buffer[12 + 1], bigEndian);
		final int numberOfItems = Utils.bytesToUnsignedShort(buffer[12 + 2], buffer[12 + 3], bigEndian);

		if ((startItem == 0) && (numberOfItems == 0)) {

			final int errorCode = buffer[12 + 4];
			LOG.info(ErrorCode.fromInt(errorCode));

			throw new ObjectServerException("ErrorCode " + errorCode + " == " + ErrorCode.fromInt(errorCode).name());
		}

		final int payloadOffset = knxHeaderLength + knxConnectionHeaderLength + responsePreludeLength;
		final int sizeOfDatapointDescriptor = 5;

		int numberOfDatapointDescriptors = knxHeader.getTotalLength() - payloadOffset;
		numberOfDatapointDescriptors = numberOfDatapointDescriptors / sizeOfDatapointDescriptor;

		final List<Datapoint> dataPoints = new ArrayList<>();
		for (int i = 0; i < numberOfDatapointDescriptors; i++) {

			final Datapoint dataPoint = new Datapoint();
			dataPoints.add(dataPoint);

			dataPoint.fromBytes(buffer, payloadOffset + i * sizeOfDatapointDescriptor);
		}

		return dataPoints;
	}

	@Override
	public List<String> getDescriptionString(final int startDataPoint, final int maxNumberOfDataPoints)
			throws IOException, ObjectServerException {

		if (startDataPoint < 1) {
			throw new IllegalArgumentException("Parameter startDataPoint has to be greater than or equal to 1!");
		}
		if (objectServerConnectionService.isNotConnected()) {
			throw new IllegalStateException("The connection service is not connected!");
		}

		final GetDescriptionStringRequest getDescriptionStringRequest = new GetDescriptionStringRequest();
		getDescriptionStringRequest.setStart(startDataPoint);
		getDescriptionStringRequest.setMaxAmount(maxNumberOfDataPoints);
		getDescriptionStringRequest.setKnxProject(knxProject);

		objectServerConnectionService.send(getDescriptionStringRequest.getBytes());

		final int knxHeaderLength = 6;
		final int knxConnectionHeaderLength = 4;
		final int responsePreludeLength = 6;

		final byte buffer[] = new byte[BUFFER_SIZE];
		objectServerConnectionService.read(buffer);

		final KNXHeader knxHeader = new KNXHeader();
		knxHeader.fromBytes(buffer, 0);

		final KNXConnectionHeader knxConnectionHeader = new KNXConnectionHeader();
		knxConnectionHeader.fromBytes(buffer, knxHeaderLength);

		final boolean bigEndian = true;
		final int startItem = Utils.bytesToUnsignedShort(buffer[12 + 0], buffer[12 + 1], bigEndian);
		final int numberOfItems = Utils.bytesToUnsignedShort(buffer[12 + 2], buffer[12 + 3], bigEndian);

		if ((startItem == 0) && (numberOfItems == 0)) {

			final int errorCode = buffer[12 + 4];
			LOG.info(ErrorCode.fromInt(errorCode));

			throw new ObjectServerException("ErrorCode " + errorCode + " == " + ErrorCode.fromInt(errorCode).name());
		}

		final int payloadOffset = knxHeaderLength + knxConnectionHeaderLength + responsePreludeLength;

		final int numberOfStrings = Utils.bytesToUnsignedShort(buffer[payloadOffset + 0], buffer[payloadOffset + 1],
				bigEndian);

		int index = payloadOffset + 2;
		final List<String> descriptions = new ArrayList<>();
		for (int i = 0; i < numberOfStrings; i++) {

			// read the length of the following string
			final int stringLen = Utils.bytesToUnsignedShort(buffer[index + 0], buffer[index + 1], bigEndian);
			index += 2;

			// read the string
			final String data = new String(buffer, index, stringLen);
			descriptions.add(data);

			index += stringLen;
		}

		return descriptions;
	}

	public void setObjectServerConnectionService(final ObjectServerConnectionService objectServerConnectionService) {
		this.objectServerConnectionService = objectServerConnectionService;
	}

	public KNXProject getKnxProject() {
		return knxProject;
	}

	public void setKnxProject(final KNXProject knxProject) {
		this.knxProject = knxProject;
	}

}

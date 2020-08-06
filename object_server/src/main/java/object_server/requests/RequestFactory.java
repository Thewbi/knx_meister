package object_server.requests;

import api.factory.Factory;
import api.factory.exception.FactoryException;
import api.project.KNXProject;
import common.packets.KNXConnectionHeader;
import common.packets.KNXHeader;

public class RequestFactory implements Factory<BaseRequest> {

	private KNXProject knxProject;

	/**
	 * args = {subService, bytes, knxHeader, knxConnectionHeader} in that order
	 *
	 * @throws FactoryException
	 */
	@Override
	public BaseRequest create(final Object... args) throws FactoryException {

		final int subService = (int) args[0];
		final byte[] bytes = (byte[]) args[1];
		final KNXHeader knxHeader = (KNXHeader) args[2];
		final KNXConnectionHeader knxConnectionHeader = (KNXConnectionHeader) args[3];

		switch (subService) {

		case GetServerItemRequest.GET_SERVER_ITEM_REQUEST_CODE:

			final GetServerItemRequest getServerItemRequest = new GetServerItemRequest();
			getServerItemRequest.setPayload(bytes.clone());
			getServerItemRequest.setKnxHeader(knxHeader);
			getServerItemRequest.setKnxConnectionHeader(knxConnectionHeader);
			getServerItemRequest.setKnxProject(knxProject);
			getServerItemRequest.fromBytes(bytes);

			return getServerItemRequest;

		case GetDatapointDescriptionRequest.GET_DATAPOINT_DESCRIPTION_REQUEST_CODE:

			final GetDatapointDescriptionRequest getDatapointDescriptionRequest = new GetDatapointDescriptionRequest();
			getDatapointDescriptionRequest.setPayload(bytes.clone());
			getDatapointDescriptionRequest.setKnxHeader(knxHeader);
			getDatapointDescriptionRequest.setKnxConnectionHeader(knxConnectionHeader);
			getDatapointDescriptionRequest.setKnxProject(knxProject);
			getDatapointDescriptionRequest.fromBytes(bytes);

			return getDatapointDescriptionRequest;

		case GetDatapointValueRequest.GET_DATAPOINT_VALUE_REQUEST_CODE:

			final GetDatapointValueRequest getDatapointValueRequest = new GetDatapointValueRequest();
			getDatapointValueRequest.setPayload(bytes.clone());
			getDatapointValueRequest.setKnxHeader(knxHeader);
			getDatapointValueRequest.setKnxConnectionHeader(knxConnectionHeader);
			getDatapointValueRequest.setKnxProject(knxProject);
			getDatapointValueRequest.fromBytes(bytes);

			return getDatapointValueRequest;

		case SetDatapointValueRequest.SET_DATAPOINT_VALUE_REQUEST_CODE:

			final SetDatapointValueRequest setDatapointValueRequest = new SetDatapointValueRequest();
			setDatapointValueRequest.setPayload(bytes.clone());
			setDatapointValueRequest.setKnxHeader(knxHeader);
			setDatapointValueRequest.setKnxConnectionHeader(knxConnectionHeader);
			setDatapointValueRequest.setKnxProject(knxProject);
			setDatapointValueRequest.fromBytes(bytes);

			return setDatapointValueRequest;

		default:
			throw new FactoryException("Request SubService " + subService + " not implemented yet!");
		}
	}

	public KNXProject getKnxProject() {
		return knxProject;
	}

	public void setKnxProject(final KNXProject knxProject) {
		this.knxProject = knxProject;
	}

}

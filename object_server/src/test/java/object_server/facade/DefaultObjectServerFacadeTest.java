package object_server.facade;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.jupiter.api.Test;

import api.exception.ObjectServerException;
import api.project.KNXProject;
import object_server.api.facade.Datapoint;
import object_server.service.DefaultObjectServerConnectionService;

public class DefaultObjectServerFacadeTest {

	private static final String IP_ADDRESS = "192.168.0.241";
//	private static final String IP_ADDRESS = "192.168.2.220";
//	private static final String IP_ADDRESS = "172.17.200.64";

	private static final int PORT = 12004;

	private final KNXProject knxProject = new KNXProject();

	@Test
	public void testGetServerItemRequest() throws UnknownHostException, IOException, ObjectServerException {

		// Arrange

//		final int startDataPoint = 1;
//		final int maxNumberOfDataPoints = 255;

		final int startDataPoint = 9;
		final int maxNumberOfDataPoints = 1;

		final DefaultObjectServerConnectionService defaultObjectServerConnectionService = new DefaultObjectServerConnectionService();

		final DefaultObjectServerFacade defaultObjectServerFacade = new DefaultObjectServerFacade();
		defaultObjectServerFacade.setKnxProject(knxProject);
		defaultObjectServerFacade.setObjectServerConnectionService(defaultObjectServerConnectionService);

		// Act

		defaultObjectServerConnectionService.connect(IP_ADDRESS, PORT);
//		final List<Datapoint> datapointDescriptions =
		defaultObjectServerFacade.getServerItemRequest(startDataPoint, maxNumberOfDataPoints);

		// Assert

//		for (final Datapoint datapoint : datapointDescriptions) {
//			System.out.println(datapoint);
//		}
	}

	@Test
	public void testGetDataPoints() throws UnknownHostException, IOException, ObjectServerException {

		// Arrange

		final int startDataPoint = 1;
		final int maxNumberOfDataPoints = 255;

		final DefaultObjectServerConnectionService defaultObjectServerConnectionService = new DefaultObjectServerConnectionService();

		final DefaultObjectServerFacade defaultObjectServerFacade = new DefaultObjectServerFacade();
		defaultObjectServerFacade.setKnxProject(knxProject);
		defaultObjectServerFacade.setObjectServerConnectionService(defaultObjectServerConnectionService);

		// Act

		defaultObjectServerConnectionService.connect(IP_ADDRESS, PORT);
		final List<Datapoint> datapointDescriptions = defaultObjectServerFacade.getDatapointDescriptions(startDataPoint,
				maxNumberOfDataPoints);

		// Assert

		for (final Datapoint datapoint : datapointDescriptions) {
			System.out.println(datapoint);
		}
	}

	@Test
	public void testGetDescriptionString() throws UnknownHostException, IOException, ObjectServerException {

		// Arrange

		final int startDataPoint = 1;
		final int maxNumberOfDataPoints = 255;

//		final int startDataPoint = 82;
//		final int maxNumberOfDataPoints = 1;

		final DefaultObjectServerConnectionService defaultObjectServerConnectionService = new DefaultObjectServerConnectionService();

		final DefaultObjectServerFacade defaultObjectServerFacade = new DefaultObjectServerFacade();
		defaultObjectServerFacade.setKnxProject(knxProject);
		defaultObjectServerFacade.setObjectServerConnectionService(defaultObjectServerConnectionService);

		// Act

		defaultObjectServerConnectionService.connect(IP_ADDRESS, PORT);
		final List<String> stringDescriptions = defaultObjectServerFacade.getDescriptionString(startDataPoint,
				maxNumberOfDataPoints);

		// Assert

		for (final String stringDescription : stringDescriptions) {
			System.out.println(stringDescription);
		}
	}

}

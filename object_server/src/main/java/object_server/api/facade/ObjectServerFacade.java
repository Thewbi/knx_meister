package object_server.api.facade;

import java.io.IOException;
import java.util.List;

import api.exception.ObjectServerException;

public interface ObjectServerFacade {

	void getServerItemRequest(int startDataPoint, int maxNumberOfDataPoints) throws IOException, ObjectServerException;

	List<Datapoint> getDatapointDescriptions(int startDataPoint, int maxNumberOfDataPoints)
			throws IOException, ObjectServerException;

	List<String> getDescriptionString(int startDataPoint, int maxNumberOfDataPoints)
			throws IOException, ObjectServerException;

}

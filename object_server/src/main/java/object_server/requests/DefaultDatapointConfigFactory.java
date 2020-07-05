package object_server.requests;

import api.factory.Factory;
import api.factory.exception.FactoryException;

/**
 * The coding of the datapoint configuration flags is following: Bit Meaning
 * Value Description 1 - 0 Transmit priority 00 System priority 01 Alarm
 * priority 10 High priority 11 Low priority 2 Datapoint communication 0
 * Disabled 1 Enabled 3 Read from bus 0 Disabled 1 Enabled 4 Write from bus 0
 * Disabled 1 Enabled 5 Read on init 0 Disabled 1 Enabled 6 Transmit to bus 0
 * Disabled 1 Enabled 7 Update on response 0 Disabled 1 Enabled
 */
public class DefaultDatapointConfigFactory implements Factory<Integer> {

	@Override
	public Integer create(final Object... args) throws FactoryException {

		final int transmitPriority = (int) args[0];
		final int datapointCommunication = (int) args[1];
		final int readFromBus = (int) args[2];
		final int writeFromBus = (int) args[3];
		final int readOnInit = (int) args[4];
		final int transmitToBus = (int) args[5];
		final int updateOnResponse = (int) args[6];

		// @formatter:off

		final int result = (transmitPriority << 6)
				 | (datapointCommunication << 5)
				 | (readFromBus << 4)
				 | (writeFromBus << 3)
				 | (readOnInit << 2)
				 | (transmitToBus << 1)
				 | (updateOnResponse << 0);

		// @formatter:on

		return result;
	}

}

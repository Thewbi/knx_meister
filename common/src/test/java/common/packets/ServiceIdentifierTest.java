package common.packets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class ServiceIdentifierTest {

	private static final Logger LOG = LogManager.getLogger(ServiceIdentifierTest.class);

	@Test
	public void testFromInt() {
		assertEquals(ServiceIdentifier.SEARCH_REQUEST,
				ServiceIdentifier.fromInt(ServiceIdentifier.SEARCH_REQUEST_CODE));
		assertEquals(ServiceIdentifier.SEARCH_RESPONSE,
				ServiceIdentifier.fromInt(ServiceIdentifier.SEARCH_RESPONSE_CODE));

		assertEquals(ServiceIdentifier.DESCRIPTION_REQUEST,
				ServiceIdentifier.fromInt(ServiceIdentifier.DESCRIPTION_REQUEST_CODE));
		assertEquals(ServiceIdentifier.DESCRIPTION_RESPONSE,
				ServiceIdentifier.fromInt(ServiceIdentifier.DESCRIPTION_RESPONSE_CODE));

		assertEquals(ServiceIdentifier.CONNECT_REQUEST,
				ServiceIdentifier.fromInt(ServiceIdentifier.CONNECT_REQUEST_CODE));
		assertEquals(ServiceIdentifier.CONNECT_RESPONSE,
				ServiceIdentifier.fromInt(ServiceIdentifier.CONNECT_RESPONSE_CODE));

		assertEquals(ServiceIdentifier.CONNECTIONSTATE_REQUEST,
				ServiceIdentifier.fromInt(ServiceIdentifier.CONNECTIONSTATE_REQUEST_CODE));
		assertEquals(ServiceIdentifier.CONNECTIONSTATE_RESPONSE,
				ServiceIdentifier.fromInt(ServiceIdentifier.CONNECTIONSTATE_RESPONSE_CODE));

		assertEquals(ServiceIdentifier.DISCONNECT_REQUEST,
				ServiceIdentifier.fromInt(ServiceIdentifier.DISCONNECT_REQUEST_CODE));
		assertEquals(ServiceIdentifier.DISCONNECT_RESPONSE,
				ServiceIdentifier.fromInt(ServiceIdentifier.DISCONNECT_RESPONSE_CODE));
	}

}

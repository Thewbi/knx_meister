package core.api;

public interface KNXCommunication {

	/**
	 * KNX Standard 2.1. - KNXnet/IP Core - 7.6 Discovery - 7.6.1 SEARCH_REQUEST
	 *
	 * Part of the discovery mechanism. A client broadcasts a search request into
	 * the network, all servers on the network answer the client. That way the
	 * client discovers servers without requiring manual input.
	 */
	void searchRequest();

}

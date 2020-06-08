package core.packets;

/**
 * KNX Standard 2.1. - KNXnet/IP Core - 7.4 KNXnet/IP services - 7.6.1
 * SEARCH_REQUEST
 *
 * see:
 * https://github.com/wireshark/wireshark/blob/master/epan/dissectors/packet-knxip.c
 */
public enum ServiceIdentifier {

	UNKNOWN(0x0000),

	/*
	 * The following service families are defined for the version 1.0 KNXnet/IP
	 * implementation of the eFCP protocol
	 */
//	#define KIP_SERVICE_CORE  0x02
//	#define KIP_SERVICE_MANAGEMENT  0x03
//	#define KIP_SERVICE_TUNNELING  0x04
//	#define KIP_SERVICE_ROUTING  0x05
//	#define KIP_SERVICE_REMOTE_LOGGING  0x06
//	#define KIP_SERVICE_REMOTE_DIAG_AND_CONFIG  0x07
//	#define KIP_SERVICE_OBJECT_SERVER  0x08
//	#define KIP_SERVICE_SECURITY  0x09

	/*
	 * The service codes for the core services (device discovery, self description
	 * and connection management) as defined in chapter 2 of the KNXnet/IP system
	 * specification
	 */
	SEARCH_REQUEST(0x0201),

	SEARCH_RESPONSE(0x0202),

	DESCRIPTION_REQUEST(0x0203),

	DESCRIPTION_RESPONSE(0x0204),

	CONNECT_REQUEST(0x0205),

	CONNECT_RESPONSE(0x0206),

	CONNECTIONSTATE_REQUEST(0x0207),

	CONNECTIONSTATE_RESPONSE(0x0208),

	DISCONNECT_REQUEST(0x0209),

	DISCONNECT_RESPONSE(0x020A),

	SEARCH_REQUEST_EXT(0x020B),

	SEARCH_RESPONSE_EXT(0x020C),

	/*
	 * The service codes for the device management services (tunneling of cEMI local
	 * management procedures) as defined in chapter 3 of the KNXnet/IP system
	 * specification
	 */

	DEVICE_CONFIGURATION_REQUEST(0x0310),

	DEVICE_CONFIGURATION_ACK(0x0311),

	/*
	 * The service codes for the tunneling services (transport of cEMI frames from
	 * service interface) as defined in chapter 4 of the KNXnet/IP system
	 * specification
	 */

	TUNNEL_REQUEST(0x0420),

	TUNNEL_RESPONSE(0x0421),

//	#define KIP_TUNNELING_FEATURE_GET  0x0422
//	#define KIP_TUNNELING_FEATURE_RESPONSE  0x0423
//	#define KIP_TUNNELING_FEATURE_SET  0x0424
//	#define KIP_TUNNELING_FEATURE_INFO  0x0425

	/*
	 * The service codes for the routing services (transport of cEMI frames between
	 * EIB couplers) as defined in chapter 5 of the KNXnet/IP system specification
	 */

	ROUTING_INDICATION(0x0530),

//	#define KIP_ROUTING_LOST_MESSAGE  0x0531
//	#define KIP_ROUTING_BUSY  0x0532
//	#define KIP_ROUTING_SYSTEM_BROADCAST  0x0533

	/*
	 * The service codes for RemoteDiagAndConfig
	 */
//	#define KIP_REMOTE_DIAG_REQUEST  0x0740
//	#define KIP_REMOTE_DIAG_RESPONSE  0x0741
//	#define KIP_REMOTE_CONFIG_REQUEST  0x0742
//	#define KIP_REMOTE_RESET_REQUEST  0x0743

	/*
	 * The service codes for KNX-IP Secure
	 */
	KIP_SECURE_WRAPPER(0x0950),

	KIP_SESSION_REQUEST(0x0951),

	KIP_SESSION_RESPONSE(0x0952),

	KIP_SESSION_AUTHENTICATE(0x0953),

	KIP_SESSION_STATUS(0x0954),

	KIP_TIMER_NOTIFY(0x0955);

	/* KNXnet/IP host protocols */
//	#define KIP_IPV4_UDP 0x01#
//	define KIP_IPV4_TCP 0x02

	public static final int UNKNOWN_CODE = 0x0000;

	public static final int SEARCH_REQUEST_CODE = 0x0201;

	public static final int SEARCH_RESPONSE_CODE = 0x0202;

	public static final int DESCRIPTION_REQUEST_CODE = 0x0203;

	public static final int DESCRIPTION_RESPONSE_CODE = 0x0204;

	public static final int CONNECT_REQUEST_CODE = 0x0205;

	public static final int CONNECT_RESPONSE_CODE = 0x0206;

	public static final int CONNECTIONSTATE_REQUEST_CODE = 0x0207;

	public static final int CONNECTIONSTATE_RESPONSE_CODE = 0x0208;

	public static final int DISCONNECT_REQUEST_CODE = 0x0209;

	public static final int DISCONNECT_RESPONSE_CODE = 0x020A;

	public static final int SEARCH_REQUEST_EXT_CODE = 0x020B;

	public static final int SEARCH_RESPONSE_EXT_CODE = 0x020C;

	public static final int DEVICE_CONFIGURATION_REQUEST_CODE = 0x0310;

	public static final int DEVICE_CONFIGURATION_ACK_CODE = 0x0311;

	public static final int TUNNEL_REQUEST_CODE = 0x0420;

	public static final int TUNNEL_RESPONSE_CODE = 0x0421;

	public static final int ROUTING_INDICATION_CODE = 0x0530;

	private final int id;

	ServiceIdentifier(final int id) {
		this.id = id;
	}

	public static ServiceIdentifier fromInt(final int id) {

		switch (id) {

		case UNKNOWN_CODE:
			return UNKNOWN;

		case SEARCH_REQUEST_CODE:
			return SEARCH_REQUEST;

		case SEARCH_RESPONSE_CODE:
			return SEARCH_RESPONSE;

		case DESCRIPTION_REQUEST_CODE:
			return DESCRIPTION_REQUEST;

		case DESCRIPTION_RESPONSE_CODE:
			return DESCRIPTION_RESPONSE;

		case CONNECT_REQUEST_CODE:
			return CONNECT_REQUEST;

		case CONNECT_RESPONSE_CODE:
			return CONNECT_RESPONSE;

		case CONNECTIONSTATE_REQUEST_CODE:
			return CONNECTIONSTATE_REQUEST;

		case CONNECTIONSTATE_RESPONSE_CODE:
			return CONNECTIONSTATE_RESPONSE;

		case DISCONNECT_REQUEST_CODE:
			return DISCONNECT_REQUEST;

		case DISCONNECT_RESPONSE_CODE:
			return DISCONNECT_RESPONSE;

		case SEARCH_REQUEST_EXT_CODE:
			return SEARCH_REQUEST_EXT;

		case SEARCH_RESPONSE_EXT_CODE:
			return SEARCH_RESPONSE_EXT;

		case DEVICE_CONFIGURATION_REQUEST_CODE:
			return DEVICE_CONFIGURATION_REQUEST;

		case DEVICE_CONFIGURATION_ACK_CODE:
			return DEVICE_CONFIGURATION_ACK;

		case TUNNEL_REQUEST_CODE:
			return TUNNEL_REQUEST;

		case TUNNEL_RESPONSE_CODE:
			return TUNNEL_RESPONSE;

		case ROUTING_INDICATION_CODE:
			return ROUTING_INDICATION;

		default:
			throw new RuntimeException("Unknown id " + id);
		}
	}

	public int getValue() {
		return id;
	}

}

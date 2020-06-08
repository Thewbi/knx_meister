package core.packets;

public enum PropertyId {

	// @formatter:off

	PID_OBJECT_TYPE(1),
	PID_LOAD_STATE_CONTROL(5),
	PID_RUN_STATE_CONTROL(6),
	PID_TABLE_REFERENCE(7),
	PID_SERVICE_CONTROL(8),
	PID_FIRMWARE_REVISION(9),

	PID_SERIAL_NUMBER(11),
	PID_MANUFACTURER_ID(12),
	PID_PROG_VERSION(13),
    PID_DEVICE_CONTROL(14),
    PID_ORDER_INFO(15),
    PID_PEI_TYPE(16),
    PID_PORT_CONFIGURATION(17),

	PID_TABLE(23),
	PID_VERSION(25),
	PID_MCB_TABLE(27),
	PID_ERROR_CODE(28),

	PID_ROUTING_COUNT(51),
	PID_PROG_MODE(54),
	PID_MAX_APDU_LENGTH(56),
	PID_SUBNET_ADDR(57),
	PID_DEVICE_ADDR(58),

	PID_RF_MULTI_TYPE(51),
	PID_RF_DOMAIN_ADDRESS(56),
	PID_RF_RETRANSMITTER(57),
	PID_RF_FILTERING_MODE_SUPPORT(58),
	PID_RF_FILTERING_MODE_SELECT(59),

	PID_MEDIUM_TYPE(51),
	PID_COMM_MODE(52),
	PID_MEDIUM_AVAILABILITY(53),
	PID_ADD_INFO_TYPES(54),
	PID_TIME_BASE(55),
	PID_TRANSP_ENABLE(56),
	PID_CLIENT_SNA(57),
	PID_CLIENT_DEVICE_ADDRESS(58),
	PID_BIBAT_NEXTBLOCK(59),

	PID_PROJECT_INSTALLATION_ID(51),
	PID_KNX_INDIVIDUAL_ADDRESS(52),
	PID_ADDITIONAL_INDIVIDUAL_ADDRESSES(53),
	PID_CURRENT_IP_ASSIGNMENT_METHOD(54),
	PID_IP_ASSIGNMENT_METHOD(55),
	PID_IP_CAPABILITIES(56),
	PID_CURRENT_IP_ADDRESS(57),
	PID_CURRENT_SUBNET_MASK(58),
	PID_CURRENT_DEFAULT_GATEWAY(59),

	PID_RF_MODE_SELECT(60),
	PID_RF_MODE_SUPPORT(61),
    PID_RF_FILTERING_MODE_SELECT_CEMI_SERVER(62),
	PID_RF_FILTERING_MODE_SUPPORT_CEMI_SERVER(63),
	PID_COMM_MODES_SUPPORTED(64),
	PID_FILTERING_MODE_SUPPORT(65),
	PID_FILTERING_MODE_SELECT(66),
	PID_MAX_INTERFACE_APDU_LENGTH(68),
	PID_MAX_LOCAL_APDU_LENGTH(69),

	PID_IP_ADDRESS(60),
	PID_SUBNET_MASK(61),
	PID_DEFAULT_GATEWAY(62),
	PID_DHCP_BOOTP_SERVER(63),
	PID_MAC_ADDRESS(64),
	PID_SYSTEM_SETUP_MULTICAST_ADDRESS(65),
	PID_ROUTING_MULTICAST_ADDRESS(66),
	PID_TTL(67),
	PID_KNXNETIP_DEVICE_CAPABILITIES(68),
	PID_KNXNETIP_DEVICE_STATE(69),

	PID_RF_BIDIR_TIMEOUT(60),
	PID_RF_DIAG_SA_FILTER_TABLE(61),
	PID_RF_DIAG_BUDGET_TABLE(62),
	PID_RF_DIAG_PROBE(63),

	PID_KNXNETIP_ROUTING_CAPABILITIES(70),
	PID_PRIORITY_FIFO_ENABLED(71),
	PID_QUEUE_OVERFLOW_TO_IP(72),
	PID_QUEUE_OVERFLOW_TO_KNX(73),
	PID_MSG_TRANSMIT_TO_IP(74),
	PID_MSG_TRANSMIT_TO_KNX(75),
	PID_FRIENDLY_NAME(76),
	PID_ROUTING_BUSY_WAIT_TIME(78),

	PID_IO_LIST(71),
	PID_HARDWARE_TYPE(78),

	PID_RF_DOMAIN_ADDRESS_CEMI_SERVER(82),

	// Part 3/5 Management - Chapter 3/5/1 Resources - 4.3.33 PID_DEVICE_DESCRIPTOR (PID = 83)
	PID_DEVICE_DESCRIPTOR(83),
	;

	// @formatter:on

	private final int id;

	PropertyId(final int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}
}

package api.configuration;

public interface ConfigurationManager {

	static final String LOCAL_IP_CONFIG_KEY = "local_ip";

	static final String PORT_CONFIG_KEY = "port";

	static final String OBJECT_SERVER_PORT_CONFIG_KEY = "object_server_port";

	static final String MULTICAST_IP_CONFIG_KEY = "multicast_ip";

	static final int KNX_PORT_DEFAULT = 3671;

	static final int POINT_TO_POINT_PORT = KNX_PORT_DEFAULT;

	static final int POINT_TO_POINT_CONTROL_PORT = KNX_PORT_DEFAULT;

	static final int POINT_TO_POINT_DATA_PORT = KNX_PORT_DEFAULT;

	void setProperty(String configKey, String value);

	String getPropertyAsString(String configKey);

	int getPropertyAsInt(String configKey);

	void dumpOptions();

}

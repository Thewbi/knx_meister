package common.configuration;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.configuration.ConfigurationManager;
import common.utils.NetworkUtils;

public class DefaultConfigurationManager implements ConfigurationManager {

	private static final Logger LOG = LogManager.getLogger(DefaultConfigurationManager.class);

	private final Map<String, Object> properties = new HashMap<>();

//	@Value("${ip}")
//	private String ip;

	/**
	 * ctor
	 */
	public DefaultConfigurationManager() {

		// add local_ip default value
		properties.computeIfAbsent(LOCAL_IP_CONFIG_KEY, k -> {
			try {
				return NetworkUtils.retrieveLocalIP();
			} catch (UnknownHostException | SocketException e) {
				LOG.error(e.getMessage(), e);
			}
			return "error";
		});

		// add multicast_ip default value
		properties.computeIfAbsent(MULTICAST_IP_CONFIG_KEY, k -> {
			return NetworkUtils.KNX_MULTICAST_IP;
		});

		// add port default value
		properties.computeIfAbsent(PORT_CONFIG_KEY, k -> {
			return ConfigurationManager.KNX_PORT_DEFAULT;
		});

		// add object server protocol port default value
		properties.computeIfAbsent(OBJECT_SERVER_PORT_CONFIG_KEY, k -> {
			return NetworkUtils.OBJECT_SERVER_PROTOCOL_PORT;
		});
	}

	@Override
	public void setProperty(final String key, final String value) {
		properties.put(key, value);
	}

	@Override
	public String getPropertyAsString(final String key) {
		final Object value = properties.get(key);
		if (value == null) {
			return null;
		}
		return value.toString();
	}

	@Override
	public int getPropertyAsInt(final String key) {
		final Object value = properties.get(key);
		if (value == null) {
			throw new RuntimeException("Configuration property '" + key + "' not present!");
		}
		return ((Integer) value).intValue();
	}

	@Override
	public void dumpOptions() {
		LOG.info("---------------------------------------------------------------------------------");
		LOG.info("Properties are:");
		LOG.info("---------------------------------------------------------------------------------");
		properties.entrySet().stream().forEach(entry -> {
			LOG.info(entry.getKey() + ": " + entry.getValue());
		});
		LOG.info("---------------------------------------------------------------------------------");
	}

}

package core.communication.controller;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import core.api.device.Device;
import core.communication.BaseDatagramPacketCallback;
import core.communication.ConnectionManager;
import core.packets.HPAIStructure;

public abstract class BaseController extends BaseDatagramPacketCallback {

	public static final int KNX_PORT_DEFAULT = 3671;

	public static final int POINT_TO_POINT_PORT = KNX_PORT_DEFAULT;

	public static final int POINT_TO_POINT_CONTROL_PORT = KNX_PORT_DEFAULT;

	public static final int POINT_TO_POINT_DATA_PORT = KNX_PORT_DEFAULT;

	private final String localInetAddress;

	private int channelNumber = 0x01;

	private final Map<String, HPAIStructure> deviceMap = new HashMap<>();

	private ConnectionManager connectionManager;

	private Device device;

	public BaseController() throws SocketException, UnknownHostException {
		localInetAddress = InetAddress.getLocalHost().getHostAddress();
	}

	protected int incrementChannelNumber() {
		channelNumber++;
		return channelNumber;
	}

	public void setConnectionManager(final ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(final Device device) {
		this.device = device;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public Map<String, HPAIStructure> getDeviceMap() {
		return deviceMap;
	}

	public String getLocalInetAddress() {
		return localInetAddress;
	}

	public int getChannelNumber() {
		return channelNumber;
	}

}

package project.parsing.domain;

import java.util.ArrayList;
import java.util.List;

import project.parsing.knx.KNXGroupAddressStyle;

public class KNXProject {

	private String id;

	private String name;

	private KNXGroupAddressStyle groupAddressStyle;

	private final List<KNXDeviceInstance> deviceInstances = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public KNXGroupAddressStyle getGroupAddressStyle() {
		return groupAddressStyle;
	}

	public void setGroupAddressStyle(final KNXGroupAddressStyle groupAddressStyle) {
		this.groupAddressStyle = groupAddressStyle;
	}

	public List<KNXDeviceInstance> getDeviceInstances() {
		return deviceInstances;
	}

}

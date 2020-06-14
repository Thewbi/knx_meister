package project.parsing.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.parsing.knx.KNXGroupAddressStyle;

public class KNXProject {

	private String id;

	private String name;

	private KNXGroupAddressStyle groupAddressStyle;

	private final List<KNXDeviceInstance> deviceInstances = new ArrayList<>();

	private final Map<String, Map<String, String>> languageStoreMap = new HashMap<>();

	private final Map<String, KNXDatapointSubtype> datapointSubtypeMap = new HashMap<>();

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

	public Map<String, Map<String, String>> getLanguageStoreMap() {
		return languageStoreMap;
	}

	public Map<String, KNXDatapointSubtype> getDatapointSubtypeMap() {
		return datapointSubtypeMap;
	}

}

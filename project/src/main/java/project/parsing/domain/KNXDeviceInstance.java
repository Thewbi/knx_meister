package project.parsing.domain;

import java.util.ArrayList;
import java.util.List;

public class KNXDeviceInstance {

	private String address;

	private final List<KNXComObject> comObjects = new ArrayList<>();

	public String getAddress() {
		return address;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public List<KNXComObject> getComObjects() {
		return comObjects;
	}

}

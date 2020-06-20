package project.parsing.domain;

public class KNXComObject {

	private String id;

	private String text;

	private int number;

	private boolean groupObject;

	private String groupAddressLink;

	private KNXGroupAddress knxGroupAddress;

	private String hardwareName;

	private String hardwareText;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public boolean isGroupObject() {
		return groupObject;
	}

	public void setGroupObject(final boolean groupObject) {
		this.groupObject = groupObject;
	}

	public String getGroupAddressLink() {
		return groupAddressLink;
	}

	public void setGroupAddressLink(final String groupAddressLink) {
		this.groupAddressLink = groupAddressLink;
	}

	public KNXGroupAddress getKnxGroupAddress() {
		return knxGroupAddress;
	}

	public void setKnxGroupAddress(final KNXGroupAddress knxGroupAddress) {
		this.knxGroupAddress = knxGroupAddress;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(final int number) {
		this.number = number;
	}

	public String getHardwareName() {
		return hardwareName;
	}

	public void setHardwareName(final String hardwareName) {
		this.hardwareName = hardwareName;
	}

	public String getHardwareText() {
		return hardwareText;
	}

	public void setHardwareText(final String hardwareText) {
		this.hardwareText = hardwareText;
	}

}

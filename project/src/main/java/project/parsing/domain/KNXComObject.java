package project.parsing.domain;

public class KNXComObject {

	private String id;

	private String text;

	private boolean groupObject;

	private String groupAddressLink;

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

}

package project.parsing.domain;

public class KNXDatapointSubtype {

	private KNXDatapointType knxDatapointType;

	private String id;

	private String text;

	public KNXDatapointType getKnxDatapointType() {
		return knxDatapointType;
	}

	public void setKnxDatapointType(final KNXDatapointType knxDatapointType) {
		this.knxDatapointType = knxDatapointType;
	}

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

}

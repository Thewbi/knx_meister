package project.parsing.domain;

public class KNXDatapointSubtype {

	private KNXDatapointType knxDatapointType;

	private String id;

	private String text;

	private String number;

	private String format;

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

	public String getNumber() {
		return number;
	}

	public void setNumber(final String number) {
		this.number = number;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(final String format) {
		this.format = format;
	}

}

package api.project;

public class KNXDatapointType {

	private String id;

	private String text;

	private String name;

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

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "KNXDatapointType [id=" + id + ", text=" + text + ", name=" + name + "]";
	}

}

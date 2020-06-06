package core.packets;

public class ProtocolDescriptor {

	private int protocol;

	private int version;

	@Override
	public String toString() {
		return protocol + " v" + version;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(final int protocol) {
		this.protocol = protocol;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

}

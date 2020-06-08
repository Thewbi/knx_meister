package core.packets;

public class ProtocolDescriptor {

	private int protocol;

	private int version;

	public ProtocolDescriptor() {

	}

	public ProtocolDescriptor(final ProtocolDescriptor other) {
		protocol = other.protocol;
		version = other.version;
	}

	@Override
	public ProtocolDescriptor clone() {
		return new ProtocolDescriptor(this);
	}

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

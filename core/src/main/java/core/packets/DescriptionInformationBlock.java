package core.packets;

public abstract class DescriptionInformationBlock {

	private int length;

	public abstract DescriptionInformationBlockType getType();

	public abstract void fromBytes(byte[] source, int index);

	public abstract byte[] getBytes();

	public int getLength() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

}

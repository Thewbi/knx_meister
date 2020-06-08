package core.common;

import core.packets.Header;

public interface KNXPacketConverter<S, T> extends Converter<S, T> {

	boolean accept(Header header);

}

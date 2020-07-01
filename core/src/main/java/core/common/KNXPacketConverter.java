package core.common;

import common.packets.KNXHeader;

public interface KNXPacketConverter<S, T> extends Converter<S, T> {

	boolean accept(KNXHeader header);

}

package core.conversion;

import api.connection.ConnectionDto;
import api.conversion.Converter;
import core.communication.Connection;

public class DefaultConnectionConnectionDtoConverter implements Converter<Connection, ConnectionDto> {

    @Override
    public ConnectionDto convert(final Connection source) {

        final ConnectionDto target = new ConnectionDto();
        target.setId(source.getId());
        target.setConnectionType(source.getConnectionType().name());
        target.setReceiveSequenceCounter(source.getReceiveSequenceCounter());
        target.setSendSequenceCounter(source.getSendSequenceCounter());

        return target;
    }

}

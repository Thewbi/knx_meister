package knx.server.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import api.connection.ConnectionDto;
import api.conversion.Converter;
import core.communication.Connection;
import core.communication.ConnectionManager;
import core.communication.controller.ServerCoreController;
import core.packets.KNXPacket;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/connections")
@Api(value = "/connections")
public class ConnectionResource {

    private static final Logger LOG = LogManager.getLogger(ConnectionResource.class);

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private Converter<Connection, ConnectionDto> connectionConnectionDtoDeviceDtoConverter;

    @Autowired
    private ServerCoreController serverCoreController;

    @ApiOperation("Returns all established connections.")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK", response = String.class) })
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConnections() {

        try {

            final List<ConnectionDto> result = connectionManager.getConnectionMap().values().stream().map(c -> {
                return connectionConnectionDtoDeviceDtoConverter.convert(c);
            }).collect(Collectors.toList());

            return Response.status(200).entity(result).build();

        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(500).build();
        }
    }

    @ApiOperation("Tries to establish a tunneling connection.")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK", response = String.class) })
    @POST
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testCreateTunnelingConnection() {

        try {

            final Status status = new Status();
            status.setStatus("OK");

            final Optional<Connection> liveBaseConnection = connectionManager.getLiveBaseConnection();
            if (!liveBaseConnection.isPresent()) {
                status.setStatus("ERROR No base connection!");
                Response.status(500).entity(status).build();
            }

            final KNXPacket retrieveConnectionRequestKNXPacket = serverCoreController
                    .retrieveConnectionRequestKNXPacket();

            liveBaseConnection.get().sendRequest(retrieveConnectionRequestKNXPacket);

            return Response.status(200).entity(status).build();

        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

}

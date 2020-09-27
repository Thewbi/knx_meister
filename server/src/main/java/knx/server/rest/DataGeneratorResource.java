package knx.server.rest;

import java.util.Optional;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

import api.datagenerator.DataGenerator;
import api.datagenerator.dto.DataGeneratorDto;
import api.device.Device;
import api.device.DeviceService;
import api.factory.Factory;
import api.factory.exception.FactoryException;
import api.project.KNXComObject;
import core.communication.Connection;
import core.communication.ConnectionManager;
import core.communication.thread.DataSenderRunnable;
import core.data.sending.DataSender;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/datagenerator")
@Api(value = "/datagenerator")
public class DataGeneratorResource {

    private static final Logger LOG = LogManager.getLogger(DataGeneratorResource.class);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private DataSender dataSender;

    @Autowired
    private Factory<DataGenerator> defaultDataGeneratorFactory;

    /**
     *
     * @param deviceAddress    the address of the device that contains the
     *                         communication object for the group address.
     * @param groupAddress     the group address connected to the communication
     *                         object within the device. Group addresses are
     *                         normally formed with slashes but slashes are special
     *                         characters in URLs, group addresses are formed with
     *                         dots here.
     * @param dataGeneratorDto the description of what type of generator to create
     * @return
     */
    @ApiOperation("Sets a generator")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK", response = String.class) })
    @POST
    @Path("/add/{deviceAddress}/{groupAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDataGenerator(@PathParam("deviceAddress") final String deviceAddress,
            @PathParam("groupAddress") final String groupAddress, final DataGeneratorDto dataGeneratorDto) {

        final String tempGroupAddress = groupAddress.replaceAll("\\.", "/");

        LOG.info("Adding data generator '{}' to device '{}' and groupAddress '{}'", dataGeneratorDto, deviceAddress,
                tempGroupAddress);

        final Device device = deviceService.getDevices().get(deviceAddress);
        final KNXComObject knxComObject = device.getComObjects().get(tempGroupAddress);

        DataGenerator dataGenerator;
        try {
            dataGenerator = defaultDataGeneratorFactory.create(dataGeneratorDto);

            knxComObject.setDataGenerator(dataGenerator);

            final Optional<Connection> connection = connectionManager.getLiveTunnelingConnection();
            if (connection.isPresent()) {

                LOG.info("Starting data sender for connection " + connection.get().getId());

                final DataSenderRunnable dataSenderRunnable = new DataSenderRunnable();
                dataSenderRunnable.setLabel(tempGroupAddress + " " + toString());
                dataSenderRunnable.setDataSender(dataSender);
                dataSenderRunnable.setConnection(connection.get());
                dataSenderRunnable.setDeviceService(deviceService);

                final Thread thread = new Thread(dataSenderRunnable);
                thread.start();
            }

            final Status status = new Status();
            status.setStatus("OK");
            final Gson gson = new Gson();

            return Response.status(201, MediaType.APPLICATION_JSON).entity(gson.toJson(status)).build();

        } catch (final FactoryException e) {
            LOG.error(e.getMessage(), e);
            return Response.status(500).build();
        }
    }

}

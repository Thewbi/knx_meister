package knx.server.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import api.conversion.Converter;
import api.device.Device;
import api.device.DeviceService;
import api.device.dto.DeviceDto;
import api.project.KNXComObject;
import api.project.dto.KNXComObjectDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/devices")
@Api(value = "/devices")
public class DevicesResource {

    private static final Logger LOG = LogManager.getLogger(DevicesResource.class);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private Converter<Device, DeviceDto> defaultDeviceDeviceDtoConverter;

    @Autowired
    private Converter<KNXComObject, KNXComObjectDto> knxComObjectKNXComObjectDtoConverter;

    @ApiOperation("Retrieve all devices parsed from the project file")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK", response = String.class) })
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDevices() {

        try {

            final List<DeviceDto> result = deviceService.getDevices().values().stream().map(d -> {
                return defaultDeviceDeviceDtoConverter.convert(d);
            }).collect(Collectors.toList());

            return Response.status(200).entity(result).build();

        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(500).build();
        }

    }

    @ApiOperation("Retrieve all communication objects of a specific device.")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK", response = String.class) })
    @GET
    @Path("/{deviceAddress}/communicationobjects")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDevices(@PathParam("deviceAddress") final String deviceAddress) {

        try {

            final Device device = deviceService.getDevices().get(deviceAddress);

            if (device == null) {
                return Response.status(404).entity("Could not find device for address " + deviceAddress).build();
            }

            final List<KNXComObjectDto> result = device.getComObjects().values().stream().map(d -> {
                return knxComObjectKNXComObjectDtoConverter.convert(d);
            }).collect(Collectors.toList());

            return Response.status(200).entity(result).build();

        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(500).build();
        }

    }

}

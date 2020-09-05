package knx.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/system")
@Api(value = "/system")
public class SystemResource {

	@ApiOperation("Retrieve current version")
	@ApiResponses({ @ApiResponse(code = 200, message = "OK", response = String.class) })
	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String version() {
		return "1.0.0";
	}

}

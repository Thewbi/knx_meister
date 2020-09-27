package core.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import api.device.Device;
import api.device.DeviceService;
import api.exception.ProjectParsingException;
import api.factory.exception.FactoryException;
import api.project.KNXProject;
import project.parsing.ProjectParser;
import project.parsing.factory.ProjectParserFactory;
import project.parsing.knx.KNXProjectParsingContext;

public class DefaultDeviceServiceTest {

    @Test
    public void testProcess() throws IOException, ProjectParsingException, FactoryException {

        final ProjectParserFactory projectParserFactory = new ProjectParserFactory();
        final ProjectParser<KNXProjectParsingContext> parser = projectParserFactory.create();

//      final File projectFile = new File("src/test/resources/KNX IP BAOS 777.knxproj");
        final File projectFile = new File("src/test/resources/KNXfirstSteps200212_5devices.knxproj");

        final KNXProject knxProject = parser.parse(projectFile);

        final DeviceService deviceService = new DefaultDeviceService();
        deviceService.retrieveDevicesFromProject(knxProject);

        assertEquals(deviceService.getDevices().size(), 5);

        deviceService.getDevices().entrySet().forEach(e -> {
            final Device device = e.getValue();
            System.out.println(e.getKey() + " - " + device + " ComObjectSize: " + device.getComObjects().size());

            device.getComObjects().entrySet().forEach(comObjectEntry -> {
                System.out.println(comObjectEntry.getKey() + " " + comObjectEntry.getValue());
            });
        });

    }
}

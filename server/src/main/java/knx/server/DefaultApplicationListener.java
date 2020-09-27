package knx.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import api.device.DeviceService;
import api.exception.ProjectParsingException;
import api.project.KNXProject;
import api.project.ProjectService;
import core.communication.MulticastListenerReaderThread;
import core.communication.ObjectServerReaderThread;

@Component
public class DefaultApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultApplicationListener.class);

    @Autowired
    private MulticastListenerReaderThread multicastListenerThread;

    @Autowired
    private ObjectServerReaderThread objectServerReaderThread;

    @Autowired
    private ProjectService projectService;

    @Autowired
    DeviceService deviceService;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {

        LOG.info("DefaultApplicationListener.onApplicationEvent()");

        try {

            final KNXProject knxProject = projectService.parseProjectFile();
            deviceService.retrieveDevicesFromProject(knxProject);

            new Thread(objectServerReaderThread).start();
            new Thread(multicastListenerThread).start();

        } catch (IOException | ProjectParsingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}

package api.device;

import java.util.Map;

import api.project.KNXProject;

public interface DeviceService {

    void retrieveDevicesFromProject(KNXProject project);

    Map<String, Device> getDevices();

}

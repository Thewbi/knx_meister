package core.devices;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.device.Device;
import api.device.DeviceService;
import api.device.DeviceStatus;
import api.project.KNXDeviceInstance;
import api.project.KNXGroupAddress;
import api.project.KNXProject;
import common.utils.Utils;

public class DefaultDeviceService implements DeviceService {

    private static final Logger LOG = LogManager.getLogger(DefaultDeviceService.class);

    /** normal mode or programming mode */
    private static final DeviceStatus DEVICE_STATUS = DeviceStatus.NORMAL_MODE;

    /** all devices known to the service */
    private final Map<String, Device> devices = new HashMap<>();

    @Override
    public void retrieveDevicesFromProject(final KNXProject project) {

        for (final KNXDeviceInstance knxDeviceInstance : project.getDeviceInstances()) {

            final Device device = new DefaultDevice();

            device.setHostPhysicalAddress(Utils.knxAddressToInteger(knxDeviceInstance.getAddress()));
            device.setPhysicalAddress(Utils.knxAddressToInteger(knxDeviceInstance.getAddress()));
            device.setDeviceStatus(DEVICE_STATUS);

            knxDeviceInstance.getComObjects().values().stream().forEach(knxComObject -> {

                final KNXGroupAddress knxGroupAddress = knxComObject.getKnxGroupAddress();
                if (knxGroupAddress != null && StringUtils.isNotBlank(knxGroupAddress.getGroupAddress())) {

                    final String groupAddress = knxGroupAddress.getGroupAddress();
                    device.getDeviceProperties().put(groupAddress, knxGroupAddress);

                    // DEBUG
                    // PUT_A and PUT_B put comObjects into knxDeviceInstance.
                    // Now copy from knxDeviceInstance into Device / DefaultDevice
                    LOG.info("PUT_C into device " + device.getPhysicalAddress() + " DataPointId:"
                            + knxComObject.getNumber() + " " + knxComObject.getKnxGroupAddress() + " "
                            + knxComObject.getHardwareName() + " " + knxComObject.getText());

                    device.getComObjects().put(groupAddress, knxComObject);
                    device.getComObjectsByDatapointId().put(knxComObject.getNumber(), knxComObject);
                }
            });

            devices.put(knxDeviceInstance.getAddress(), device);
        }
    }

    @Override
    public Map<String, Device> getDevices() {
        return devices;
    }

}

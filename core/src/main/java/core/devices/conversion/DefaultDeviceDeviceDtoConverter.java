package core.devices.conversion;

import api.conversion.Converter;
import api.device.Device;
import api.device.dto.DeviceDto;
import common.utils.Utils;

public class DefaultDeviceDeviceDtoConverter implements Converter<Device, DeviceDto> {

    @Override
    public DeviceDto convert(final Device device) {

        final DeviceDto deviceDto = new DeviceDto();
        deviceDto.setPhysicalAddress(device.getPhysicalAddress());
        deviceDto.setPhysicalAddressAsString(Utils.integerToKNXAddress(device.getPhysicalAddress(), "."));

        return deviceDto;
    }

}

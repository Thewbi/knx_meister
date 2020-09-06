package api.device.dto;

public class DeviceDto {

    private int physicalAddress;

    private String physicalAddressAsString;

    public int getPhysicalAddress() {
        return physicalAddress;
    }

    public void setPhysicalAddress(final int physicalAddress) {
        this.physicalAddress = physicalAddress;
    }

    public String getPhysicalAddressAsString() {
        return physicalAddressAsString;
    }

    public void setPhysicalAddressAsString(final String physicalAddressAsString) {
        this.physicalAddressAsString = physicalAddressAsString;
    }

}

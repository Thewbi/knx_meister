package api.project;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import api.datagenerator.DataGenerator;

public class KNXComObject {

    private String id;

    private String text;

    private int number;

    private boolean groupObject;

    private String groupAddressLink;

    private KNXGroupAddress knxGroupAddress;

    private String hardwareName;

    private String hardwareText;

    private KNXProject knxProject;

    private DataGenerator dataGenerator;

    public KNXDatapointSubtype getDataPointSubtype(final KNXProject knxProject) {

        final KNXGroupAddress knxGroupAddress = getKnxGroupAddress();
        if (knxGroupAddress == null) {
            return null;
        }

        final String dataPointTypeId = knxGroupAddress.getDataPointType();

        return knxProject.getDatapointSubtypeMap().get(dataPointTypeId);
    }

    public KNXDatapointType getDataPointType(final KNXProject knxProject) {

        final KNXDatapointSubtype dataPointSubtype = getDataPointSubtype(knxProject);
        if (dataPointSubtype == null) {
            return null;
        }

        return dataPointSubtype.getKnxDatapointType();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public boolean isGroupObject() {
        return groupObject;
    }

    public void setGroupObject(final boolean groupObject) {
        this.groupObject = groupObject;
    }

    public String getGroupAddressLink() {
        return groupAddressLink;
    }

    public void setGroupAddressLink(final String groupAddressLink) {
        this.groupAddressLink = groupAddressLink;
    }

    public KNXGroupAddress getKnxGroupAddress() {
        return knxGroupAddress;
    }

    public void setKnxGroupAddress(final KNXGroupAddress knxGroupAddress) {
        this.knxGroupAddress = knxGroupAddress;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public String getHardwareName() {
        return hardwareName;
    }

    public void setHardwareName(final String hardwareName) {
        this.hardwareName = hardwareName;
    }

    public String getHardwareText() {
        return hardwareText;
    }

    public void setHardwareText(final String hardwareText) {
        this.hardwareText = hardwareText;
    }

    @Override
    public String toString() {

//		return "KNXComObject [id=" + id + ", text=" + text + ", number=" + number + ", groupObject=" + groupObject
//				+ ", groupAddressLink=" + groupAddressLink + ", knxGroupAddress=" + knxGroupAddress + ", hardwareName="
//				+ hardwareName + ", hardwareText=" + hardwareText + "]";

        final StringBuilder stringBuilder = new StringBuilder();

        // label
        if (isGroupObject()) {
            stringBuilder.append("[KNXComObject is a GroupObject] ");
        }

        // number, datapoint Id
        stringBuilder.append("DataPointId:").append(getNumber()).append(" (0x")
                .append(String.format("%1$02X", getNumber())).append(")");

        // group address
        if (getKnxGroupAddress() != null) {
            stringBuilder.append(" ").append(getKnxGroupAddress().getGroupAddress());
        }

        // data point type
        final KNXGroupAddress knxGroupAddress = getKnxGroupAddress();
        if (knxGroupAddress != null) {

            final String dataPointTypeId = knxGroupAddress.getDataPointType();

            final KNXDatapointSubtype knxDatapointSubtype = knxProject.getDatapointSubtypeMap().get(dataPointTypeId);

            final Map<String, String> languageMap = knxProject.getLanguageStoreMap().get("de-DE");
            final KNXDatapointType knxDatapointType = knxDatapointSubtype.getKnxDatapointType();
            final String datapointTranslated = languageMap.get(knxDatapointType.getId());
            final String datapointSubtypeTranslated = languageMap.get(knxDatapointSubtype.getId());

            stringBuilder.append(" ").append(knxDatapointType.getName()).append(" ")
                    .append(knxDatapointSubtype.getNumber()).append(" ").append(datapointTranslated).append(", ")
                    .append(datapointSubtypeTranslated).append(" ").append(knxDatapointSubtype.getFormat())
                    .append(" DataPointType: ").append(dataPointTypeId);
        }

        // id
        stringBuilder.append(" ").append(getId());

        // text
        if (StringUtils.isNotBlank(getText())) {
            stringBuilder.append(" ").append(getText());
        }

        // hardware information
        stringBuilder.append(" ").append(getHardwareName()).append(" ").append(getHardwareText());

        return stringBuilder.toString();
    }

    public KNXProject getKnxProject() {
        return knxProject;
    }

    public void setKnxProject(final KNXProject knxProject) {
        this.knxProject = knxProject;
    }

    public DataGenerator getDataGenerator() {
        return dataGenerator;
    }

    public void setDataGenerator(final DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

}

package object_server.conversion;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.conversion.Converter;
import api.data.serializer.DataSerializer;
import api.project.KNXComObject;
import api.project.KNXDatapointType;
import api.project.KNXProject;

public class ComObjectValueConverter implements Converter<KNXComObject, byte[]> {

    private static final Logger LOG = LogManager.getLogger(ComObjectValueConverter.class);

    private KNXProject knxProject;

    private Map<String, DataSerializer<Object>> dataSerializerMap;

    @Override
    public byte[] convert(final KNXComObject knxComObject) {

        if (knxProject == null) {
//			throw new ObjectServerException("knxProject is null!");
            return null;
        }

        Object value = null;

        final KNXDatapointType dataPointType = knxComObject.getDataPointType(knxProject);
        switch (dataPointType.getId()) {

        // 1-Bit
        case "DPT-1":
            // return value or insert and return default value
            if (!knxProject.getValueMap().containsKey(knxComObject.getNumber())) {
                knxProject.getValueMap().put(knxComObject.getNumber(), 0);
            }
            value = knxProject.getValueMap().get(knxComObject.getNumber());

            return new byte[] { (byte) (int) value };

        // unsigned byte
        case "DPT-5":
            // return value or insert and return default value
            if (!knxProject.getValueMap().containsKey(knxComObject.getNumber())) {
                knxProject.getValueMap().put(knxComObject.getNumber(), 0);
            }
            value = knxProject.getValueMap().get(knxComObject.getNumber());
            return new byte[] { (byte) (int) value };

        // 2-Byte Octet Float
        case "DPT-9":
            // return value or insert and return default value
            if (!knxProject.getValueMap().containsKey(knxComObject.getNumber())) {
                knxProject.getValueMap().put(knxComObject.getNumber(), 0.0d);
            }
            value = knxProject.getValueMap().get(knxComObject.getNumber());
            final DataSerializer<Object> dataSerializer = dataSerializerMap
                    .get(knxComObject.getDataPointSubtype(knxProject).getFormat());
            return dataSerializer.serializeToBytes(value);

        default:
            final String msg = "Unknown knxDatapointType: " + dataPointType.getId();
            LOG.error(msg);
//            throw new ObjectServerException(msg);
            return null;
        }

    }

    public KNXProject getKnxProject() {
        return knxProject;
    }

    public void setKnxProject(final KNXProject knxProject) {
        this.knxProject = knxProject;
    }

    public Map<String, DataSerializer<Object>> getDataSerializerMap() {
        return dataSerializerMap;
    }

    public void setDataSerializerMap(final Map<String, DataSerializer<Object>> dataSerializerMap) {
        this.dataSerializerMap = dataSerializerMap;
    }

}

package project.parsing.knx.steps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.project.KNXComObject;
import api.project.KNXDatapointSubtype;
import api.project.KNXDatapointType;
import api.project.KNXDeviceInstance;
import api.project.KNXGroupAddress;
import api.project.KNXProject;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class OutputParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(OutputParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		final KNXProject knxProject = context.getKnxProject();

		if (knxProject == null) {
			return;
		}

		for (final KNXDeviceInstance knxDeviceInstance : knxProject.getDeviceInstances()) {

			LOG.info("KNXDeviceInstance: " + knxDeviceInstance.getId() + " " + knxDeviceInstance.getAddress());

			LOG.info("ManufacturerId: " + knxDeviceInstance.getManufacturerId() + " ("
					+ context.getKnxProject().getManufacturerMap().get(knxDeviceInstance.getManufacturerId()).getName()
					+ ")");
			LOG.info("HardwareId: " + knxDeviceInstance.getHardwareId());
			LOG.info("ProductId: " + knxDeviceInstance.getProductId());

			final Collection<KNXComObject> values = knxDeviceInstance.getComObjects().values();
			final List<KNXComObject> valueList = new ArrayList<KNXComObject>(values);

			Collections.sort(valueList, new Comparator<KNXComObject>() {
				@Override
				public int compare(final KNXComObject lhs, final KNXComObject rhs) {
					return lhs.getNumber() - rhs.getNumber();
				}
			});

			for (final KNXComObject knxComObject : valueList) {

				if (!knxComObject.isGroupObject()) {
					continue;
				}

				final StringBuilder stringBuilder = new StringBuilder();

				if (knxComObject.isGroupObject()) {
					stringBuilder.append("[GroupObject] ");
				}

				// number and text
				stringBuilder.append(knxComObject.getNumber()).append(" ").append(knxComObject.getId());

				if (StringUtils.isNotBlank(knxComObject.getText())) {
					stringBuilder.append(" ").append(knxComObject.getText());
				}

				// hardware information
				stringBuilder.append(" ").append(knxComObject.getHardwareName()).append(" ")
						.append(knxComObject.getHardwareText());

				// group address
				if (knxComObject.getKnxGroupAddress() != null) {
					stringBuilder.append(" ").append(knxComObject.getKnxGroupAddress().getGroupAddress());
				}

				// data point type
				final KNXGroupAddress knxGroupAddress = knxComObject.getKnxGroupAddress();
				if (knxGroupAddress != null) {

					final String dataPointTypeId = knxGroupAddress.getDataPointType();

					final KNXDatapointSubtype knxDatapointSubtype = knxProject.getDatapointSubtypeMap()
							.get(dataPointTypeId);

					final Map<String, String> languageMap = knxProject.getLanguageStoreMap().get("de-DE");
					final KNXDatapointType knxDatapointType = knxDatapointSubtype.getKnxDatapointType();
					final String datapointTranslated = languageMap.get(knxDatapointType.getId());
					final String datapointSubtypeTranslated = languageMap.get(knxDatapointSubtype.getId());

					stringBuilder.append(" ").append(knxDatapointType.getName()).append(" ")
							.append(knxDatapointSubtype.getNumber()).append(" ").append(datapointTranslated)
							.append(", ").append(datapointSubtypeTranslated).append(" ")
							.append(knxDatapointSubtype.getFormat());
				}

				LOG.info(stringBuilder.toString());
			}
		}
	}

}

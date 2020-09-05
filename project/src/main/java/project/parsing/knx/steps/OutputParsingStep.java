package project.parsing.knx.steps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.project.KNXComObject;
import api.project.KNXDeviceInstance;
import api.project.KNXProject;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

/**
 * This step dumps information to the console that was parsed in prior steps.
 */
public class OutputParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(OutputParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		final KNXProject knxProject = context.getKnxProject();

		if (knxProject == null) {

			LOG.warn("No project found in the context, aborting!");
			return;
		}

		int index = 0;
		for (final KNXDeviceInstance knxDeviceInstance : knxProject.getDeviceInstances()) {

			LOG.info("");
			LOG.info("Device Index: {}", index);
			LOG.info("KNXDeviceInstance: " + knxDeviceInstance.getId() + " " + knxDeviceInstance.getAddress());

			LOG.info("ManufacturerId: " + knxDeviceInstance.getManufacturerId() + " ("
					+ context.getKnxProject().getManufacturerMap().get(knxDeviceInstance.getManufacturerId()).getName()
					+ ")");
			LOG.info("HardwareId: " + knxDeviceInstance.getHardwareId());
			LOG.info("ProductId: " + knxDeviceInstance.getProductId());

			final Collection<KNXComObject> values = knxDeviceInstance.getComObjects().values();

			// sort the KNXComObjects by number ascending
			final List<KNXComObject> valueList = new ArrayList<KNXComObject>(values);
			Collections.sort(valueList, new Comparator<KNXComObject>() {
				@Override
				public int compare(final KNXComObject lhs, final KNXComObject rhs) {
					return lhs.getNumber() - rhs.getNumber();
				}
			});

			// output the KNXComObjects
			for (final KNXComObject knxComObject : valueList) {

				if (!knxComObject.isGroupObject()) {
					continue;
				}

				LOG.info(knxComObject.toString());
			}

			index++;
		}
	}

}

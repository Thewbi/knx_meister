package common.utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import api.project.KNXComObject;
import api.project.KNXDatapointSubtype;
import api.project.KNXDeviceInstance;
import api.project.KNXGroupAddress;
import api.project.KNXProject;

public class KNXProjectUtils {

	private KNXProjectUtils() {
		// no instances of this class
	}

	public static KNXGroupAddress retrieveGroupAddress(final KNXProject knxProject, final int deviceIndex,
			final int dataPointId) {

//		// TODO: how to identify the correct device if there are several devices in the
//		// list?
//		final KNXDeviceInstance knxDeviceInstance = knxProject.getDeviceInstances().get(0);
//
//		// pick one of the communication objects by its name/id
//		final KNXComObject knxComObject = knxDeviceInstance.getComObjects().get(comObjectId);

		final Optional<KNXComObject> knxComObjectOptional = retrieveComObjectByDatapointId(knxProject, deviceIndex,
				dataPointId);

		if (!knxComObjectOptional.isPresent()) {
			return null;
		}

		// retrieve the group address to send the data to
		return knxComObjectOptional.get().getKnxGroupAddress();
	}

	public static KNXDatapointSubtype retrieveDataPointSubType(final KNXProject knxProject, final int deviceIndex,
			final int dataPointId) {

//		// TODO: how to identify the correct device if there are several devices in the
//		// list?
//		final KNXDeviceInstance knxDeviceInstance = knxProject.getDeviceInstances().get(0);
//
//		// pick one of the communication objects by its name/id
//		final KNXComObject knxComObject = knxDeviceInstance.getComObjects().get(comObjectId);
//
//		// retrieve the group address to send the data to
//		final KNXGroupAddress knxGroupAddress = knxComObject.getKnxGroupAddress();

		final KNXGroupAddress knxGroupAddress = retrieveGroupAddress(knxProject, deviceIndex, dataPointId);

		// the group address also stores the datatype. The data has to be send in this
		// specific datatype so the receiver can decode it correctly
		final String dataPointType = knxGroupAddress.getDataPointType();

		// retrieve the datapoint subtype because the datapoint subtype stores datapoint
		// type
		return knxProject.getDatapointSubtypeMap().get(dataPointType);
	}

	public static List<KNXComObject> retrieveComObjectListByDatapointId(final KNXProject knxProject,
			final int dataPointId) {

		// TODO: how to identify the correct device if there are several devices in the
		// list?
		final KNXDeviceInstance knxDeviceInstance = knxProject.getDeviceInstances().get(0);

		// TODO: maybe create a map from datapoint id to ComObject????
		final List<KNXComObject> knxComObjects = knxDeviceInstance.getComObjects().values().stream()
				.filter(c -> c.getNumber() == dataPointId).filter(c -> c.isGroupObject()).collect(Collectors.toList());

		return knxComObjects;
	}

	public static Optional<KNXComObject> retrieveComObjectByDatapointId(final KNXProject knxProject,
			final int deviceIndex, final int dataPointId) {

		// TODO: how to identify the correct device if there are several devices in the
		// list?
		final KNXDeviceInstance knxDeviceInstance = knxProject.getDeviceInstances().get(deviceIndex);

		// TODO: maybe create a map from datapoint id to ComObject????
		return knxDeviceInstance.getComObjects().values().stream().filter(c -> c.getNumber() == dataPointId)
				.filter(c -> c.isGroupObject()).findFirst();
	}

}

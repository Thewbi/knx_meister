package project.parsing.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class KNXDeviceInstance {

	private String id;

	private String productRefId;

	private String address;

	private KNXProduct knxProduct;

	private final Map<String, KNXComObject> comObjects = new HashMap<>();

	private Set<String> groupObjectInstancesSet;

	public String getAddress() {
		return address;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, KNXComObject> getComObjects() {
		return comObjects;
	}

	public String getProductRefId() {
		return productRefId;
	}

	public void setProductRefId(final String productRefId) {
		this.productRefId = productRefId;

	}

	public String getManufacturerId() {

		if (StringUtils.isBlank(productRefId)) {
			return StringUtils.EMPTY;
		}

		return productRefId.split("_")[0];
	}

	public String getHardwareId() {

		if (StringUtils.isBlank(productRefId)) {
			return StringUtils.EMPTY;
		}

		return productRefId.split("_")[1];
	}

	public String getProductId() {

		if (StringUtils.isBlank(productRefId)) {
			return StringUtils.EMPTY;
		}

		return productRefId.split("_")[2];
	}

	public KNXProduct getKnxProduct() {
		return knxProduct;
	}

	public void setKnxProduct(final KNXProduct knxProduct) {
		this.knxProduct = knxProduct;
	}

	public Set<String> getGroupObjectInstancesSet() {
		return groupObjectInstancesSet;
	}

	public void setGroupObjectInstancesSet(final Set<String> groupObjectInstancesSet) {
		this.groupObjectInstancesSet = groupObjectInstancesSet;
	}

}

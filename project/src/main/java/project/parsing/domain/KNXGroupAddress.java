package project.parsing.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class KNXGroupAddress {

	private String id;

	private String name;

	private int rangeStart;

	private int address;

	private int addressComponent;

	private String dataPointType;

	private final List<KNXGroupAddress> knxGroupAddresses = new ArrayList<>();

	private KNXGroupAddress parentKNXGroupAddress;

	public String getGroupAddress() {

		if (parentKNXGroupAddress != null) {
			final String parentGroupAddress = parentKNXGroupAddress.getGroupAddress();
			return StringUtils.isBlank(parentGroupAddress) ? Integer.toString(addressComponent)
					: parentGroupAddress + "/" + addressComponent;
		}

		return StringUtils.EMPTY;
	}

	public void assignAddresses() {

		int index = 0;
		for (final KNXGroupAddress knxGroupAddress : knxGroupAddresses) {

			knxGroupAddress.setAddressComponent(index);
			index++;

			knxGroupAddress.assignAddresses();
		}
	}

	public void sortChildren() {
		Collections.sort(knxGroupAddresses, new Comparator<KNXGroupAddress>() {

			@Override
			public int compare(final KNXGroupAddress lhs, final KNXGroupAddress rhs) {
				return lhs.getRangeStart() - rhs.getRangeStart();
			}

		});
		Collections.sort(knxGroupAddresses, new Comparator<KNXGroupAddress>() {

			@Override
			public int compare(final KNXGroupAddress lhs, final KNXGroupAddress rhs) {
				return lhs.getAddress() - rhs.getAddress();
			}

		});

		for (final KNXGroupAddress knxGroupAddress : knxGroupAddresses) {
			knxGroupAddress.sortChildren();
		}
	}

	public void dump() {

		System.out.println(getId() + " " + getGroupAddress() + " " + getName());

		for (final KNXGroupAddress knxGroupAddress : knxGroupAddresses) {
			knxGroupAddress.dump();
		}
	}

	public List<KNXGroupAddress> getKNXGroupAddresses() {
		return knxGroupAddresses;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public int getRangeStart() {
		return rangeStart;
	}

	public void setRangeStart(final int rangeStart) {
		this.rangeStart = rangeStart;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(final int address) {
		this.address = address;
	}

	public String getDataPointType() {
		return dataPointType;
	}

	public void setDataPointType(final String dataPointType) {
		this.dataPointType = dataPointType;
	}

	public KNXGroupAddress getParentKNXGroupAddress() {
		return parentKNXGroupAddress;
	}

	public void setParentKNXGroupAddress(final KNXGroupAddress parentKNXGroupAddress) {
		this.parentKNXGroupAddress = parentKNXGroupAddress;
	}

	public int getAddressComponent() {
		return addressComponent;
	}

	public void setAddressComponent(final int addressComponent) {
		this.addressComponent = addressComponent;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}

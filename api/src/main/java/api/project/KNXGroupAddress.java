package api.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parsed from a ETS5 project, group addresses are contained in a tree where
 * each parent layer stores a single digit of a group address.<br />
 * <br />
 *
 * For a three level group address, there will be a tree of group addresses of
 * depth of at least three.<br />
 * <br />
 *
 * For convenience, this class has also a string member groupAddress where you
 * can assign a group address directly. If the KNXGroupAddress instance has no
 * parent, then this string member is used directly.<br />
 * <br />
 */
public class KNXGroupAddress {

	private static final Logger LOG = LogManager.getLogger(KNXGroupAddress.class);

	private String id;

	private String name;

	private int rangeStart;

	private int address;

	private int addressComponent;

	private String dataPointType;

	private String groupAddress;

	private Object value;

	private final List<KNXGroupAddress> knxGroupAddresses = new ArrayList<>();

	private KNXGroupAddress parentKNXGroupAddress;

	public String getGroupAddress() {

		if (parentKNXGroupAddress != null) {
			final String parentGroupAddress = parentKNXGroupAddress.getGroupAddress();
			return StringUtils.isBlank(parentGroupAddress) ? Integer.toString(addressComponent)
					: parentGroupAddress + "/" + addressComponent;
		}

		if (StringUtils.isNotBlank(groupAddress)) {
			return groupAddress;
		}

		return StringUtils.EMPTY;
	}

	public void setGroupAddress(final String groupAddress) {
		this.groupAddress = groupAddress;
	}

	public void assignAddresses() {
		assignAddresses(0);
	}

	public void assignAddresses(final int level) {

//		int index = level == 2 ? 1 : 0;
		int index = 0;
		for (final KNXGroupAddress knxGroupAddress : knxGroupAddresses) {

			knxGroupAddress.setAddressComponent(index);
			index++;

			// recurse
			knxGroupAddress.assignAddresses(level + 1);
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
		LOG.info("ID=" + getId() + " GroupAddress=" + getGroupAddress() + " Name=" + getName());
		for (final KNXGroupAddress knxGroupAddress : knxGroupAddresses) {
			knxGroupAddress.dump();
		}
	}

	@Override
	public String toString() {
		return getGroupAddress();
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

	public Object getValue() {
		return value;
	}

	public void setValue(final Object value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((knxGroupAddresses == null) ? 0 : knxGroupAddresses.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final KNXGroupAddress other = (KNXGroupAddress) obj;
		if (knxGroupAddresses == null) {
			if (other.knxGroupAddresses != null)
				return false;
		} else if (!knxGroupAddresses.equals(other.knxGroupAddresses))
			return false;
		return true;
	}

}

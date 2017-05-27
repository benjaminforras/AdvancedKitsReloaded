package hu.tryharddevs.advancedkits.kits.flags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListFlag<T> extends Flag<ArrayList<T>> {
	private final Flag<T> subFlag;

	public ListFlag(String name, Flag<T> subFlag) {
		super(name);
		this.subFlag = subFlag;
	}

	public Flag<T> getType() {
		return subFlag;
	}

	@Override public ArrayList<T> getDefault() {
		return new ArrayList<>();
	}

	@Override public ArrayList<T> parseInput(String input) throws InvalidFlagValueException {
		if (input.isEmpty()) {
			return new ArrayList<>();
		} else {
			ArrayList<T> items = new ArrayList<>();

			for (String str : input.split(",")) {
				items.add(subFlag.parseInput(str));
			}
			return items;
		}
	}

	@Override public ArrayList<T> unmarshal(Object o) {
		if (o instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) o;
			ArrayList<T>  items      = new ArrayList<>();

			for (Object sub : collection) {
				T item = subFlag.unmarshal(sub);
				if (item != null) {
					items.add(item);
				}
			}

			return items;
		} else {
			return null;
		}
	}

	@Override public Object marshal(ArrayList<T> o) {
		List<Object> list = new ArrayList<>();
		for (T item : o) {
			list.add(subFlag.marshal(item));
		}

		return list;
	}
}

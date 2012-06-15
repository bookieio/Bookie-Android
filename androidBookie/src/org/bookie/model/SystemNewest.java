package org.bookie.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class SystemNewest extends Observable {
	private static SystemNewest singleton;

	private List<BookMark> bmarks;


	private SystemNewest() {
		super();
		bmarks = new ArrayList<BookMark>();
	}

	public static SystemNewest getSystemNewest() {
		if(singleton == null) singleton = new SystemNewest();
		return singleton;
	}

	public void updateList(List<BookMark> updated) {
		bmarks = new ArrayList<BookMark>(updated);
		hasChanged();
		notifyObservers();
	}

	public List<BookMark> getList() {
		return bmarks;
	}


}

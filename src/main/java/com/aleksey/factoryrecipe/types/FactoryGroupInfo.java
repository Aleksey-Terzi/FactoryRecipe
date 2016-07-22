package com.aleksey.factoryrecipe.types;

import java.util.ArrayList;
import java.util.List;

public class FactoryGroupInfo {
	public String name;
	public String anchor;
	
	public List<FactoryInfo> factories;
	
	public FactoryGroupInfo(String name, String anchor) {
		this.name = name;
		this.anchor = anchor;
		this.factories = new ArrayList<FactoryInfo>();
	}
}

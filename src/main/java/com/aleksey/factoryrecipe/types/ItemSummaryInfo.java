package com.aleksey.factoryrecipe.types;

import java.util.ArrayList;
import java.util.List;

public class ItemSummaryInfo implements Comparable<ItemSummaryInfo> {
	public String name;
	public String anchor;
	public List<FactoryInfo> inputFactories;
	public List<FactoryInfo> outputFactories;
	public List<FactoryInfo> randomFactories;
	
	public ItemSummaryInfo(String name, String anchor) {
		this.name = name;
		this.anchor = anchor;
		this.inputFactories = new ArrayList<FactoryInfo>();
		this.outputFactories = new ArrayList<FactoryInfo>();
		this.randomFactories = new ArrayList<FactoryInfo>();
	}
	
	public int compareTo(ItemSummaryInfo o) {
		return this.name.compareTo(o.name);
	}
}
package com.aleksey.factoryrecipe.types;

import java.util.ArrayList;
import java.util.List;

public class RecipeInfo {
	public List<ItemInfo> output;
	public List<ItemInfo> input;
	public List<String> inputExcluded;
	public int time;
	public String type;
	public String name;
	public int fuelConsumptionInterval;
	public int weight;
	public String link;
	public String anchor;
	
	public RecipeInfo() {
		this.output = new ArrayList<ItemInfo>();
		this.input = new ArrayList<ItemInfo>();
		this.inputExcluded = new ArrayList<String>();
	}
}

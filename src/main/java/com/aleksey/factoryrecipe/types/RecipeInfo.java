package com.aleksey.factoryrecipe.types;

import java.util.ArrayList;
import java.util.List;

public class RecipeInfo implements Comparable<RecipeInfo> {
	public List<ItemInfo> output;
	public List<ItemInfo> input;
	public ItemInfo fuel;
	public List<String> inputExcluded;
	public int time;
	public String type;
	public String name;
	public int weight;
	public String link;
	public String anchor;
	
	public RecipeInfo() {
		this.output = new ArrayList<ItemInfo>();
		this.input = new ArrayList<ItemInfo>();
		this.inputExcluded = new ArrayList<String>();
	}
	
	public int compareTo(RecipeInfo o) {
		int cmp = getTypeSequence(o.type).compareTo(getTypeSequence(type));
		
		if(cmp != 0 || this.output.size() == 0 || o.output.size() == 0) {
			return cmp;
		}
		
		return this.output.get(0).name.compareTo(o.output.get(0).name);
	}
	
	private static Integer getTypeSequence(String type) {
		if(type.equalsIgnoreCase("SETUP")) return 0;
		if(type.equalsIgnoreCase("PRODUCTION")) return 1;
		if(type.equalsIgnoreCase("PYLON")) return 2;
		if(type.equalsIgnoreCase("ENCHANT")) return 3;
		if(type.equalsIgnoreCase("RANDOM")) return 4;
		if(type.equalsIgnoreCase("COMPACT")) return 5;
		if(type.equalsIgnoreCase("DECOMPACT")) return 6;
		if(type.equalsIgnoreCase("REPAIR")) return 7;
		if(type.equalsIgnoreCase("UPGRADE")) return 8;
		
		return 9;
	}
}

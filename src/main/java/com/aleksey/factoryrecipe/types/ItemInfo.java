package com.aleksey.factoryrecipe.types;

public class ItemInfo {
	public int amount;
	public String rawName;
	public String name;
	
	public ItemInfo(String rawName, String name, int amount) {
		this.amount = amount;
		this.rawName = rawName;
		this.name = name;
	}
}

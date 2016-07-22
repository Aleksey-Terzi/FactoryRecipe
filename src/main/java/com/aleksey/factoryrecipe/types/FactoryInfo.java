package com.aleksey.factoryrecipe.types;

import java.util.ArrayList;
import java.util.List;

public class FactoryInfo {
	public String name;
	public String anchor;
	public String upgradeLink;
	
	public List<FactoryInfo> levels;
	public List<RecipeInfo> recipes;
	
	public FactoryInfo(String name, String anchor, String upgradeLink) {
		this.name = name;
		this.anchor = anchor;
		this.upgradeLink = upgradeLink;
		this.levels = new ArrayList<FactoryInfo>();
		this.recipes = new ArrayList<RecipeInfo>();
	}
}

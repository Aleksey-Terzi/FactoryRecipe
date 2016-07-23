package com.aleksey.factoryrecipe.utils;

public class LinkHelper {
	public static String getAnchor(String name) {
		return name == null ? null: name.toLowerCase().replace(' ', '-');
	}
}

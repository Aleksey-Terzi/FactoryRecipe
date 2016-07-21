package com.aleksey.factoryrecipe.types;

import java.util.ArrayList;
import java.util.List;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;

public class Node implements Comparable<Node> {
	public Node parent;
	public List<Node> children;
	
	public FurnCraftChestEgg fcc;
	
	public Node(Node parent, FurnCraftChestEgg fcc) {
		this.parent = parent;
		this.children = new ArrayList<Node>();
		this.fcc = fcc;
		
		if(this.parent != null) {
			this.parent.children.add(this);
		}
	}
	
	public void setParent(Node parent) {
		if(this.parent == parent) return;
		
		this.parent.children.remove(this);
		
		this.parent = parent;
		this.parent.children.add(this);
	}
	
	public int compareTo(Node o) {
		return this.fcc.getName().compareTo(o.fcc.getName());
	}
}

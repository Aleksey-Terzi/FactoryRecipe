package com.aleksey.factoryrecipe.creation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.aleksey.factoryrecipe.types.FactoryInfo;
import com.aleksey.factoryrecipe.types.ItemSummaryInfo;
import com.aleksey.factoryrecipe.utils.ResourceHelper;

public class ItemSummaryFileCreator {
	private FactoryListCreator factoryListCreator;
	private String title;
	private List<ItemSummaryInfo> itemSummaryList;
	private PrintWriter writer;
	
	public boolean create(FactoryListCreator factoryListCreator, String title) {
		this.factoryListCreator = factoryListCreator;
		this.title = title;
		
		createItemSummaryList();
		
		try {
			this.writer = new PrintWriter("item-summary.html", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
        
        try {
        	writeFile();
        } finally {
        	this.writer.close();
        }
        
        return true;
	}
	
	private void createItemSummaryList() {
		HashMap<String, ItemSummaryInfo> itemSummaryMap = this.factoryListCreator.getItemSummaryMap();
		
		this.itemSummaryList = new ArrayList<ItemSummaryInfo>();
		
		for(ItemSummaryInfo info : itemSummaryMap.values()) {
			this.itemSummaryList.add(info);
		}
		
		Collections.sort(this.itemSummaryList);
	}
	
	private void writeFile() {
		String style = ResourceHelper.readText("/index_style.txt");
		
		this.writer.println("<!DOCTYPE html>");
		this.writer.println("<html>");
		this.writer.println("<head lang=\"en\">");
		this.writer.println("<title>Recipes for Factory plugin of " + this.title + "</title>");
		this.writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
		this.writer.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" />");
		
		this.writer.write(style);
		
		this.writer.println("</head>");
		this.writer.println("<body>");
		
		this.writer.println("<div class=main>");
		this.writer.println("<h1>Summary by Items</h1>");
		
		writeSummary();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		this.writer.println("<p>Generated on " + format.format(new Date()) + "</p>");
		
		this.writer.println("</div>");
		
		this.writer.println("</body>");
		this.writer.println("</html>");
	}
	
	private void writeSummary() {
		this.writer.println("<table class=recipe-list>");
		this.writer.println("<tr><th>Item</th><th>Where Produced</th><th>Where Used</th></tr>");
		
		for(ItemSummaryInfo itemSummaryInfo : this.itemSummaryList) {
			this.writer.println("<tr>");
			
			this.writer.println("<td>");
			this.writer.println("<a id=" + itemSummaryInfo.anchor + "></a>");
			this.writer.println(itemSummaryInfo.name);
			this.writer.println("</td>");
			
			this.writer.println("<td class=output><ul>");
			writeFactoryList(itemSummaryInfo.outputFactories, false);
			writeFactoryList(itemSummaryInfo.randomFactories, true);
			this.writer.println("</ul></td>");
			
			this.writer.println("<td class=input><ul>");
			writeFactoryList(itemSummaryInfo.inputFactories, false);
			this.writer.println("</ul></td>");
			
			this.writer.println("</tr>");
		}
		
		this.writer.println("</table>");
	}
	
	private void writeFactoryList(List<FactoryInfo> factories, boolean isRandom) {
		for(FactoryInfo factoryInfo : factories) {
			this.writer.println("<li>");
			this.writer.println("<a href='index.html#" + factoryInfo.anchor + "'>");
			
			this.writer.println(factoryInfo.rawName);
			
			if(isRandom) {
				this.writer.println(" (RANDOM)");
			}
			
			this.writer.println("</a>");
			
			this.writer.println("</li>");
		}
	}
}

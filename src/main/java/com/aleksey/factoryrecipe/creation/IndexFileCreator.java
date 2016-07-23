package com.aleksey.factoryrecipe.creation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.aleksey.factoryrecipe.types.FactoryGroupInfo;
import com.aleksey.factoryrecipe.types.FactoryInfo;
import com.aleksey.factoryrecipe.types.ItemInfo;
import com.aleksey.factoryrecipe.types.RecipeInfo;
import com.aleksey.factoryrecipe.utils.ResourceHelper;

public class IndexFileCreator {
	private FactoryListCreator factoryListCreator; 
	private PrintWriter writer;
	
	public boolean create() {
		this.factoryListCreator = new FactoryListCreator();
		this.factoryListCreator.create();
		
		try {
			this.writer = new PrintWriter("index.html", "UTF-8");
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
	
	private void writeFile() {
		String style = ResourceHelper.readText("/index_style.txt");
		
		this.writer.println("<!DOCTYPE html>");
		this.writer.println("<html>");
		this.writer.println("<head lang=\"en\">");
		this.writer.println("<title>Recipes for Factory plugin of CivCraft: Worlds</title>");
		this.writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
		this.writer.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" />");
		
		this.writer.write(style);
		
		this.writer.println("</head>");
		this.writer.println("<body>");
		
		this.writer.println("<div class=main>");
		this.writer.println("<h1>Recipes for Factory plugin of CivCraft: Worlds</h1>");
		
		writeContent();
		
		for(FactoryGroupInfo group : this.factoryListCreator.getGroups()) {
			writeFactoryGroup(group);
		}
		
		for(String item : this.factoryListCreator.getBadItems()) {
			this.writer.println("<div><strong>" + item + "</strong></div>");
		}
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		this.writer.println("<p>Generated on " + format.format(new Date()) + "</p>");
		
		this.writer.println("</div>");
		
		this.writer.println("</body>");
		this.writer.println("</html>");
	}
	
	private void writeContent() {
		this.writer.println("<h2>Table of Contents</h2>");
		this.writer.println("<ul>");
		
		for(FactoryGroupInfo group : this.factoryListCreator.getGroups()) {
			this.writer.println("<li>");

			this.writer.println("<a href='#" + group.anchor + "'>" + group.name + "</a>");
			
			if(group.factories.size() > 1) {
				this.writer.println("<ul>");
				
				for(FactoryInfo factoryInfo : group.factories) {
					String anchor = factoryInfo.levels.size() == 0 ? factoryInfo.anchor: factoryInfo.levels.get(0).anchor;
					
					this.writer.println("<li>");
					this.writer.println("<a href='#" + anchor + "'>" + factoryInfo.name + "</a>");
					this.writer.println("</li>");
				}
				
				this.writer.println("</ul>");
			}
			
			this.writer.println("</li>");
		}
		
		this.writer.println("</ul>");
	}
	
	private void writeFactoryGroup(FactoryGroupInfo group) {
		this.writer.println("<div>");
		this.writer.println("<a id=" + group.anchor + "></a>");
		this.writer.println("<h2>" + group.name + "</h2>");
		this.writer.println("</div>");
		
		for(FactoryInfo factoryInfo: group.factories) {
			writeFactory(factoryInfo);
		}
	}
	
	private void writeFactory(FactoryInfo factoryInfo) {
		boolean hasWeightColumn = factoryInfo.name != null && factoryInfo.name.toLowerCase().indexOf("pylon") >= 0;
		
		this.writer.println("<div>");
		
		if(factoryInfo.anchor != null) {
			this.writer.println("<a id=" + factoryInfo.anchor + "></a>");
		}
		
		if(factoryInfo.name != null) {
			this.writer.println("<div class=factory-header>");
			this.writer.println("<div><h3>" + factoryInfo.name + "</div></h3>");
			this.writer.println("<div>(<a href='" + factoryInfo.upgradeLink + "'>Show Upgrade Recipe</a>)</div>");
			this.writer.println("</div>");
		}
		
		this.writer.println("<table class=recipe-list>");
		this.writer.println("<tr>");
		
		if(hasWeightColumn) {
			this.writer.println("<th>Output</th><th>Input</th><th>Production Time</th><th>Weight<th>Type</th><th>Notes</th>");
		} else {
			this.writer.println("<th>Output</th><th>Input</th><th>Production Time</th><th>Type</th><th>Notes</th>");
		}
		
		this.writer.println("</tr>");

		if(factoryInfo.recipes.size() > 0) {
			writeRecipeList(factoryInfo, hasWeightColumn, false);
		} else {
			for(FactoryInfo level : factoryInfo.levels) {
				writeRecipeList(level, hasWeightColumn, true);
			}
		}
		
		this.writer.println("</table>");
		
		this.writer.println("</div>");
	}
	
	private void writeRecipeList(
			FactoryInfo factoryInfo,
			boolean hasWeightColumn,
			boolean showLevel
			)
	{
		if(showLevel) {
			int colspan = hasWeightColumn ? 6: 5;
			
			this.writer.println("<tr><th colspan=" + colspan + ">");
			this.writer.println("<a id=" + factoryInfo.anchor + "></a>");
			this.writer.println(factoryInfo.name);
			this.writer.println("</th></tr>");
		}
		
		for(RecipeInfo recipeInfo : factoryInfo.recipes) {
			writeRecipe(recipeInfo, hasWeightColumn);
		}
	}
	
	private void writeRecipe(RecipeInfo info, boolean hasWeightColumn) {
		this.writer.println("<tr>");
		
		//Output
		this.writer.println("<td>");
		writeItemList(info.output);
		this.writer.println("</td>");
		
		//Input
		this.writer.println("<td>");

		if(info.input.size() > 0) {
			this.writer.println("<span class=item-section>Ingredients:</span>");
			writeItemList(info.input);
		}
		
		if(info.fuel != null) {
			this.writer.println("<span class=item-section>Fuel:</span>");
		
			List<ItemInfo> list = new ArrayList<ItemInfo>();
			list.add(info.fuel);
			
			writeItemList(list);
		}
		
		this.writer.println("</td>");

		this.writer.println("<td class=prod-time>" + getTimeText(info.time) + "</td>");
		
		if(hasWeightColumn) {
			String weight = info.weight > 0 ? Integer.toString(info.weight): "";
			
			this.writer.println("<td class=weight>" + weight + "</td>");
		}
		
		this.writer.println("<td>" + info.type + "</td>");
		
		//Input
		this.writer.println("<td>");
		writeNotes(info);
		this.writer.println("</td>");
		
		this.writer.println("</tr>");
	}

	private void writeItemList(List<ItemInfo> items) {
		if(items.size() == 0) return;
		
		this.writer.println("<table class=item-list>");
		
		for(ItemInfo info : items) {
			this.writer.println("<tr>");
			this.writer.print("<td>" + info.amount + "</td>");
			this.writer.print("<td>x</td>");
			this.writer.print("<td>" + info.name + "</td>");
			this.writer.println("</tr>");
		}
		
		this.writer.println("</table>");
	}
	
	private void writeNotes(RecipeInfo info) {
		if(info.anchor != null) {
			this.writer.println("<a id='" + info.anchor + "'></a>");
		}
		
		if(info.link != null) {
			this.writer.println("<a href='#" + info.link + "'>");
		}
		
		this.writer.println(info.name);
		
		if(info.link != null) {
			this.writer.println("</a>");
		}
		
		if(info.inputExcluded.size() > 0) {
			this.writer.println("<div>");
			this.writer.println("<strong>Excluded:</strong>");
			
			for(String mat : info.inputExcluded) {
				this.writer.println("<div> - " + mat + "</div>");
			}
			
			this.writer.println("</div>");
		}
	}
	
	private static String getTimeText(int time) {
		int totalSeconds = time / 20;
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 60 / 60;
		
		if(hours > 0) {
			if(seconds > 0) {
				return Integer.toString(hours)
						+ ":" + StringUtils.leftPad(Integer.toString(minutes), 2, '0')
						+ ":" + StringUtils.leftPad(Integer.toString(seconds), 2, '0')
						+ " hr";
			}
			else if(minutes > 0) {
				return Integer.toString(hours)
						+ ":" + StringUtils.leftPad(Integer.toString(minutes), 2, '0')
						+ " hr";
			}
			
			return Integer.toString(hours) + " hr";
		}
		else if(minutes > 0) {
			if(seconds > 0) {
				return Integer.toString(minutes)
						+ ":" + StringUtils.leftPad(Integer.toString(seconds), 2, '0')
						+ " min";
			}
			
			return Integer.toString(minutes) + " min";
		}
		
		return Integer.toString(seconds) + " sec";
	}
}

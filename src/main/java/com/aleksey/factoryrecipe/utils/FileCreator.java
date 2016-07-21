package com.aleksey.factoryrecipe.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.aleksey.factoryrecipe.types.ItemInfo;
import com.aleksey.factoryrecipe.types.Node;
import com.aleksey.factoryrecipe.types.RecipeInfo;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.recipes.AOERepairRecipe;
import com.github.igotyou.FactoryMod.recipes.CompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.DecompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.DeterministicEnchantingRecipe;
import com.github.igotyou.FactoryMod.recipes.FactoryMaterialReturnRecipe;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.recipes.PylonRecipe;
import com.github.igotyou.FactoryMod.recipes.RandomOutputRecipe;
import com.github.igotyou.FactoryMod.recipes.RepairRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.google.common.base.Objects;

public class FileCreator {
	private static HashMap<String, String> _nameReplacements;

	private PrintWriter writer;
	private HashSet<String> _badItems;
	
	public FileCreator() {
		if(_nameReplacements != null) return;
		
		_nameReplacements = new HashMap<String, String>();
		_nameReplacements.put("LOG:-1", "Any Normal Wood Log");
		_nameReplacements.put("LOG_2:-1", "Acacia or Dark Oak Log");
		_nameReplacements.put("SAPLING:-1", "Any Sapling");
		_nameReplacements.put("WOOD:-1", "Any Wood Plank");
		_nameReplacements.put("STONE:-1", "Any Stone");
		_nameReplacements.put("SAND:-1", "Any Sand");
		_nameReplacements.put("SPONGE:0", "Bastion");
	}
	
	public boolean create() {
		Node root = createFactoryTree();
		
		try {
			this.writer = new PrintWriter("recipes.html", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return true;
		}
        
        try {
        	writeFile(root);
        } finally {
        	this.writer.close();
        }
        
        return true;
	}
	
	private Node createFactoryTree() {
		FactoryModManager manager = FactoryMod.getManager();
		HashMap<String, IFactoryEgg> eggs = manager.getAllEggs();

		Node root = new Node(null, null);
		HashMap<FurnCraftChestEgg, Node> nodeMap = new HashMap<FurnCraftChestEgg, Node>(); 
		
		for(IFactoryEgg egg : eggs.values()) {
			if(!(egg instanceof FurnCraftChestEgg)) continue;
			
			FurnCraftChestEgg fcc = (FurnCraftChestEgg)egg;
			
			Node fccNode = nodeMap.get(fcc);
			
			if(fccNode == null) {
				fccNode = new Node(root, fcc);
				nodeMap.put(fcc, fccNode);
			}
			
			List<FurnCraftChestEgg> childFactories = getChildFactories(fcc);
			
			for(FurnCraftChestEgg childFcc : childFactories) {
				Node childNode = nodeMap.get(childFcc);
				
				if(childNode != null) {
					childNode.setParent(fccNode);
				} else {
					childNode = new Node(fccNode, childFcc);
					nodeMap.put(childFcc, childNode);
				}
			}
		}
		
		sortNodes(root);
		
		return root;
	}
	
	private void sortNodes(Node parent) {
		Collections.sort(parent.children);
		
		for(Node child : parent.children) {
			sortNodes(child);
		}
	}
	
	private static List<FurnCraftChestEgg> getChildFactories(FurnCraftChestEgg parent) {
		ArrayList<FurnCraftChestEgg> result = new ArrayList<FurnCraftChestEgg>();
		
		for(IRecipe recipe : parent.getRecipes()) {
			if((recipe instanceof Upgraderecipe)) {
				IFactoryEgg egg = ((Upgraderecipe)recipe).getEgg();
				
				if(egg instanceof FurnCraftChestEgg) {
					result.add((FurnCraftChestEgg)egg);
				}
			}
		}
		
		return result;
	}
	
	private void writeFile(Node root) {
		_badItems = new HashSet<String>();
		
		this.writer.println("<!DOCTYPE html>");
		this.writer.println("<html>");
		this.writer.println("<head lang=\"en\">");
		this.writer.println("<title>Recipes for Factory plugin of CivCraft: Worlds</title>");
		this.writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
		this.writer.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" />");
		
		this.writer.println("<style>");
		this.writer.println("body {");
		this.writer.println("    font-family: \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\";");
		this.writer.println("	color: #333;");
		this.writer.println("}");
		this.writer.println("h2 { border-bottom: 1px solid #eee; }");
		this.writer.println("table td { vertical-align: top; }");
		this.writer.println("div.main { margin:auto; width:1020px; }");
		this.writer.println("table.recipe-list { border-collapse: collapse; border: 1px solid #ddd; width:100%; }");
		this.writer.println("table.recipe-list > tbody > tr > td,");
		this.writer.println("table.recipe-list > tbody > tr > th { border: 1px solid #ddd; padding:5px; margin:0px; }");
		this.writer.println("table.recipe-list > tbody > tr:nth-child(even) { background-color: #F8F8F8; }");
		this.writer.println("table.recipe-list > tbody > tr:nth-child(odd) { background-color: white; }");
		this.writer.println("table.item-list td { text-align: center; min-width:50px;}");
		this.writer.println("table.item-list td+td { text-align: left; min-width:0px; padding-right:15px; }");
		this.writer.println("table.item-list td+td+td { padding-right: 0px; }");
		this.writer.println("td.prod-time, td.weight { text-align:center; }");
		this.writer.println("span.item-section { font-style: italic; }");
		this.writer.println("</style>");
		this.writer.println("</head>");
		this.writer.println("<body>");
		
		this.writer.println("<div class=main>");
		this.writer.println("<h1>Recipes for Factory plugin of CivCraft: Worlds</h1>");
		
		
		writeContent(root);
		
		for(Node child : root.children) {
			writeFactory(child);
		}
		
		for(String item : _badItems) {
			this.writer.println("<div><strong>" + item + "</strong></div>");
		}
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		this.writer.println("<p>Generated on " + format.format(new Date()) + "</p>");
		
		this.writer.println("</div>");
		
		this.writer.println("</body>");
		this.writer.println("</html>");
	}
	
	private void writeContent(Node root) {
		this.writer.println("<h2>Table of Contents</h2>");
		this.writer.println("<ul>");
		
		writeContentLink(root.children.get(0));
		
		for(Node node : root.children.get(0).children) {
			writeContentLink(node);
		}
		
		this.writer.println("</ul>");
	}
	
	private void writeContentLink(Node node) {
		String name = getSectionName(node.fcc);
		String anchor = getSectionAnchor(node.fcc);
		
		this.writer.println("<li><a href='#" + anchor + "'>" + name + "</a></li>");
	}
	
	private void writeFactory(Node node) {
		this.writer.println("<div>");
		
		String factoryName = node.fcc.getName();
		Node nextNode = null;
		List<FurnCraftChestEgg> recipes = new ArrayList<FurnCraftChestEgg>();
		
		recipes.add(node.fcc);
		
		nextNode = node;
		
		while(nextNode.children.size() == 1) {
			nextNode = nextNode.children.get(0);
			factoryName += " / " + nextNode.fcc.getName();
			recipes.add(nextNode.fcc);
		}
		
		if(node.parent.parent == null || node.parent.parent.parent == null) {
			this.writer.println("<a id=" + getSectionAnchor(node.fcc) + "></a>");
		}
		
		if(node.parent.parent == null) {
			this.writer.println("<h2>" + factoryName + "</h2>");
		}
		else {
			if(node.parent.parent.parent == null) {
				this.writer.println("<h2>" + getSectionName(node.fcc) + "</h2>");
			}
			
			this.writer.println("<h3>" + factoryName + "</h3>");
		}
		
		boolean hasWeightColumn = node.fcc.getName().toLowerCase().indexOf("pylon") >= 0;
		boolean showSetupCost = node.parent.parent == null;
		
		this.writer.println("<table class=recipe-list>");
		this.writer.println("<tr>");
		
		if(hasWeightColumn) {
			this.writer.println("<th>Output</th><th>Input</th><th>Production Time</th><th>Weight<th>Type</th><th>Notes</th>");
		} else {
			this.writer.println("<th>Output</th><th>Input</th><th>Production Time</th><th>Type</th><th>Notes</th>");
		}
		
		this.writer.println("</tr>");
		
		HashSet<IRecipe> shownRecipes = new HashSet<IRecipe>();
		
		for(FurnCraftChestEgg fcc : recipes) {
			writeRecipeList(fcc, hasWeightColumn, showSetupCost, shownRecipes, recipes.size() > 1);
		}
		
		this.writer.println("</table>");

		this.writer.println("</div>");
		
		for(Node child : nextNode.children) {
			writeFactory(child);
		}
	}
	
	private static String getSectionAnchor(FurnCraftChestEgg fcc) {
		return getSectionName(fcc).toLowerCase().replace(' ', '-');
	}
	
	private static String getSectionName(FurnCraftChestEgg fcc) {
		String name = fcc.getName();

		if(name.equalsIgnoreCase("Basic Forge")) return "Enchanting";
		if(name.equalsIgnoreCase("Basic Fortifications")) return "Fortifications";
		if(name.equalsIgnoreCase("Blacksmith")) return "Blacksmith";
		if(name.equalsIgnoreCase("Farmstead Factory")) return "Agriculture and XP Production";
		if(name.equalsIgnoreCase("Laboratory")) return "Aether and Advanced Techs";
		if(name.equalsIgnoreCase("Stone Smelter")) return "Smelter";
		if(name.equalsIgnoreCase("Wood Processor")) return "Wood Processor";
		
		return name;
	}
	
	private void writeRecipeList(
			FurnCraftChestEgg fcc,
			boolean hasWeightColumn,
			boolean showSetupCost,
			HashSet<IRecipe> shownRecipes,
			boolean showLevel
			)
	{
		if(showLevel) {
			int colspan = hasWeightColumn ? 6: 5;
			
			this.writer.println("<tr><th colspan=" + colspan + ">" + fcc.getName() + "</th></tr>");
		}
		
		ItemStack fuel = fcc.getFuel();
		int fuelConsumptionInterval = fcc.getFuelConsumptionIntervall();
		
		if(showSetupCost) {
			RecipeInfo setup = getSetupCost(fcc);
			
			if(setup != null) {
				writeRecipe(hasWeightColumn, setup, fuel);
			}
		}
		
		for(IRecipe recipe : fcc.getRecipes()) {
			if(shownRecipes.contains(recipe)) continue;

			RecipeInfo info = getRecipeInfo(recipe);
			
			if(info.fuelConsumptionInterval < 0) {
				info.fuelConsumptionInterval = fuelConsumptionInterval;
			}

			writeRecipe(hasWeightColumn, info, fuel);
			
			shownRecipes.add(recipe);
		}
	}
	
	private void writeRecipe(boolean hasWeightColumn, RecipeInfo info, ItemStack fuel) {
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
		
		if(info.fuelConsumptionInterval != 0) {
			this.writer.println("<span class=item-section>Fuel:</span>");
			writeFuelInfo(fuel, info);
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
	
	private RecipeInfo getSetupCost(IFactoryEgg egg) {
		FactoryModManager manager = FactoryMod.getManager();
		
		RecipeInfo info = new RecipeInfo();
		info.time = 0;
		info.name = "Setup Factory";
		info.type = "SETUP";
		info.fuelConsumptionInterval = 0;
		
		addItemMapInfo(info.input, manager.getTotalSetupCost(egg));
		
		return info;
	}
	
	private void writeFuelInfo(ItemStack fuel, RecipeInfo recipeInfo) {
		int amount = recipeInfo.time / recipeInfo.fuelConsumptionInterval;
		
		if(recipeInfo.time % recipeInfo.fuelConsumptionInterval != 0) {
			amount++;
		}
		
		List<ItemInfo> list = new ArrayList<ItemInfo>();
		list.add(getItemInfo(fuel, amount));
		
		writeItemList(list);
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
		this.writer.println(info.name);
		
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
	
	private RecipeInfo getRecipeInfo(IRecipe recipe) {
		RecipeInfo info = new RecipeInfo();
		info.time = recipe.getProductionTime();
		info.name = recipe.getRecipeName();
		
		InputRecipe inputRecipe = (InputRecipe)recipe;
		
		info.fuelConsumptionInterval = inputRecipe.getFuelConsumptionIntervall();
		
		addItemMapInfo(info.input, inputRecipe.getInput());
		
		getProductionRecipeInfo(recipe, info);
		getCompactingRecipeInfo(recipe, info);
		getDecompactingRecipeInfo(recipe, info);
		getRepairRecipeInfo(recipe, info);
		getUpgradeRecipeInfo(recipe, info);
		getAOERepairRecipeInfo(recipe, info);
		getPylonRecipeInfo(recipe, info);
		getDeterministicEnchantingRecipeInfo(recipe, info);
		getRandomOutputRecipeInfo(recipe, info);
		getFactoryMaterialReturnRecipeInfo(recipe, info);
		
		return info;
	}
	
	private boolean getProductionRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof ProductionRecipe)) return false;
		
		ProductionRecipe recipe = (ProductionRecipe)recipeInterface;
		
		info.type = "PRODUCTION";
		
		addItemMapInfo(info.output, recipe.getOutput());
		
		return true;
	}

	private boolean getCompactingRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof CompactingRecipe)) return false;
		
		info.type = "COMPACT";
		
		info.input.add(new ItemInfo("Stack of Items", 1));
		info.output.add(new ItemInfo("Compacted Item", 1));

		@SuppressWarnings("unchecked")
		List<Material> excludedMaterials = (List<Material>)getFieldValue(recipeInterface, "excludedMaterials");
		
		if(excludedMaterials != null) {
			for(Material mat : excludedMaterials) {
				info.inputExcluded.add(ItemNameHelper.lookup(mat));
			}
		}
		
		return true;
	}
	
	private boolean getDecompactingRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof DecompactingRecipe)) return false;
		
		info.type = "DECOMPACT";
		
		info.input.add(new ItemInfo("Compacted Item", 1));
		info.output.add(new ItemInfo("Stack of Items", 1));
		
		return true;
	}
	
	private boolean getRepairRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof RepairRecipe)) return false;
		
		info.type = "REPAIR";
		
		int healthPerRun = (int)getFieldValue(recipeInterface, "healthPerRun");
		
		info.output.add(new ItemInfo("Health", healthPerRun));
		
		return true;
	}
	
	private boolean getUpgradeRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof Upgraderecipe)) return false;
		
		info.type = "UPGRADE";
		
		return true;
	}
	
	private boolean getAOERepairRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof AOERepairRecipe)) return false;
		
		info.type = "AOEREPAIR";
		
		return true;
	}
	
	private boolean getPylonRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof PylonRecipe)) return false;
		
		PylonRecipe recipe = (PylonRecipe)recipeInterface;
		
		info.type = "PYLON";
		info.weight = recipe.getWeight();
		
		ItemStack output = recipe.getRecipeRepresentation();
		
		info.output.add(getItemInfo(output, output.getAmount()));
		
		return true;
	}
	
	private boolean getDeterministicEnchantingRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof DeterministicEnchantingRecipe)) return false;
		
		info.type = "ENCHANT";
		
		ItemMap tool = (ItemMap)getFieldValue(recipeInterface, "tool");
		ItemStack toolStack = tool.getItemStackRepresentation().get(0);
		
		info.input.add(getItemInfo(toolStack, toolStack.getAmount()));
		
		Enchantment enchant = (Enchantment)getFieldValue(recipeInterface, "enchant");
		int level = (int)getFieldValue(recipeInterface, "level");

		ItemMeta im = toolStack.getItemMeta();
		im.removeEnchant(enchant);
		im.addEnchant(enchant, level, true);
		toolStack.setItemMeta(im);
		
		info.output.add(getItemInfo(toolStack, toolStack.getAmount()));
		
		return true;
	}
	
	private boolean getRandomOutputRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof RandomOutputRecipe)) return false;
		
		RandomOutputRecipe recipe = (RandomOutputRecipe)recipeInterface;
		
		info.type = "RANDOM";

		Map<ItemMap, Double> outputs = recipe.getOutputs();
		
		for(Entry<ItemMap, Double> entry : outputs.entrySet()) {
			for(Entry<ItemStack, Integer> itemMapEntry : entry.getKey().getEntrySet()) {
				ItemInfo itemInfo = getItemInfo(itemMapEntry.getKey(), itemMapEntry.getValue());
				itemInfo.name += " (" + entry.getValue() + "%)";

				info.output.add(itemInfo);
			}
		}
		
		return true;
	}
	
	private boolean getFactoryMaterialReturnRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof FactoryMaterialReturnRecipe)) return false;
		
		info.type = "COSTRETURN";

		return true;
	}
	
	private void addItemMapInfo(List<ItemInfo> list, ItemMap itemMap) {
		Set<Entry<ItemStack, Integer>> items = itemMap.getEntrySet();
		
		for(Entry<ItemStack, Integer> entry : items) {
			list.add(getItemInfo(entry.getKey(), entry.getValue()));
		}
	}
	
	private ItemInfo getItemInfo(ItemStack stack, int amount) {
		String name = getLore(stack);
		boolean isCompacted = Objects.equal(name, "Compacted Item");
		
		if(isCompacted || name == null || name.length() == 0) {
			String key = stack.getType() + ":" + stack.getDurability();
			
			name = _nameReplacements.get(key);
			
			if(name == null || name.length() == 0) {
				if(stack.getDurability() < 0) {
					_badItems.add(stack.getType() + ":" + stack.getDurability());
				}
				
				name = ItemNameHelper.lookup(stack);
			}
			
			String enchantText = getEnchantText(stack);
			
			if(enchantText != null) {
				name += " (" + enchantText + ")";
			}
		}
		
		if(isCompacted) {
			name = "Compacted " + name;
		}
		
		return new ItemInfo(name, amount);
	}
	
	private static String getEnchantText(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		Map<Enchantment, Integer> enchants = meta.getEnchants();
		
		if(enchants.size() == 0) return null;
		
		StringBuilder text = new StringBuilder();
		
		for(Entry<Enchantment, Integer> entry : enchants.entrySet()) {
			if(text.length() > 0) {
				text.append(", ");
			}
			
			Enchantment ench = entry.getKey();
			int level = entry.getValue();
			
			text.append(getEnchantName(ench));
			
			if(!ench.equals(Enchantment.SILK_TOUCH)
					&& !ench.equals(Enchantment.ARROW_FIRE)
					&& !ench.equals(Enchantment.ARROW_INFINITE)
					&& !ench.equals(Enchantment.MENDING)
					&& !ench.equals(Enchantment.OXYGEN)
				)
			{
				String levelText;
				
				switch(level) {
				case 1:
					levelText = "I";
					break;
				case 2:
					levelText = "II";
					break;
				case 3:
					levelText = "III";
					break;
				case 4:
					levelText = "IV";
					break;
				case 5:
					levelText = "V";
					break;
				default:
					levelText = Integer.toString(level);
					break;
				}
				
				text.append(" ");
				text.append(levelText);
			}
		}
		
		return text.toString();
	}
	
	private static String getEnchantName(Enchantment enchant) {
	    if(enchant.equals(Enchantment.PROTECTION_ENVIRONMENTAL)) return "Protection";
	    if(enchant.equals(Enchantment.PROTECTION_FIRE)) return "Fire Protection";
	    if(enchant.equals(Enchantment.PROTECTION_FALL)) return "Feather Falling";
	    if(enchant.equals(Enchantment.PROTECTION_EXPLOSIONS)) return "Blast Protection";
	    if(enchant.equals(Enchantment.PROTECTION_PROJECTILE)) return "Projectile Protection";
	    if(enchant.equals(Enchantment.OXYGEN)) return "Respiration";
	    if(enchant.equals(Enchantment.WATER_WORKER)) return "Aqua Affinity";
	    if(enchant.equals(Enchantment.THORNS)) return "Thorns";
	    if(enchant.equals(Enchantment.DEPTH_STRIDER)) return "Depth Strider";
	    if(enchant.equals(Enchantment.FROST_WALKER)) return "Frost Walker";
	    if(enchant.equals(Enchantment.DAMAGE_ALL)) return "Sharpness";
	    if(enchant.equals(Enchantment.DAMAGE_UNDEAD)) return "Smite";
	    if(enchant.equals(Enchantment.DAMAGE_ARTHROPODS)) return "Bane of Arthropods";
	    if(enchant.equals(Enchantment.KNOCKBACK)) return "Knockback";
	    if(enchant.equals(Enchantment.FIRE_ASPECT)) return "Fire Aspect";
	    if(enchant.equals(Enchantment.LOOT_BONUS_MOBS)) return "Looting";
	    if(enchant.equals(Enchantment.DIG_SPEED)) return "Efficiency";
	    if(enchant.equals(Enchantment.SILK_TOUCH)) return "Silk Touch";
	    if(enchant.equals(Enchantment.DURABILITY)) return "Unbreaking";
	    if(enchant.equals(Enchantment.LOOT_BONUS_BLOCKS)) return "Fortune";
	    if(enchant.equals(Enchantment.ARROW_DAMAGE)) return "Power";
	    if(enchant.equals(Enchantment.ARROW_KNOCKBACK)) return "Punch";
	    if(enchant.equals(Enchantment.ARROW_FIRE)) return "Flame";
	    if(enchant.equals(Enchantment.ARROW_INFINITE)) return "Infinity";
	    if(enchant.equals(Enchantment.LUCK)) return "Luck of the Sea";
	    if(enchant.equals(Enchantment.LURE)) return "Lure";
	    if(enchant.equals(Enchantment.MENDING)) return "Mending";
	    
	    return "";
	}
	
	private static String getLore(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		
		if(!meta.hasLore()) return null;
		
		StringBuilder lore = new StringBuilder();
		
		for (String line: meta.getLore()) {
			if(lore.length() > 0) {
				lore.append("\n");
			}
			
			lore.append(line);
		}
		
		return lore.toString();
	}
	
	private static Object getFieldValue(Object obj, String fieldName) {
		Field field;
		
		try {
			field = obj.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException e1) {
			e1.printStackTrace();
			return null;
		} catch (SecurityException e1) {
			e1.printStackTrace();
			return null;
		}
		
		field.setAccessible(true);
		
		try {
			return field.get(obj);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}

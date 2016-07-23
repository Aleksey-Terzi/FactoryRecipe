package com.aleksey.factoryrecipe.creation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.aleksey.factoryrecipe.types.FactoryGroupInfo;
import com.aleksey.factoryrecipe.types.FactoryInfo;
import com.aleksey.factoryrecipe.types.ItemInfo;
import com.aleksey.factoryrecipe.types.ItemSummaryInfo;
import com.aleksey.factoryrecipe.types.Node;
import com.aleksey.factoryrecipe.types.RecipeInfo;
import com.aleksey.factoryrecipe.utils.ItemNameHelper;
import com.aleksey.factoryrecipe.utils.ItemStackHelper;
import com.aleksey.factoryrecipe.utils.LinkHelper;
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

public class FactoryListCreator {
	private static HashMap<String, String> _nameReplacements;

	private HashSet<String> badItems;
	private HashMap<String, String> upgradeAnchorMap;
	private List<FactoryGroupInfo> groups;
	private FactoryGroupInfo current;
	private HashMap<String, ItemSummaryInfo> itemSummaryMap;
	
	public HashSet<String> getBadItems() {
		return this.badItems; 
	}
	
	public List<FactoryGroupInfo> getGroups() {
		return this.groups; 
	}
	
	public HashMap<String, ItemSummaryInfo> getItemSummaryMap() {
		return this.itemSummaryMap;
	}

	public FactoryListCreator() {
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
	
	public void create() {
		this.badItems = new HashSet<String>();
		this.groups = new ArrayList<FactoryGroupInfo>();
		this.itemSummaryMap = new HashMap<String, ItemSummaryInfo>();
		
		Node root = createFactoryTree();
		
		for(Node child : root.children) {
			readFactory(child);
		}
	}
	
	private Node createFactoryTree() {
		FactoryModManager manager = FactoryMod.getManager();
		HashMap<String, IFactoryEgg> eggs = manager.getAllEggs();

		Node root = new Node(null, null);
		HashMap<FurnCraftChestEgg, Node> nodeMap = new HashMap<FurnCraftChestEgg, Node>();
		
		this.upgradeAnchorMap = new HashMap<String, String>();
		
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
	
	private List<FurnCraftChestEgg> getChildFactories(FurnCraftChestEgg parent) {
		ArrayList<FurnCraftChestEgg> result = new ArrayList<FurnCraftChestEgg>();
		
		for(IRecipe recipe : parent.getRecipes()) {
			if((recipe instanceof Upgraderecipe)) {
				IFactoryEgg egg = ((Upgraderecipe)recipe).getEgg();
				
				if(egg instanceof FurnCraftChestEgg) {
					result.add((FurnCraftChestEgg)egg);
					
					this.upgradeAnchorMap.put(egg.getName(), LinkHelper.getAnchor(recipe.getRecipeName()));
				}
			}
		}
		
		return result;
	}
	
	private void readFactory(Node node) {
		String factoryName = node.fcc.getName();
		Node nextNode = null;
		List<FurnCraftChestEgg> recipes = new ArrayList<FurnCraftChestEgg>();
		
		recipes.add(node.fcc);
		
		//Get level recipes
		nextNode = node;
		
		while(nextNode.children.size() == 1) {
			nextNode = nextNode.children.get(0);
			factoryName += " / " + nextNode.fcc.getName();
			recipes.add(nextNode.fcc);
		}
		
		//Get factory's attributes
		boolean hasLevels = recipes.size() > 1;
		boolean addSetupCost = node.parent.parent == null;
		String factoryAnchor = hasLevels ? null: LinkHelper.getAnchor(node.fcc.getName());
		String upgradeLink = "#" + this.upgradeAnchorMap.get(node.fcc.getName());

		if(node.parent.parent == null || node.parent.parent.parent == null) {
			String groupName;
			
			if(node.parent.parent == null) {
				groupName = factoryName;
				factoryName = null;
				factoryAnchor = null;
				upgradeLink = null;
			} else {
				groupName = getGroupName(node.fcc);
			}
			
			this.groups.add(this.current = new FactoryGroupInfo(groupName, LinkHelper.getAnchor(groupName)));
		}
		
		FactoryInfo factoryInfo = new FactoryInfo(node.fcc.getName(), factoryName, factoryAnchor, upgradeLink);
		
		this.current.factories.add(factoryInfo);
		
		// Get recipes
		HashSet<IRecipe> shownRecipes = new HashSet<IRecipe>();
		
		for(FurnCraftChestEgg fcc : recipes) {
			FactoryInfo levelInfo;
			
			if(hasLevels) {
				String levelUpgradeLink = "#" + this.upgradeAnchorMap.get(fcc.getName());
				String levelAnchor = LinkHelper.getAnchor(fcc.getName());
				
				factoryInfo.levels.add(levelInfo = new FactoryInfo(fcc.getName(), fcc.getName(), levelAnchor, levelUpgradeLink));
			} else {
				levelInfo = factoryInfo;
			}
			
			readRecipeList(fcc, addSetupCost, shownRecipes, levelInfo);
			
			Collections.sort(levelInfo.recipes);
		}
		
		Collections.sort(factoryInfo.recipes);
		
		// Read next factories
		for(Node child : nextNode.children) {
			readFactory(child);
		}
	}
	
	private static String getGroupName(FurnCraftChestEgg fcc) {
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
	
	private void readRecipeList(
			FurnCraftChestEgg fcc,
			boolean addSetupCost,
			HashSet<IRecipe> shownRecipes,
			FactoryInfo factoryInfo
			)
	{
		if(addSetupCost) {
			factoryInfo.recipes.add(getSetupCost(fcc));
		}
		
		for(IRecipe recipe : fcc.getRecipes()) {
			RecipeInfo recipeInfo = getRecipeInfo(fcc, recipe);
			
			addItemSummary(factoryInfo, recipeInfo);

			if(shownRecipes.contains(recipe)) continue;
			
			factoryInfo.recipes.add(recipeInfo);
			
			if(!recipeInfo.type.equalsIgnoreCase("REPAIR")) {
				shownRecipes.add(recipe);
			}
		}
	}
	
	private void addItemSummary(FactoryInfo factoryInfo, RecipeInfo recipeInfo) {
		if(recipeInfo.type.equalsIgnoreCase("ENCHANT")
				|| recipeInfo.type.equalsIgnoreCase("COMPACT")
				|| recipeInfo.type.equalsIgnoreCase("DECOMPACT")
			)
		{
			return;
		}
		
		for(ItemInfo itemInfo : recipeInfo.input) {
			if(itemInfo.rawName == null || itemInfo.rawName.equalsIgnoreCase("Essence")) {
				continue;
			}
			
			String anchor = LinkHelper.getAnchor(itemInfo.rawName);
			ItemSummaryInfo itemSummaryInfo = this.itemSummaryMap.get(anchor);
			
			if(itemSummaryInfo == null) {
				itemSummaryInfo = new ItemSummaryInfo(itemInfo.rawName, anchor);
				this.itemSummaryMap.put(anchor, itemSummaryInfo);
			}
			
			if(!itemSummaryInfo.inputFactories.contains(factoryInfo)) {
				itemSummaryInfo.inputFactories.add(factoryInfo);
			}
		}

		for(ItemInfo itemInfo : recipeInfo.output) {
			if(itemInfo.rawName == null) continue;
			
			String anchor = LinkHelper.getAnchor(itemInfo.rawName);
			ItemSummaryInfo itemSummaryInfo = this.itemSummaryMap.get(anchor);
			
			if(itemSummaryInfo == null) {
				itemSummaryInfo = new ItemSummaryInfo(itemInfo.rawName, anchor);
				this.itemSummaryMap.put(anchor, itemSummaryInfo);
			}
			
			if(recipeInfo.type.equalsIgnoreCase("RANDOM")) {
				if(!itemSummaryInfo.outputFactories.contains(factoryInfo)
					&& !itemSummaryInfo.randomFactories.contains(factoryInfo))
				{
					itemSummaryInfo.randomFactories.add(factoryInfo);
				}
			}
			else if(!itemSummaryInfo.outputFactories.contains(factoryInfo)) {
				itemSummaryInfo.outputFactories.add(factoryInfo);
				itemSummaryInfo.randomFactories.remove(factoryInfo);
			}
		}
}
	
	private RecipeInfo getSetupCost(IFactoryEgg egg) {
		FactoryModManager manager = FactoryMod.getManager();
		
		RecipeInfo info = new RecipeInfo();
		info.time = 0;
		info.name = "Setup Factory";
		info.type = "SETUP";
		
		addItemMapInfo(info.input, manager.getTotalSetupCost(egg));
		
		return info;
	}
	
	private RecipeInfo getRecipeInfo(FurnCraftChestEgg fcc, IRecipe recipe) {
		RecipeInfo info = new RecipeInfo();
		info.time = recipe.getProductionTime();
		info.name = recipe.getRecipeName();
		
		InputRecipe inputRecipe = (InputRecipe)recipe;
		
		int fuelConsumptionInterval = inputRecipe.getFuelConsumptionIntervall();
		
		if(fuelConsumptionInterval < 0) {
			fuelConsumptionInterval = fcc.getFuelConsumptionIntervall();
		}
		
		int fuelAmount = info.time / fuelConsumptionInterval;
		
		if(info.time % fuelConsumptionInterval != 0) {
			fuelAmount++;
		}
		
		info.fuel = getItemInfo(fcc.getFuel(), fuelAmount);
		
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
		
		info.input.add(new ItemInfo(null, "Stack of Items", 1));
		info.output.add(new ItemInfo(null, "Compacted Item", 1));

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
		
		info.input.add(new ItemInfo(null, "Compacted Item", 1));
		info.output.add(new ItemInfo(null, "Stack of Items", 1));
		
		return true;
	}
	
	private boolean getRepairRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof RepairRecipe)) return false;
		
		info.type = "REPAIR";
		
		int healthPerRun = (int)getFieldValue(recipeInterface, "healthPerRun");
		
		info.output.add(new ItemInfo(null, "Health", healthPerRun));
		
		return true;
	}
	
	private boolean getUpgradeRecipeInfo(IRecipe recipeInterface, RecipeInfo info) {
		if(!(recipeInterface instanceof Upgraderecipe)) return false;
		
		Upgraderecipe recipe = (Upgraderecipe)recipeInterface;
		
		info.type = "UPGRADE";
		info.link = LinkHelper.getAnchor(recipe.getEgg().getName());
		info.anchor = LinkHelper.getAnchor(recipe.getRecipeName());
		
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
		String rawName = ItemStackHelper.getLore(stack);
		String name;
		boolean isCompacted = Objects.equal(rawName, "Compacted Item");
		
		if(isCompacted || rawName == null || rawName.length() == 0) {
			String key = stack.getType() + ":" + stack.getDurability();
			
			rawName = _nameReplacements.get(key);
			
			if(rawName == null || rawName.length() == 0) {
				if(stack.getDurability() < 0) {
					this.badItems.add(stack.getType() + ":" + stack.getDurability());
				}
				
				rawName = ItemNameHelper.lookup(stack);
			}
			
			name = rawName;
			
			String enchantText = ItemStackHelper.getEnchantList(stack);
			
			if(enchantText != null) {
				name += " (" + enchantText + ")";
			}
		} else {
			name = rawName;
		}
		
		if(isCompacted) {
			name = "Compacted " + name;
		}
		
		if(rawName.equalsIgnoreCase("Aether\nCompacted Item")) {
			rawName = "Aether";
		}

		return new ItemInfo(rawName, name, amount);
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

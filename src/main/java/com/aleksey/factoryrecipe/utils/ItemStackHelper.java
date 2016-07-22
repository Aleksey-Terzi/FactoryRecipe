package com.aleksey.factoryrecipe.utils;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackHelper {
	public static String getEnchantList(ItemStack item) {
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
	
	public static String getLore(ItemStack item) {
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
}

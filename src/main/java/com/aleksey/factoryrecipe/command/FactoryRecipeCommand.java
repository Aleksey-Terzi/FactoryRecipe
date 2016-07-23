package com.aleksey.factoryrecipe.command;

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aleksey.factoryrecipe.FactoryRecipe;
import com.aleksey.factoryrecipe.creation.FactoryListCreator;
import com.aleksey.factoryrecipe.creation.IndexFileCreator;
import com.aleksey.factoryrecipe.creation.ItemSummaryFileCreator;

public class FactoryRecipeCommand {
	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender == null || ((sender instanceof Player) && ((Player)sender).hasPermission(""))) {
			return false;
		}
		
		FactoryListCreator factoryListCreator = new FactoryListCreator();
		factoryListCreator.create();
		
		boolean result = new IndexFileCreator().create(factoryListCreator)
				&& new ItemSummaryFileCreator().create(factoryListCreator);
		
		String message = result
			? "Files have been created."
			: "Failed to create files.";
        
    	if(sender instanceof Player) {
    		sender.sendMessage(message);
    	} else {
    		FactoryRecipe.getPluginLogger().log(Level.INFO, message);
    	}
    	
    	return true;
	}
}

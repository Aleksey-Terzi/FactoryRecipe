package com.aleksey.factoryrecipe.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aleksey.factoryrecipe.utils.FileCreator;

public class FactoryRecipeCommand {
	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender == null || ((sender instanceof Player) && ((Player)sender).hasPermission(""))) {
			return false;
		}
		
        return new FileCreator().create();
	}
}

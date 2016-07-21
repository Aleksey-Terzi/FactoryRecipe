package com.aleksey.factoryrecipe;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.aleksey.factoryrecipe.command.FactoryRecipeCommand;

public class FactoryRecipe extends JavaPlugin {
    private static FactoryRecipe instance;
    public static FactoryRecipe getInstance() {
    	return instance;
    }
    
    public static Logger getPluginLogger() {
    	return instance.getLogger();
    }
    
    @Override
    public void onEnable() {
    	instance = this;
    }
    
    @Override
    public void onDisable() {
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return FactoryRecipeCommand.onCommand(sender, command, label, args);
    }
}

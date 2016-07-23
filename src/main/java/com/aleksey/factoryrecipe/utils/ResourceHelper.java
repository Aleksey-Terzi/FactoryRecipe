package com.aleksey.factoryrecipe.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.aleksey.factoryrecipe.FactoryRecipe;

public class ResourceHelper {
	public static String readText(String resourcePath) {
		InputStream stream = FactoryRecipe.class.getResourceAsStream(resourcePath);
		
		if(stream == null) return null;
		
    	StringBuilder script = new StringBuilder("");
    	
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    		String line;
    		
            while ((line = reader.readLine()) != null) { 
            	script.append(line);
            	script.append("\n");
            }
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    	return script.toString();
	}
}

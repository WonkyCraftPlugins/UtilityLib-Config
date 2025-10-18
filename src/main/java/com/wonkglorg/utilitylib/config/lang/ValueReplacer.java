package com.wonkglorg.utilitylib.config.lang;

import java.util.HashMap;
import java.util.Map;

public record ValueReplacer(Map<String, String> replacements){
	
	public static ValueReplacer replace(String value, String replacement) {
		return new ValueReplacer(Map.of(value, replacement));
	}
	
	public static ValueReplacer replace(String value1, String replacement1, String value2, String replacement2) {
		return new ValueReplacer(Map.of(value1, replacement1, value2, replacement2));
	}
	
	public static ValueReplacer replace(String value1, String replacement1, String value2, String replacement2, String value3, String replacement3) {
		return new ValueReplacer(Map.of(value1, replacement1, value2, replacement2, value3, replacement3));
	}
	
	public static ValueReplacer replace(String... values) {
		Map<String, String> replacements = new HashMap<>();
		for(int i = 1; i < values.length; i += 2){
			replacements.put(values[i - 1], values[i]);
		}
		return new ValueReplacer(replacements);
	}
	
	public String apply(String value) {
		for(var entry : replacements.entrySet()){
			value = value.replace(entry.getKey(), entry.getValue());
		}
		return value;
	}
}

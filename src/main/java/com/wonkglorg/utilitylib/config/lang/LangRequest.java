package com.wonkglorg.utilitylib.config.lang;

import com.wonkglorg.utilitylib.config.LangManager;
import com.wonkglorg.utilitylib.config.types.LangConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Language Value request which can be further modified with additional properties and modifiers.
 */
@SuppressWarnings("unused")
public class LangRequest{
	private final Logger logger;
	/**
	 * The lang manager this request was returned by
	 */
	private final LangManager langManager;
	/**
	 * The locale used for the initial request
	 */
	private final Locale locale;
	/**
	 * The key used for the initial request
	 */
	private final String key;
	/**
	 * The defaultValue used for the initial request
	 */
	private final String defaultValue;
	/**
	 * Map of all replacements applied to this request
	 */
	private final Map<String, String> replacements = new HashMap<>();
	
	private final Map<String, Component> componentReplacements = new HashMap<>();
	/**
	 * The initial returned result without any other modifications
	 */
	private final String initialResult;
	/**
	 * The current result made after all values are collected
	 */
	private String result;
	/**
	 * Weather or not the initially provided locale should be forced. If false certain methods may re request the message in the given language such as {@link #sendToAudience(Audience)}
	 */
	private boolean forceLocale = false;
	
	public LangRequest(LangManager langManager, Locale locale, String key, String defaultValue) {
		logger = langManager.getLogger();
		this.langManager = langManager;
		this.locale = locale;
		this.key = key;
		this.defaultValue = defaultValue;
		this.initialResult = getValue(this.locale, key, defaultValue);
		this.result = initialResult;
	}
	
	public LangRequest forceLocale(boolean forceLocale) {
		this.forceLocale = forceLocale;
		return this;
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, String replacement) {
		if(replacement == null) return this;
		replacements.put(value, replacement);
		result = result.replace(value, replacement);
		return this;
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, char replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, short replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, int replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, long replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, double replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, float replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	public LangRequest replace(String value, Component replacement) {
		if(replacement == null) return this;
		componentReplacements.put(value, replacement);
		return this;
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value1, String replacement1, String value2, String replacement2) {
		replacements.put(value1, replacement1);
		replacements.put(value2, replacement2);
		result = result.replace(value1, replacement1).replace(value2, replacement2);
		return this;
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value1, String replacement1, String value2, String replacement2, String value3, String replacement3) {
		replacements.put(value1, replacement1);
		replacements.put(value2, replacement2);
		replacements.put(value3, replacement3);
		result = result.replace(value1, replacement1).replace(value2, replacement2).replace(value3, replacement3);
		return this;
	}
	
	public String getUnmodifiedResult() {
		return initialResult;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * Updates the input with all replacements found in the populated replacement map
	 *
	 * @param input to modify
	 * @return modified input
	 */
	private String updateResult(String input) {
		for(var replacement : replacements.entrySet()){
			input = input.replace(replacement.getKey(), replacement.getValue());
		}
		return input;
	}
	
	public String getResult() {
		return result;
	}
	
	private Component toComponent(Function<String, Component> toComponent, String result) {
		if(componentReplacements.isEmpty()){
			return toComponent.apply(result);
		}
		
		List<String> keys = componentReplacements.keySet().stream().sorted(Comparator.comparingInt(String::length).reversed()).toList();
		
		List<Component> components = new ArrayList<>();
		int index = 0;
		int length = result.length();
		
		while(index < length){
			int nearestPos = length;
			String nearestKey = null;
			
			for(String key : keys){
				int pos = result.indexOf(key, index);
				if(pos >= 0 && pos < nearestPos){
					nearestPos = pos;
					nearestKey = key;
				}
			}
			
			if(nearestKey == null){
				if(index < length){
					components.add(toComponent.apply(result.substring(index)));
				}
				break;
			}
			
			if(nearestPos > index){
				components.add(toComponent.apply(result.substring(index, nearestPos)));
			}
			
			Component replacement = componentReplacements.get(nearestKey);
			if(replacement != null){
				components.add(replacement);
			} else {
				components.add(toComponent.apply(nearestKey));
			}
			
			index = nearestPos + nearestKey.length();
		}
		
		return Component.join(JoinConfiguration.noSeparators(), components);
	}
	
	/**
	 *
	 * @param toComponent the converter to use
	 * @return the output as a component
	 */
	public Component toComponent(Function<String, Component> toComponent) {
		return toComponent(toComponent, result);
	}
	
	/**
	 * @return the output as a component
	 */
	public Component toComponent() {
		return toComponent(MiniMessage.miniMessage()::deserialize, result);
	}
	
	/**
	 * Sends the request's result to the given audience using the MiniMessage formatting.
	 *
	 * @param audience if the audience is a {@link Player} requests their locale to modify the message with unless {@link #forceLocale} is set to true.
	 */
	public void sendToAudience(@NotNull Audience audience) {
		sendToAudience(audience, MiniMessage.miniMessage()::deserialize);
	}
	
	/**
	 * Sends the request's result to the given audience using the MiniMessage formatting.
	 *
	 * @param audience if the audience is a {@link Player} requests their locale to modify the message with unless {@link #forceLocale} is set to true.
	 * @param toComponent the function to use turning the result into a component to send
	 */
	public void sendToAudience(@NotNull Audience audience, Function<String, Component> toComponent) {
		if(audience instanceof Player player){
			if(player.locale() == locale || forceLocale){
				audience.sendMessage(toComponent(toComponent, this.result));
			} else {
				String value = getValue(this.locale, key, defaultValue);
				value = updateResult(value);
				audience.sendMessage(toComponent(toComponent, this.result));
			}
		} else {
			audience.sendMessage(toComponent(toComponent, this.result));
		}
	}
	
	/**
	 * Gets a value from the language file with global replacements applied
	 *
	 * @param locale the locale to get the value from
	 * @param key the key to get by
	 * @param defaultValue the default value to return if no value was found
	 * @return the returned result or the value if no result was found
	 */
	@Contract(pure = true, value = "_,null,null -> null; _,_,!null -> !null")
	private String getValue(final Locale locale, final String key, String defaultValue) {
		LangConfig config;
		defaultValue = defaultValue != null ? defaultValue : key;
		var configOptional = langManager.getAnyValidLangConfig(locale);
		if(configOptional.isPresent()){
			config = configOptional.get();
		} else {
			logger.log(Level.INFO, "No lang file could be loaded for request: " + key + " using default value!");
			return defaultValue;
		}
		
		String editString = config.getString(key);
		if(editString == null){
			editString = defaultValue;
		}
		
		for(var mapValue : langManager.getReplacerMap().entrySet()){
			editString = editString.replace(mapValue.getKey(), mapValue.getValue());
		}
		
		if(config.isUpdateRequest()){
			config.updateReplacerMap();
		}
		
		for(var mapValue : config.getReplacerMap().entrySet()){
			editString = editString.replace(mapValue.getKey(), mapValue.getValue());
		}
		
		return editString;
	}
	
}

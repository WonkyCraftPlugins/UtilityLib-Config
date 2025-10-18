package com.wonkglorg.utilitylib.config.lang;

import com.wonkglorg.utilitylib.config.LangManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * A Language Value request which can be further modified with additional properties and modifiers.
 */
public class LangRequest{
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
	/**
	 * The initial returned result without any other modifications
	 */
	private final String initialResult;
	/**
	 * The current result made after all values are collected
	 */
	private String result;
	
	public LangRequest(LangManager langManager, Locale locale, String key, String defaultValue) {
		this.langManager = langManager;
		this.locale = locale;
		this.key = key;
		this.defaultValue = defaultValue;
		this.initialResult = langManager.getValue(this.locale, key, defaultValue);
		this.result = initialResult;
	}
	
	public LangRequest replace(String value, String replacement) {
		replacements.put(value, replacement);
		result = result.replace(value, replacement);
		return this;
	}
	
	public LangRequest replace(String value1, String replacement1, String value2, String replacement2) {
		replacements.put(value1, replacement1);
		replacements.put(value2, replacement2);
		result = result.replace(value1, replacement1).replace(value2, replacement2);
		return this;
	}
	
	public LangRequest replace(String value1, String replacement1, String value2, String replacement2, String value3, String replacement3) {
		replacements.put(value1, replacement1);
		replacements.put(value2, replacement2);
		replacements.put(value3, replacement3);
		result = result.replace(value1, replacement1).replace(value2, replacement2).replace(value3, replacement3);
		return this;
	}
	
	public String getInitialResult() {
		return initialResult;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	private String updateResult(String result) {
		for(var replacement : replacements.entrySet()){
			result = result.replace(replacement.getKey(), replacement.getValue());
		}
		return result;
	}
	
	public String getResult() {
		return result;
	}
	
	public void sendToAudience(@NotNull Audience audience) {
		sendToAudience(audience, MiniMessage.miniMessage()::deserialize);
	}
	
	public void sendToAudience(@NotNull Audience audience, Function<String, Component> toComponent) {
		if(audience instanceof Player player){
			if(player.locale() == locale){
				audience.sendMessage(toComponent.apply(this.result));
			} else {
				String value = langManager.getValue(this.locale, key, defaultValue);
				value = updateResult(value);
				player.sendMessage(toComponent.apply(value));
			}
		} else {
			audience.sendMessage(toComponent.apply(this.result));
		}
		
	}
}

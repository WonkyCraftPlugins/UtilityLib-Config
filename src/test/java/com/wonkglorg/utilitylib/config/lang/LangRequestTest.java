package com.wonkglorg.utilitylib.config.lang;

import com.wonkglorg.utilitylib.config.LangManager;
import com.wonkglorg.utilitylib.config.types.LangConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

class LangRequestTest{
	
	@Test
	void canResolveLang() {
		JavaPlugin plugin = Mockito.mock(JavaPlugin.class);
		Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
		LangManager langManager = LangManager.createInstance(plugin);
		LangConfig langConfig = Mockito.mock(LangConfig.class);
		Mockito.when(langConfig.getString(any(), any())).thenReturn("<red>This is a cool text %placeholder% %placeholder2%");
		langManager.setDefaultLang(Locale.US);
		langManager.addLanguage(langConfig, Locale.US);
		langManager.addLanguage(langConfig, Locale.ENGLISH);
		
		for(int i = 0; i < 9000; i++){
			LangRequest langRequest = new LangRequest(langManager, Locale.US, "test", "test");
			
			List<Component> components = langRequest.replace("%placeholder%", "Replacement").replace("%placeholder2%",
					Component.text("EEEEEEEEEEEEEEE")).toComponent();
		}
	}
	
}

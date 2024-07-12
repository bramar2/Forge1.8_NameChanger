package bramar.namechanger.config;

import java.util.List;

import com.google.common.collect.Lists;

import bramar.namechanger.ConfigHandler;
import bramar.namechanger.NameChanger;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.client.config.IConfigElement;

public class NCGuiConfig extends GuiConfig {
	
	public List<HoverChecker> hoverCheckers = Lists.<HoverChecker>newArrayList();
	public List<String> tooltips = Lists.<String>newArrayList();
	public NCGuiConfig(GuiScreen parent) {
		super(parent,
				new ConfigElement(ConfigHandler.config.getCategory("general"))
				.getChildElements(),
				NameChanger.MODID,
				false,
				false,
				"NameChanger: Change your nickname to whatever you desire!");
		titleLine2 = "File Path: \"{minecraft_directory}/" + ConfigHandler.getFile() + "\"";
		tooltips.add("Changes your nick name");
		tooltips.add("Changes your nickname prefix");
		tooltips.add("Changes your nickname suffix");
		tooltips.add("Toggles whether the mod is enabled or not");
		tooltips.add("Toggles whether your rank is removed from chat or not");
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if(buttonList == null || hoverCheckers == null || tooltips == null) return;
		if(hoverCheckers.size() == 0) {
			for(GuiButton btn : buttonList) {
				hoverCheckers.add(new HoverChecker(btn, btn.getButtonWidth()));
			}
			return;
		}
		for(int i = 0; i < buttonList.size(); i++) {
			GuiButton button = buttonList.get(i);
			HoverChecker hover = hoverCheckers.get(i);
			String tooltip = tooltips.get(i);
			if(hover.checkHover(mouseX, mouseY)) {
				drawToolTip(fontRendererObj.listFormattedStringToWidth(tooltip, 400), mouseX, mouseY);
			}
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if(button.displayString.equalsIgnoreCase("Done")) {
			// Acts as a save changes button
			try {
				String P_NAME = null;
				String P_PREFIX = null;
				String P_SUFFIX = null;
				String enabled = null;
				String haveRank = null;
				String tabList = null;
				for(IConfigElement element : this.configElements) {
					String key = element.getName();
					Object value = element.get();
					if(value instanceof java.lang.String) {
						if(key.equalsIgnoreCase("PlayerName")) {
							P_NAME = (String) value;
						}else if(key.equalsIgnoreCase("PlayerPrefix")) {
							P_PREFIX = (String) value;
						}else if(key.equalsIgnoreCase("PlayerSuffix")) {
							P_SUFFIX = (String) value;
						}else if(key.equalsIgnoreCase("Enabled")) {
							enabled = value.toString();
						}else if(key.equalsIgnoreCase("HaveRank")) {
							haveRank = value.toString();
						}else if(key.equalsIgnoreCase("TabList")) {
							tabList = value.toString();
						}
					}
				}
				if(P_NAME != null) {
					NameChanger.P_NAME = P_NAME;
					NameChanger.instance.setConfiguration("PlayerName", P_NAME);
				}
				if(P_PREFIX != null) {
					NameChanger.P_PREFIX = P_PREFIX;
					NameChanger.instance.setConfiguration("PlayerPrefix", P_PREFIX);
				}
				if(P_SUFFIX != null) {
					NameChanger.P_SUFFIX = P_SUFFIX;
					NameChanger.instance.setConfiguration("PlayerSuffix", P_SUFFIX);
				}
				if(enabled != null) {
					if(enabled.equalsIgnoreCase("true") || enabled.equalsIgnoreCase("yes")) {
						NameChanger.instance.setConfiguration("Enabled", "true");
						NameChanger.instance.setEnabled(true);
					}else {
						NameChanger.instance.setConfiguration("Enabled", "false");
						NameChanger.instance.setEnabled(false);
					}
					
				}
				if(haveRank != null) {
					if(haveRank.equalsIgnoreCase("true") || haveRank.equalsIgnoreCase("yes")) {
						NameChanger.instance.setConfiguration("HaveRank", "true");
						NameChanger.haveRank = true;
					}else {
						NameChanger.instance.setConfiguration("HaveRank", "false");
						NameChanger.haveRank = false;
					}
				}
				if(tabList != null) {
					if(tabList.equalsIgnoreCase("true") || tabList.equalsIgnoreCase("yes")) {
						NameChanger.instance.setConfiguration("TabList", "true");
						NameChanger.tabList = true;
					}else {
						NameChanger.instance.setConfiguration("TabList", "false");
						NameChanger.tabList = false;
					}
				}
				NameChanger.instance.refresh();
				System.out.println("Successfully saved changes!");
			}catch(Exception e1) {
				e1.printStackTrace();
				System.out.println("Failed to save changes to configuration!");
			}
		}
	}
}

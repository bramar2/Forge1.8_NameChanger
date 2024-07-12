package bramar.namechanger;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import bramar.namechanger.command.ChangeNameCommand;
import bramar.namechanger.command.ChangePrefixCommand;
import bramar.namechanger.command.ChangeSuffixCommand;
import bramar.namechanger.command.RefreshNameCommand;
import bramar.namechanger.command.ResetNameCommand;
import bramar.namechanger.command.TestCommand;
import bramar.namechanger.config.NCGuiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;


/**
 * Main class
 * @info
 * NameChanger Mod. RELEASE 1.4<br>
 * Changes your name to a nickname you desire!<br>
 * Only works for Minecraft Forge 1.8 (Recommended) - 1.12<br>
 * Some features may break in 1.9+<br>
 * This mod has been ONLY tested on 1.8<br>
 * 
 * @changelog
 * Changelog 1.4<br>
 * Added /refreshname that refreshes the name and loads the config if there is a new one.<br>
 * Added EntityPlayer#refreshDisplayName on PlayerLoggedInEvent, EntityJoinWorldEvent, ClientConnectedToServerEvent<br>
 * Added in-game configuration by going into mod list and clicking 'Config'. This is way better than manually going to the config file.<br>
 * The 'Done' button in configuration now acts as a Save changes button<br><br>
 * 
 * Changelog 1.5<br>
 * You can now enter double quotes by putting \" for the input of the quotes. It's like an escape, but worse.<br>
 * <br>
 * Changelog 1.7<br>
 * Fixed single-player world sometimes crashes the client because of an unexpected null NameChanger instance<br>
 * Modified the UUID check by converting it to string and then checking it making it 100% accurate<br>
 * <br>
 * Changelog 2.0<br>
 * Added two configurations: Enabled and HaveRank<br>
 * Enabled: Whether or not the mod is enabled<br>
 * HaveRank: Does it remove the previous word if it contains [ and ]<br>
 * Tablist is now updated with the nickname! [Only if the tablist contains your username]<br> 
 * 
 * @version 1.0
 * @author bramar
 */
@Mod(modid = NameChanger.MODID,
	version = NameChanger.VERSION,
	name = NameChanger.NAME,
	guiFactory = "bramar.namechanger.config.NCGuiFactory")
public class NameChanger {
	// Mod Info incase the mcmod.info didnt register
	
    public static final String MODID = "namechanger";
    public static final String VERSION = "2.0";
    public static final String NAME = "NameChanger";
    public static final String COLOR_CHAR = "§";
    // Player Nick Info
    public static String P_PREFIX = "";
    public static String P_NAME = "";
    public static String P_SUFFIX = "";
    /**
     * Gets the current instance of the mod
     */
    @Instance(value = "NameChanger")
    public static NameChanger instance;
    // Config category
    public final String categoryName = "general";
    // Gives logs for debugging
    protected final boolean debugMode = false;
    // Whether /nctest is enabled or not
    protected final boolean testCommand = false;
    // Color code symbols for checking because development environment and minecraft
    // color char is different
    private final String[] colorCodeSymbols = {"§", "\u00A7"};
    
    /**
     * Refresh's the configuration. Used in /refreshname and NCGuiConfig
     */
    public void refresh() {
    	ConfigHandler.init();
		if(Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.refreshDisplayName();
		String categoryName = NameChanger.instance.categoryName;
		if(!ConfigHandler.hasKey(categoryName, "PlayerName")) ConfigHandler.writeConfig(categoryName, "PlayerName", "");
    	else P_NAME = ConfigHandler.getString(categoryName, "PlayerName");
    	if(!ConfigHandler.hasKey(categoryName, "PlayerPrefix")) ConfigHandler.writeConfig(categoryName, "PlayerPrefix", "");
    	else P_PREFIX = ConfigHandler.getString(categoryName, "PlayerPrefix");
    	if(!ConfigHandler.hasKey(categoryName, "PlayerSuffix")) ConfigHandler.writeConfig(categoryName, "PlayerSuffix", "");
    	else P_SUFFIX = ConfigHandler.getString(categoryName, "PlayerSuffix");
    	if(!ConfigHandler.hasKey(categoryName, "OtherInfo")) ConfigHandler.writeConfig(categoryName, "OtherInfo", "Use '&y' to use the last 1/2/3 color codes.");
    	if(!ConfigHandler.hasKey(categoryName, "Enabled")) {
    		String enabled = ConfigHandler.getString(categoryName, "Enabled");
    		if(enabled.equalsIgnoreCase("true") || enabled.equalsIgnoreCase("yes")) {
				setConfiguration("Enabled", "true");
				setEnabled(true);
			}else {
				setConfiguration("Enabled", "false");
				setEnabled(false);
			}
    	}
    	if(!ConfigHandler.hasKey(categoryName, "HaveRank")) {
    		String enabled = ConfigHandler.getString(categoryName, "HaveRank");
    		if(enabled.equalsIgnoreCase("true") || enabled.equalsIgnoreCase("yes")) {
				setConfiguration("HaveRank", "true");
				setEnabled(true);
			}else {
				setConfiguration("HaveRank", "false");
				setEnabled(false);
			}
    	}
    	if(Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.refreshDisplayName();
    }
    
    /**
     * Gets the IChatComponent object from the String with COLOR_CHAR as Colors
     * (Using ChatStyle)
     * @param s The string that you want to convert to IChatComponent (COLOR_CHAR for color)
     * @return The chat as IChatComponent with the correct colors
     */
    public IChatComponent getChatWithColorCode(String s) {
    	if(debugMode) System.out.println("DEBUG: getChatWithColorCode('" + s + "')");
    	List<IChatComponent> siblings = new ArrayList<IChatComponent>();
    	ChatColor last = null;
    	String[] betweenChar = s.split(COLOR_CHAR);
    	boolean first = true;
    	for(int a = 0; a < betweenChar.length; a++) {
    		String str = betweenChar[a];
    		if(first) {
    			IChatComponent comp = new ChatComponentText(str);
    			if(debugMode) System.out.println("DEBUG: Sibling.add(Text:'" + comp.getUnformattedText() + "',Color:'" + comp.getChatStyle().getColor() + "',BOLD:" + comp.getChatStyle().getBold() + ",ITALIC:" + comp.getChatStyle().getItalic() + ",STRIKETHROUGH:" + comp.getChatStyle().getStrikethrough() + ",MAGIC:" + comp.getChatStyle().getObfuscated());
    			siblings.add(comp);
    			first = false;
    			continue;
    		}
    		if(debugMode) System.out.println("DEBUG: betweenChar{'" + str + "'}");
    		if(debugMode) System.out.println("DEBUG: SiblingSize=" + siblings.size());
    		if(debugMode) System.out.println("DEBUG: LastChatColor=" + (last == null ? "NULL" : last.getName()));
    		if(str.length() == 0) {
    			if(debugMode) System.out.println("DEBUG: str.length() == 0");
    			if(last != null) System.out.println("DEBUG: LastChatColor != null is TRUE");
    			else System.out.println("DEBUG: LastChatColor is NULL");
    			IChatComponent comp = new ChatComponentText("&");
    			if(last != null) comp.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.valueOf(last.getName())));
    			if(debugMode) System.out.println("DEBUG: Sibling.add(Text:'" + comp.getUnformattedText() + "',Color:'" + comp.getChatStyle().getColor() + "',BOLD:" + comp.getChatStyle().getBold() + ",ITALIC:" + comp.getChatStyle().getItalic() + ",STRIKETHROUGH:" + comp.getChatStyle().getStrikethrough() + ",MAGIC:" + comp.getChatStyle().getObfuscated());
    			siblings.add(comp);
    		}else if(str.length() == 1) {
    			if(debugMode) System.out.println("DEBUG: str.length() == 1");
    			IChatComponent comp = null;
    			if(ChatColor.isColorChar(str.charAt(0))) {
    				if(a == betweenChar.length - 1 && str.equalsIgnoreCase(Character.toString(Minecraft.getMinecraft().thePlayer.getName().charAt(0)))) {
        				if(debugMode) System.out.println("DEBUG: This is the last string && String is == to '&" + Minecraft.getMinecraft().thePlayer.getName().charAt(0) + "'. Aborting! This is to prevent a bug.");
        				continue;
        			}
    				last = new ChatColor(str.charAt(0));
    				if(debugMode) System.out.println("DEBUG: Color Char='" + str.charAt(0) + "'");
    			}
    			else {
    				if(a == betweenChar.length - 1 && str.equalsIgnoreCase(Character.toString(Minecraft.getMinecraft().thePlayer.getName().charAt(0)))) {
        				if(debugMode) System.out.println("DEBUG: This is the last string && String is == to '&" + Minecraft.getMinecraft().thePlayer.getName().charAt(0) + "'. Aborting! This is to prevent a bug.");
        				continue;
        			}
    				if(debugMode) System.out.println("DEBUG: Not a color char.");
    				comp = new ChatComponentText("&" + str);
    				if(last != null) comp.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.valueOf(last.getName())));
    				if(debugMode) System.out.println("DEBUG: Sibling.add(Text:'" + comp.getUnformattedText() + "',Color:'" + comp.getChatStyle().getColor() + "',BOLD:" + comp.getChatStyle().getBold() + ",ITALIC:" + comp.getChatStyle().getItalic() + ",STRIKETHROUGH:" + comp.getChatStyle().getStrikethrough() + ",MAGIC:" + comp.getChatStyle().getObfuscated());
    				siblings.add(comp);
    			}
    		}else {
    			if(debugMode) System.out.println("DEBUG: str.length() == " + str.length());
    			if(!ChatColor.isColorChar(str.charAt(0))) {
    				if(debugMode) System.out.println("DEBUG: ChatColor.isColorChar(str.charAt(0) is FALSE");
    				IChatComponent comp = new ChatComponentText("&" + str);
    				if(last != null) comp.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.valueOf(last.getName())));
    				if(debugMode) System.out.println("DEBUG: Sibling.add(Text:'" + comp.getUnformattedText() + "',Color:'" + comp.getChatStyle().getColor() + "',BOLD:" + comp.getChatStyle().getBold() + ",ITALIC:" + comp.getChatStyle().getItalic() + ",STRIKETHROUGH:" + comp.getChatStyle().getStrikethrough() + ",MAGIC:" + comp.getChatStyle().getObfuscated());
    				siblings.add(comp);
    				continue;
    			}
    			EnumChatFormatting lastInEnum = EnumChatFormatting.valueOf(new ChatColor(str.charAt(0)).getName());
    			if(debugMode) System.out.println("DEBUG: lastInEnum=" + lastInEnum.toString());
    			boolean isSpecial = false;
    			switch(lastInEnum) {
    			case BOLD:
    				isSpecial = true;
    			case OBFUSCATED:
    				isSpecial = true;
    			case RESET:
    				isSpecial = true;
    			case UNDERLINE:
    				isSpecial = true;
    			case STRIKETHROUGH:
    				isSpecial = true;
				default:
					break;
    			}
    			IChatComponent comp = null;
    			if(isSpecial) {
    				if(debugMode) System.out.println("DEBUG: isSpecial == TRUE");
    				comp = new ChatComponentText(str.substring(1));
    				ChatStyle style = new ChatStyle();
    				boolean reset = false;
    				if(lastInEnum == EnumChatFormatting.BOLD) {
    					style.setBold(true);
    				}else if(lastInEnum == EnumChatFormatting.ITALIC) {
    					style.setItalic(true);
    				}else if(lastInEnum == EnumChatFormatting.STRIKETHROUGH) {
    					style.setStrikethrough(true);
    				}else if(lastInEnum == EnumChatFormatting.OBFUSCATED) {
    					style.setObfuscated(true);
    				}else if(lastInEnum == EnumChatFormatting.UNDERLINE) {
    					style.setUnderlined(true);
    				}else {
    					// Reset
    					reset = true;
    				}
    				if(last != null) style.setColor(EnumChatFormatting.valueOf(last.getName()));
    				if(reset) style.setColor(EnumChatFormatting.RESET);
    				comp.setChatStyle(style);
    			}else {
    				if(debugMode) System.out.println("DEBUG: isSpecial == FALSE");
    				comp = new ChatComponentText(str.substring(1));
    				last = new ChatColor(str.charAt(0));
    				comp.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.valueOf(last.getName())));
    			}
    			if(debugMode) System.out.println("DEBUG: Sibling.add(Text:'" + comp.getUnformattedText() + "',Color:'" + comp.getChatStyle().getColor() + "',BOLD:" + comp.getChatStyle().getBold() + ",ITALIC:" + comp.getChatStyle().getItalic() + ",STRIKETHROUGH:" + comp.getChatStyle().getStrikethrough() + ",MAGIC:" + comp.getChatStyle().getObfuscated());
    			siblings.add(comp);
    		}
    	}
    	if(debugMode) System.out.println("DEBUG: List has been put together!");
    	IChatComponent full = new ChatComponentText("");
    	for(int i = 0; i < siblings.size(); i++) {
    		IChatComponent sibling = siblings.get(i);
    		if(i == siblings.size() - 1 && sibling.getUnformattedText().equalsIgnoreCase("&" + Character.toString(Minecraft.getMinecraft().thePlayer.getName().charAt(0)))) continue;
    		if(sibling.getUnformattedText().contains("&" + Character.toString(Minecraft.getMinecraft().thePlayer.getName().charAt(0)))) {
    			ChatStyle style = sibling.getChatStyle();
    			sibling = new ChatComponentText(sibling.getUnformattedText().replace("&" + Character.toString(Minecraft.getMinecraft().thePlayer.getName().charAt(0)), ""));
    			sibling.setChatStyle(style);
    		}
    		if(sibling.getUnformattedText().contains("&y")) {
    			ChatStyle style = sibling.getChatStyle();
    			sibling = new ChatComponentText(sibling.getUnformattedText().replace("&y", ""));
    			sibling.setChatStyle(style);
    		}
    		full.appendSibling(sibling);
    	}
    	return full;
    }
    /**
     * Remove color codes from the string
     * @param s The string WITH the color codes.
     * @return The string without the color codes.
     */
    public String removeColorCode(String s) {
    	String clone = new String(s);
    	for(char c : "0123456789abcdefklmnor".toCharArray()) {
    		for(String symbol : colorCodeSymbols) {
    			s = s.replace(symbol + c, "");
    		}
    	}
    	String unicode = "";
    	for(char c : clone.toCharArray()) {
    		if(c == '\\') unicode += "\\";
    		else unicode += c;
    	}
    	if(debugMode) System.out.println("DEBUG: With unicode: '" + unicode + "'");
    	if(debugMode) System.out.println("DEBUG: RemoveColorCode, BEFORE: '" + clone + "', AFTER: '" + s + "'");
    	return s;
    }
    /**
     * Get the replaced string
     * @return The string that is supposed to be the replace the player name.
     */
    public String getReplaced() {
    	if(debugMode) System.out.println("DEBUG: uncount(P_NAME, ' ') == " + uncount(P_NAME, ' ') + ", FULL: '" + P_PREFIX + P_NAME + P_SUFFIX + "'");
    	if(P_NAME.equalsIgnoreCase("")) P_NAME = Minecraft.getMinecraft().thePlayer.getName();
    	if(P_NAME.contains(" ")) if(uncount(P_NAME, ' ') == 0) P_NAME = Minecraft.getMinecraft().thePlayer.getName();
    	
    	return (P_PREFIX + P_NAME + P_SUFFIX).replace("&", COLOR_CHAR);
    }
    /**
     * Count occurrences of a char that is not the char inputed in string.
     * @param s The string you want to count from
     * @param c The char that is uncounted
     * @return The occurrences of every char that is not 'c' variable in the string
     */
    public static int uncount(String s, char c) {
    	int count = 0;
    	for(char ch : s.toCharArray()) {
    		if(ch != c) count++;
    	}
    	return count;
    }

    /**
     * Count occurrences of a char in string. Used in methods like this, because
     * unavailable in below source Java 8
     * @param s The string you want to count from
     * @param c The char that is counted
     * @return The occurrences of 'c' variable char in the string
     */
    public static int count(String s, char c) {
    	int count = 0;
    	for(char ch : s.toCharArray()) {
    		if(ch == c) count++;
    	}
    	return count;
    }
    
    private static boolean enabled = true;
    public static boolean haveRank = false;
    public static boolean tabList = true;
    
    public boolean isEnabled() {
		return enabled;
	}
    public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
    
    @SubscribeEvent
    public void onClientTick(ClientTickEvent e) {
    	if(e.phase == Phase.END && enabled && tabList) {
    		try {
    			String replaced = getReplaced();
    			if(replaced.equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.getName())) return;
    			Field field = null;
				try {
					field = NetHandlerPlayClient.class.getDeclaredField("field_147310_i");
				}catch(Exception e1) {
					try {
						field = NetHandlerPlayClient.class.getDeclaredField("playerInfoMap");
					}catch(Exception ignored) {}
				}
				field.setAccessible(true);
				NetHandlerPlayClient net = Minecraft.getMinecraft().getNetHandler();
				Map<UUID, NetworkPlayerInfo> map = (Map<UUID, NetworkPlayerInfo>) field.get(net);
				if(map.size() == 0) return;
				Map<UUID, NetworkPlayerInfo> modifications = Maps. <UUID, NetworkPlayerInfo> newHashMap();
				for(Map.Entry<UUID, NetworkPlayerInfo> entry : map.entrySet()) {
					NetworkPlayerInfo info = entry.getValue();
					if(info.getGameProfile().getName().equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.getName())) {
						info.setDisplayName(getChatWithColorCode(getReplaced()));
						modifications.put(entry.getKey(), info);
						break;
					}
				}
				for(Map.Entry<UUID, NetworkPlayerInfo> entry : modifications.entrySet()) {
					map.remove(entry.getKey());
					map.put(entry.getKey(), entry.getValue());
				}
				field.set(net, map);
    		}catch(Exception ignored) {}
    	}
    }
    
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    	try {
    		instance = this;
        	ClientCommandHandler.instance.registerCommand(new ChangeNameCommand());
        	ClientCommandHandler.instance.registerCommand(new ChangePrefixCommand());
        	ClientCommandHandler.instance.registerCommand(new ChangeSuffixCommand());
        	ClientCommandHandler.instance.registerCommand(new ResetNameCommand());
        	ClientCommandHandler.instance.registerCommand(new RefreshNameCommand());
        	if(testCommand) ClientCommandHandler.instance.registerCommand(new TestCommand());
        	MinecraftForge.EVENT_BUS.register(new NameChanger());
        	ConfigHandler.setFile("namechanger.cfg");
        	ConfigHandler.init();
        	if(!ConfigHandler.hasKey(categoryName, "PlayerName")) ConfigHandler.writeConfig(categoryName, "PlayerName", "");
        	else P_NAME = ConfigHandler.getString(categoryName, "PlayerName");
        	if(!ConfigHandler.hasKey(categoryName, "PlayerPrefix")) ConfigHandler.writeConfig(categoryName, "PlayerPrefix", "");
        	else P_PREFIX = ConfigHandler.getString(categoryName, "PlayerPrefix");
        	if(!ConfigHandler.hasKey(categoryName, "PlayerSuffix")) ConfigHandler.writeConfig(categoryName, "PlayerSuffix", "");
        	else P_SUFFIX = ConfigHandler.getString(categoryName, "PlayerSuffix");
        	if(!ConfigHandler.hasKey(categoryName, "OtherInfo")) ConfigHandler.writeConfig(categoryName, "OtherInfo", "Use '&y' to use the last 1/2/3 color codes.");
        	if(!ConfigHandler.hasKey(categoryName, "Enabled")) ConfigHandler.writeConfig(categoryName, "Enabled", "true");
        	else {
        		String enabled = ConfigHandler.getString(categoryName, "Enabled");
        		if(enabled.equalsIgnoreCase("true") || enabled.equalsIgnoreCase("yes")) {
    				setConfiguration("Enabled", "true");
    				setEnabled(true);
    			}else {
    				setConfiguration("Enabled", "false");
    				setEnabled(false);
    			}
        	}
        	if(!ConfigHandler.hasKey(categoryName, "HaveRank")) ConfigHandler.writeConfig(categoryName, "HaveRank", "false");
        	else {
        		String enabled = ConfigHandler.getString(categoryName, "HaveRank");
        		if(enabled.equalsIgnoreCase("true") || enabled.equalsIgnoreCase("yes")) {
    				setConfiguration("HaveRank", "true");
    				haveRank = true;
    			}else {
    				setConfiguration("HaveRank", "false");
    				haveRank = false;
    			}
        	}
        	if(!ConfigHandler.hasKey(categoryName, "TabList")) ConfigHandler.writeConfig(categoryName, "TabList", "true");
        	else {
        		String enabled = ConfigHandler.getString(categoryName, "TabList");
        		if(enabled.equalsIgnoreCase("true") || enabled.equalsIgnoreCase("yes")) {
    				setConfiguration("TabList", "true");
    				tabList = true;
    			}else {
    				setConfiguration("TabList", "false");
    				tabList = false;
    			}
        	}
    	}catch(Exception e1) {
    		FMLLog.severe("NameChanger > An error has occured. This mod will NOT WORK properly! Please try to restart the game. Error:");
    		System.err.println(e1);
    		return;
    	}finally {
    		System.out.println("NameChanger successfully initialized!");
    	}
    }
    
    @SubscribeEvent
    public void nameAboveHead(PlayerEvent.NameFormat e) {
    	try {
    		if(e.username.equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.getName())) e.displayname = getReplaced();
    	}catch(Exception ignored) {}
    }
    
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent e) {
    	if(!enabled) return;
    	String formatted = replace(e.message.getFormattedText());
		IChatComponent c = getChatWithColorCode(formatted);
		if(e.message.getFormattedText().equalsIgnoreCase(formatted)) return;
		e.message = c;
    }
    /**
     * Replaces the player name with the nick in a string
     * @param str The string
     * @return The string with replaced player name
     */
    public String replace(String str) {
    	if(debugMode) System.out.println("DEBUG: Replacing string!");
    	String[] stringArray = str.split(" ");
    	StringBuilder result = new StringBuilder();
    	for(int i = 0; i < stringArray.length; i++) {
    		if(debugMode) System.out.println("DEBUG: Replacing string[" + i + "]: '" + stringArray[i] + "'");
    		if(!stringArray[i].contains(Minecraft.getMinecraft().thePlayer.getName())) {
    			if(debugMode) System.out.println("DEBUG: String does not contain name!");
    			result.append(stringArray[i] + " ");
    			continue;
    		}
    		if(i != 0 && haveRank) {
    			// Check for rank
    			try {
    				if(stringArray[i - 1].contains("[") && stringArray[i - 1].contains("]")) {
        				String strResult = result.toString();
        				String newStr = "";
        				String[] split = strResult.split(" ");
        				for(int x = 0; x < split.length; x++) {
        					if(x != split.length - 1) newStr += split[x] + " ";
        					else {
        						String r = split[x].replaceAll("\\[.*?\\]", "");
        						if(uncount(r, ' ') != 0) r += " ";
        						newStr += r + " ";
        					}
        				}
        				newStr = newStr.substring(0, newStr.length() - 1);
        				result = new StringBuilder(newStr);
        			}
    			}catch(Exception ignored) {}
    		}
    		String nameRemoved = stringArray[i].replace(Minecraft.getMinecraft().thePlayer.getName(), "");
    		boolean overlapped = false;
    		for(char c : removeColorCode(nameRemoved).toCharArray()) {
    			if(Character.isDigit(c) || Character.isLetter(c)) {
    				if(debugMode) System.out.println("DEBUG: String overlapped (There is other digits/characters in it making it overlapped)");
    				overlapped = true;
    				break;
    			}
    		}    		
    		if(overlapped) result.append(stringArray[i] + " ");
    		else {
    			// Figure out to check if there is COLOR_CHAR behind stringArray[i]. If there is, take 2 or 1 (if only)
    			String full = "";
    			for(int b = 0; b < stringArray.length; b++) {
    				full += stringArray[b] + " ";
    			}
    			if(i != 0) full = full.split(stringArray[i])[0] + " ";
    			full = full += stringArray[i];
    			boolean containsColorCode = false;
    			for(String s : colorCodeSymbols) {
    				if(full.contains(s)) {
    					containsColorCode = true;
    					break;
    				}
    			}
    			if(!containsColorCode) {
    				if(debugMode) System.out.println("DEBUG: Full does NOT contain color codes (The chat does not have color code before it)");
    				result.append(stringArray[i].replace(Minecraft.getMinecraft().thePlayer.getName(), getReplaced()) + " ");
    				continue;
    			}
    			String colorCode = "";
    			if(debugMode) System.out.println("DEBUG: full = " + full);
    			try {
    				for(int g = 0; g < colorCodeSymbols.length; g++) {
    					String symbol = colorCodeSymbols[g];
    					try {
    						int occurence = countOccurences(full, symbol);
    						if(occurence == 0) {
    							if(debugMode) System.out.println("Occurence=0,Symbol Number " + g);
    							continue;
    						}else if(debugMode) System.out.println("Occurence!=0,Symbol Number " + g);
            				if(occurence == 1) {
                				// One
            					if(debugMode) System.out.println("DEBUG: Occurences of symbol is 1");
                				colorCode += COLOR_CHAR + full.split(symbol)[1].charAt(0);
                			}else {
                				if(debugMode) System.out.println("DEBUG: Occurences of symbol is Above 2");
                				// Above 2
                				String[] ccss = full.split(symbol);
                				List<String> ccssList = new ArrayList<String>();
                				if(debugMode) System.out.println("DEBUG: ccssList:");
                				for(int d = 1; d < ccss.length; d++) {
                					if(debugMode) System.out.println("DEBUG: [" + (i - 1) + "]: '" + ccss[d] + "'");
                					ccssList.add(ccss[d]);
                				}
                				ccss = ccssList.toArray(new String[ccssList.size() - 1]);
                				try {
                					colorCode += COLOR_CHAR + ccss[ccss.length - 2].charAt(0) + COLOR_CHAR + ccss[ccss.length - 1].charAt(0);
                					if(debugMode) System.out.println("DEBUG: First exception didnt occur");
                				}catch(Exception e1) {
                					try {
                						colorCode += COLOR_CHAR + stringArray[i].split(COLOR_CHAR)[1].toCharArray()[0];
                						if(debugMode) System.out.println("DEBUG: First exception occured but second didn't. First message: " + e1.getMessage());
                					}catch(Exception e2) {
                						if(debugMode) System.out.println("DEBUG: First AND second exception occured. First message: '" + e1.getMessage() + "', Second message: '" + e2.getMessage() + "'");
                					}
                				}
                			}
    					}catch(Exception ignored) {}
    				}
    			}catch(Exception ignored) {
    				if(debugMode) System.out.println("[DEBUG] ChatColor Exception:");
    				if(debugMode) ignored.printStackTrace();
    			}
    			String replaced = getReplaced();
    			replaced = replaced.replace("&y", colorCode);
    			for(String s : colorCodeSymbols) {
    				replaced = replaced.replace(s + "y", colorCode);
    			}
    			String finalStr = stringArray[i].replace(Minecraft.getMinecraft().thePlayer.getName(), replaced + colorCode) + " ";
    			result.append(finalStr);
    			if(debugMode) System.out.println("DEBUG: Color code: '" + colorCode + "'");
    			if(debugMode) System.out.println("DEBUG: Final String: '" + finalStr + "'");
    		}
    	}
    	String stringResult = result.toString();
    	stringResult = stringResult.substring(0, stringResult.length() - 1);
    	if(debugMode) System.out.println("DEBUG: String RESULT: '" + stringResult + "'");
    	return stringResult;
    }
    /**
     * Count occurrences of a substring in a string
     * @param main The string you want to count occurrences from
     * @param sub The substring you want to check
     * @return How many substrings there are in main string
     */
    public int countOccurences(String main, String sub) {
    	int lastIndex = 0;
    	int count = 0;
    	while(lastIndex != -1) {
    		lastIndex = main.indexOf(sub, lastIndex);
    		
    		if(lastIndex != -1) {
    			count++;
    			lastIndex += sub.length();
    		}
    	}
    	if(debugMode) System.out.println("DEBUG: countOccurences(sub:" + sub + ",main:" + main + ") result: " + count);
    	return count;
    }
    /**
     * Sets configuration for key and value
     * @param key Config Key
     * @param value Config Value
     */
    public void setConfiguration(String key, String value) {
    	if(!new File(ConfigHandler.getFile()).exists()) {
    		System.out.println("DEBUG: Setting configuration but file doesnt exist. Initializing file...");
    		ConfigHandler.init();
    	}
    	ConfigHandler.writeConfig(categoryName, key, value);
    	ConfigHandler.config.save();
    	if(debugMode) System.out.println("DEBUG: ConfigWrite(" + categoryName + ") " + key + ":" + value);
    	if(Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.refreshDisplayName();
    }
    protected static class ChatColor {
    	public static final ChatColor BLACK = new ChatColor('0');
    	public static final ChatColor DARK_BLUE = new ChatColor('1');
    	public static final ChatColor DARK_GREEN = new ChatColor('2');
    	public static final ChatColor DARK_AQUA = new ChatColor('3');
    	public static final ChatColor DARK_RED = new ChatColor('4');
    	public static final ChatColor DARK_PURPLE = new ChatColor('5');
    	public static final ChatColor GOLD = new ChatColor('6');
    	public static final ChatColor GRAY = new ChatColor('7');
    	public static final ChatColor DARK_GRAY = new ChatColor('8');
    	public static final ChatColor BLUE = new ChatColor('9');
    	public static final ChatColor GREEN = new ChatColor('a');
    	public static final ChatColor AQUA = new ChatColor('b');
    	public static final ChatColor RED = new ChatColor('c');
    	public static final ChatColor LIGHT_PURPLE = new ChatColor('d');
    	public static final ChatColor YELLOW = new ChatColor('e');
    	public static final ChatColor WHITE = new ChatColor('f');
    	
    	public static final ChatColor OBFUSCATED = new ChatColor('k');
    	public static final ChatColor BOLD = new ChatColor('l');
    	public static final ChatColor STRIKETHROUGH = new ChatColor('m');
    	public static final ChatColor UNDERLINE = new ChatColor('n');
    	public static final ChatColor ITALIC = new ChatColor('o');
    	public static final ChatColor RESET = new ChatColor('r');
    	
    	private static final char[] colorChars = "0123456789abcdefklmnor".toCharArray();
    	
    	public static boolean isColorChar(char c) {
    		for(char chars : colorChars) {
    			if(c == chars) return true;
    		}
    		return false;
    	}
    	
    	public String getName() {
    		try {
    			for(Field f : ChatColor.class.getDeclaredFields()) {
    				if(Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {
    					ChatColor c = (ChatColor) f.get(null);
    					if(c.c == this.c) return f.getName();
    				}
    			}
    		}catch(Exception ignored) {}
    		return null;
    	}
    	
    	public boolean equals(ChatColor c) {
    		return this.c == c.c;
    	}
    	
    	private String toString;
    	private char c;
    	public ChatColor(char c) {
    		toString = NameChanger.COLOR_CHAR + c;
    		this.c = c;
    	}
    	@Override
    	public String toString() {
    		return toString;
    	}
    	@Override
    	protected ChatColor clone() {
    		return new ChatColor(c);
    	}
    }
}
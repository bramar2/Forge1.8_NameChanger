package bramar.namechanger.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import static bramar.namechanger.NameChanger.P_NAME;
import static bramar.namechanger.NameChanger.P_PREFIX;
import static bramar.namechanger.NameChanger.P_SUFFIX;

import bramar.namechanger.ConfigHandler;
import bramar.namechanger.NameChanger;

public class RefreshNameCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "refreshname";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Refreshes the display name of the player. Can be used to refresh the name IF it has been configured in the config file.";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		try {
			NameChanger.instance.refresh();
		}catch(Exception e1) {
			sender.addChatMessage(new ChatComponentText("An unexcepted error occured: " + e1.getClass().getName() + ": " + e1.getMessage() + " [Please contact the author of the mod]").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
}

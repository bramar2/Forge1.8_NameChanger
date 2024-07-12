package bramar.namechanger.command;

import bramar.namechanger.NameChanger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class ResetNameCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "resetname";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Usage: /resetname: Resets your prefix, suffix, and name!";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(sender instanceof EntityPlayer) {
			try {
				((EntityPlayer)sender).refreshDisplayName();
			}catch(Exception ignored) {}
		}
		NameChanger.P_PREFIX = "";
		NameChanger.P_NAME = "";
		NameChanger.P_SUFFIX = "";
		NameChanger.instance.setConfiguration("PlayerName", "");
		NameChanger.instance.setConfiguration("PlayerPrefix", "");
		NameChanger.instance.setConfiguration("PlayerSuffix", "");
		sender.addChatMessage(new ChatComponentText("Prefix, name, and suffix has been all resetted in-game and in the config file.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)));
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

}

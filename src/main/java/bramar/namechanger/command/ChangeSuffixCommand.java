package bramar.namechanger.command;

import bramar.namechanger.NameChanger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class ChangeSuffixCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "changesuffix";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Usage: /changesuffix \"Enter suffix here\": Changes your suffix! (Client-side)";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(sender instanceof EntityPlayer) {
			try {
				((EntityPlayer)sender).refreshDisplayName();
			}catch(Exception ignored) {}
		}
		if(args.length == 0) {
			sender.addChatMessage(new ChatComponentText(this.getCommandUsage(sender)).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			return;
		}
		String allArgs = "";
		for(int i = 0; i < args.length; i++) {
			allArgs += args[i] + " ";
		}
		allArgs = allArgs.replace("\\\"", "{DoubleQuotesHere69420}");
		try {
			allArgs = allArgs.substring(0, allArgs.length() - 1);
		}catch(Exception ignored) {}
		if(!allArgs.contains("\"")) {
			sender.addChatMessage(new ChatComponentText(this.getCommandUsage(sender)).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			return;
		}
		if(NameChanger.count(allArgs, '\"') == 1) {
			sender.addChatMessage(new ChatComponentText(this.getCommandUsage(sender)).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			return;
		}
		
		String name = allArgs.split("\"")[1];
		name = name.replace("{DoubleQuotesHere69420}", "\"");
		NameChanger.P_SUFFIX = name;
		NameChanger.instance.setConfiguration("PlayerSuffix", name);
		sender.addChatMessage(new ChatComponentText("It has been set to \"" + name + "\"").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)));
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

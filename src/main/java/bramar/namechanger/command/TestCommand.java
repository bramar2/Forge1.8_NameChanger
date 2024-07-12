package bramar.namechanger.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class TestCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "nctest";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "No usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		// No testing currently
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
package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Flags;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.MenuBuilder.chat.ChatMenuBuilder;
import hu.tryharddood.advancedkits.MenuBuilder.chat.LineBuilder;
import hu.tryharddood.advancedkits.MenuBuilder.chat.component.MenuComponentCheckbox;
import hu.tryharddood.advancedkits.Utils.LineUtils;
import hu.tryharddood.advancedkits.Utils.Minecraft;
import hu.tryharddood.advancedkits.Variables;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

import static hu.tryharddood.advancedkits.Utils.I18n.tl;


/**
 * Class:
 *
 * @author TryHardDood
 */
public class SetFlagCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.KITADMIN_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/kit setflag <kit> <flag> [value]";
	}

	@Override
	public String getDescription() {
		return "Sets a flag for a kit.";
	}

	@Override
	public int getArgs() {
		return -1;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;

		if (args.length <= 2)
		{
			sender.sendMessage(tl("chat_usage") + ":");
			sender.sendMessage(ChatColor.GREEN + getUsage() + ChatColor.GRAY + " - " + ChatColor.BLUE + getDescription());
			return;
		}

		Kit kit = AdvancedKits.getKitManager().getKit(args[1]);
		if (kit == null)
		{
			sendMessage(sender, tl("error_kit_not_found"), ChatColor.RED);
			return;
		}

		String[] strings = Arrays.copyOfRange(args, 2, args.length);
		String   value   = getArgString(strings, 1);

		Boolean found = false;

		kit.setSave(true);
		for (Flags flag : Flags.values())
		{
			if (flag.getName().equalsIgnoreCase(strings[0]))
			{
				found = true;
				if (strings.length == 1)
				{
					if (flag.getType().equals(Boolean.class))
					{
						new ChatMenuBuilder().withLine(new LineBuilder().append("Click here to change the state of the flag: ").append(new MenuComponentCheckbox((Boolean) kit.getFlag(flag, false)).onChange((player1, oldValue, newValue) ->
						{
							kit.setFlag(flag, newValue);
							player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + flag.getName() + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + newValue);
						}))).show(player);
					}
					else if (flag.getType().equals(Material.class))
					{
						ItemStack itemStack = AdvancedKits.ServerVersion.newerThan(Minecraft.Version.v1_9_R1) ? player.getInventory().getItemInMainHand() : player.getItemInHand();
						if (itemStack == null || itemStack.getType() == Material.AIR)
						{
							player.sendMessage(ChatColor.RED + LineUtils.line);
							player.sendMessage(LineUtils.newline);

							player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GOLD + "" + ChatColor.BOLD + "Attention!"));
							player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, "You can't set the kit's icon to: "));
							player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.AQUA + "" + ChatColor.BOLD + Material.AIR.toString().toLowerCase()));

							player.sendMessage(LineUtils.newline);
							player.sendMessage(ChatColor.RED + LineUtils.line);

							kit.setSave(false);
							return;
						}

						ItemMeta itemMeta = itemStack.getItemMeta();

						player.sendMessage(ChatColor.GREEN + LineUtils.line);
						player.sendMessage(LineUtils.newline);

						player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, "Are you sure you want to set the kit's icon to: "));
						player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : ChatColor.AQUA + "" + ChatColor.BOLD + itemStack.getType().toString().toLowerCase()));
						new ChatMenuBuilder().withLine(new LineBuilder().append(player12 ->
								{
									kit.setFlag(flag, itemStack.getType().toString());
									player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + flag.getName() + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + itemStack.getType().toString());
								},
								new TextComponent(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, ChatColor.GREEN + "" + ChatColor.BOLD + "Yes!")))
								.append(LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.RED + "" + ChatColor.BOLD + "No!")))
								.show(player);

						player.sendMessage(LineUtils.newline);
						player.sendMessage(ChatColor.GREEN + LineUtils.line);
					}
				}
				else if (strings.length > 1)
				{
					if (flag.getType().equals(String.class))
					{
						kit.setFlag(flag, value);
						player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + flag.getName() + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + value);
					}
					else if (flag.getType().equals(Integer.class))
					{
						if (!isNumeric(value))
						{
							player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_integer"));
							kit.setSave(false);
							return;
						}

						kit.setFlag(flag, Integer.valueOf(value));
						player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + flag.getName() + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + value);
					}
					else if (flag.getType().equals(Double.class))
					{
						if (!isDouble(value))
						{
							player.sendMessage(AdvancedKits.getConfiguration().getChatPrefix() + " " + ChatColor.RED + tl("kitadmin_flag_integer"));
							kit.setSave(false);
							return;
						}

						kit.setFlag(flag, Double.valueOf(value));
						player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + flag.getName() + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + value);
					}
					else if (flag.getType().equals(Boolean.class))
					{
						if (!value.contains("true") && !value.contains("false"))
						{
							player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error! " + tl("kitadmin_flag_boolean"));
							kit.setSave(false);
							return;
						}

						kit.setFlag(flag, Boolean.valueOf(value));
						player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + flag.getName() + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + value);
					}
					else if (flag.getType().equals(Material.class))
					{
						Material material = Material.matchMaterial(value);
						if (material == null || material == Material.AIR)
						{
							player.sendMessage(ChatColor.RED + LineUtils.line);
							player.sendMessage(LineUtils.newline);

							player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GOLD + "" + ChatColor.BOLD + "Attention!"));
							player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, "You can't set the kit's icon to: "));
							player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.AQUA + "" + ChatColor.BOLD + value.toLowerCase()));

							player.sendMessage(LineUtils.newline);
							player.sendMessage(ChatColor.RED + LineUtils.line);

							kit.setSave(false);
							return;
						}

						player.sendMessage(ChatColor.GREEN + LineUtils.line);
						player.sendMessage(LineUtils.newline);

						player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, "Are you sure you want to set the kit's icon to: "));
						player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.AQUA + "" + ChatColor.BOLD + material.toString().toLowerCase()));
						new ChatMenuBuilder().withLine(new LineBuilder().append(player12 ->
								{
									kit.setFlag(flag, material.toString());
									player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + flag.getName() + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + material.toString());
								},
								new TextComponent(LineUtils.getCenteredMessage(LineUtils.Aligns.LEFT, ChatColor.GREEN + "" + ChatColor.BOLD + "Yes!")))
								.append(LineUtils.getCenteredMessage(LineUtils.Aligns.RIGHT, ChatColor.RED + "" + ChatColor.BOLD + "No!")))
								.show(player);

						player.sendMessage(LineUtils.newline);
						player.sendMessage(ChatColor.GREEN + LineUtils.line);
					}
				}
			}
		}
		if (!found)
		{
			if (strings[0].equalsIgnoreCase("addworld"))
			{
				if (kit.getWorlds().contains(value))
				{
					player.sendMessage(ChatColor.RED + LineUtils.line);
					player.sendMessage(LineUtils.newline);

					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GOLD + "" + ChatColor.BOLD + "Attention!"));
					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, tl("kitadmin_flag_addworld_wrong")));
					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.AQUA + "" + ChatColor.BOLD + value.toLowerCase()));

					player.sendMessage(LineUtils.newline);
					player.sendMessage(ChatColor.RED + LineUtils.line);

					kit.setSave(false);
					return;
				}

				kit.AddWorld(value);
				player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + "blockedworlds" + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + kit.getWorlds().toString());
			}
			else if (strings[0].equalsIgnoreCase("removeworld") || strings[0].equalsIgnoreCase("delworld"))
			{
				if (!kit.getWorlds().contains(value))
				{
					player.sendMessage(ChatColor.RED + LineUtils.line);
					player.sendMessage(LineUtils.newline);

					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GOLD + "" + ChatColor.BOLD + "Attention!"));
					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, tl("kitadmin_flag_delworld_wrong")));
					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.AQUA + "" + ChatColor.BOLD + value.toLowerCase()));

					player.sendMessage(LineUtils.newline);
					player.sendMessage(ChatColor.RED + LineUtils.line);

					kit.setSave(false);
					return;
				}

				kit.RemoveWorld(value);
				player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + "blockedworlds" + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + kit.getWorlds().toString());
			}
			else if (strings[0].equalsIgnoreCase("addcommand"))
			{
				if (kit.getCommands().contains(value))
				{
					player.sendMessage(ChatColor.RED + LineUtils.line);
					player.sendMessage(LineUtils.newline);

					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GOLD + "" + ChatColor.BOLD + "Attention!"));
					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, tl("kitadmin_flag_addcommand_wrong")));
					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.AQUA + "" + ChatColor.BOLD + value.toLowerCase()));

					player.sendMessage(LineUtils.newline);
					player.sendMessage(ChatColor.RED + LineUtils.line);

					kit.setSave(false);
					return;
				}

				kit.AddCommand(value);
				player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + "commands" + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + kit.getCommands().toString());
			}
			else if (strings[0].equalsIgnoreCase("removecommand") || strings[0].equalsIgnoreCase("delcommand"))
			{
				if (!kit.getCommands().contains(value))
				{
					player.sendMessage(ChatColor.RED + LineUtils.line);
					player.sendMessage(LineUtils.newline);

					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.GOLD + "" + ChatColor.BOLD + "Attention!"));
					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, tl("kitadmin_flag_delcommand_wrong")));
					player.sendMessage(LineUtils.getCenteredMessage(LineUtils.Aligns.CENTER, ChatColor.AQUA + "" + ChatColor.BOLD + value.toLowerCase()));

					player.sendMessage(LineUtils.newline);
					player.sendMessage(ChatColor.RED + LineUtils.line);

					kit.setSave(false);
					return;
				}

				kit.RemoveCommand(value);
				player.sendMessage(ChatColor.GREEN + "Flag '" + ChatColor.GOLD + "commands" + ChatColor.GREEN + "' value has been changed to: " + ChatColor.GOLD + kit.getCommands().toString());
			}
			else
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error! " + ChatColor.GRAY + "This flag doesn't exists.");
			}
		}
		kit.setSave(false);
	}
}
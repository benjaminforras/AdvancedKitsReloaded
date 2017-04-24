package hu.tryharddevs.advancedkits;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

/**
 * Register a command with the command system!
 * <br><br>
 * Handles arguments, argument type handling, predictions, tab completion, sub-sub-sub-commands, error handling, help pages, and more!
 * <br><br>
 * CommandManager manager = new CommandManager(plugin, "Help Tag", "command", "permission node");
 *
 * @author Stumblinbear
 */
public class CommandManager implements TabCompleter, CommandExecutor
{
	static         boolean                                                  GLOBAL_DEBUG = false;
	private static HashMap<Class<? extends AbstractArg<?>>, AbstractArg<?>> argInstances = new HashMap<>();
	String tag;
	String command;
	String permissionScheme;
	private ArrayList<Cmd>    commands       = new ArrayList<>();
	private ArrayList<Method> commandMethods = new ArrayList<>();

	public CommandManager(JavaPlugin plugin, String tag, String permissionScheme, String command, String... aliases)
	{
		this.tag = tag;
		this.command = command;
		this.permissionScheme = permissionScheme;

		// Used to inject the command without using plugin.yml
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

			bukkitCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);

			PluginCommand pluginCommand = c.newInstance(command, plugin);
			pluginCommand.setTabCompleter(this);
			pluginCommand.setExecutor(this);
			if (aliases.length > 0) pluginCommand.setAliases(Arrays.asList(aliases));
			commandMap.register(command, pluginCommand);

			loadCommandClass(this.getClass());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Cmd(cmd = "debug", args = "[bool]", argTypes = {Arg.ArgBoolean.class}, help = "Toggle debug mode.", longhelp = "Toggle debug mode. Shows information on command usage.", only = CommandOnly.OP, permission = "debug")
	public static CommandFinished cmdToggleDebugMode(CommandSender sender, Object[] args)
	{
		GLOBAL_DEBUG = (args.length != 0 ? (Boolean) args[0] : !GLOBAL_DEBUG);
		sender.sendMessage(ChatColor.YELLOW + "Debug mode is now: " + (GLOBAL_DEBUG ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
		return CommandFinished.DONE;
	}

	/**
	 * Parses classes for @Cmd annotations.
	 */
	public CommandManager loadCommandClass(Class<?> commandClass)
	{
		try {
			for (Method method : commandClass.getMethods()) {
				if (method.isAnnotationPresent(Cmd.class)) {
					Cmd cmd = method.getAnnotation(Cmd.class);
					commands.add(cmd);
					commandMethods.add(method);
				}
			}
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}

		return this;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		List<String> predictions = new ArrayList<>();
		String       token       = (args.length == 0 ? "" : args[args.length - 1]);

		for (Cmd c : commands) {
			List<String> cmdPredictions = getPredicted(c, token, args.length - 1);
			// Prevent duplicate entries
			if (cmdPredictions != null) {
				for (String str : cmdPredictions) {
					if (!predictions.contains(str)) predictions.add(str);
				}
			}
		}

		return predictions;
	}

	/**
	 * Get a prediction of the next command argument.
	 */
	private List<String> getPredicted(Cmd c, String token, int i)
	{
		String[] cmdArg = c.cmd().split(" ");
		// If no token, return all possible commands.
		if (token == "") return Collections.singletonList(cmdArg[0]);
		// If the amount of args is more than available, or it doesn't start with the token.
		if (i >= cmdArg.length) {
			int argNum = i - cmdArg.length;
			if (argNum >= c.argTypes().length) {
				return null;
			}
			else {
				if (!argInstances.containsKey(c.argTypes()[argNum])) {
					try {
						argInstances.put(c.argTypes()[argNum], c.argTypes()[argNum].newInstance());
					}
					catch (Exception ignored) {
					}
				}
				AbstractArg<?> absArg = argInstances.get(c.argTypes()[argNum]);
				return absArg.getPredictions();
			}
			// If it doesn't start with the token.
		}
		else if (!cmdArg[i].startsWith(token)) return null;
		// It must be a match!
		return Arrays.asList(cmdArg[i]);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		StopWatch sw = null;
		if (GLOBAL_DEBUG) {
			sw = new StopWatch();
			sw.start();
		}

		CommandFinished finishedType = runCommand(sender, args);
		if (finishedType.shouldPrint()) {
			sender.sendMessage(ChatColor.RED + finishedType.getErrorString());

			// Do our best to predict which command was going to be used.
			if (finishedType == CommandFinished.COMMAND) {
				// TreeMaps automatically sort by numbers.
				TreeMap<Double, Cmd> possible = new TreeMap<>();
				for (Cmd c : commands) {
					// Reduce arg array to the shortest one.
					String[] fixedArgs = new String[c.cmd().split(" ").length];
					System.arraycopy(args, 0, fixedArgs, 0, (args.length > fixedArgs.length ? fixedArgs.length : args.length));

					// Combine the arguments.
					String cmdArgs = StringUtils.join(fixedArgs, " ");

					// Use Levenshtein Distance to get how similar the two strings are to each other. Calculate percentage with the value returned.
					possible.put((1D - (StringUtils.getLevenshteinDistance(cmdArgs, c.cmd()) / (Math.max(cmdArgs.length(), c.cmd().length()) * 1D))) * 100D, c);
				}

				// Are there even any predictions?
				if (possible.size() > 0) {
					// Get the last entry. (The one with the highest possibility)
					Map.Entry<Double, Cmd> entry = possible.pollLastEntry();
					sender.sendMessage("");
					sender.sendMessage(ChatColor.GOLD + "   Did you mean: " + ChatColor.GRAY + "/" + label + " " + entry.getValue().cmd() + ChatColor.GOLD + "? We're " + ((int) (entry.getKey() * 10) / 10D) + "% sure.");
					sender.sendMessage("");
				}
			}
		}

		if (GLOBAL_DEBUG && sw != null) {
			sw.stop();
			sender.sendMessage(ChatColor.YELLOW + "Command took " + sw.getTime() + " milliseconds to complete.");
		}
		return true;
	}

	/**
	 * Find the best command to run and do so.
	 */
	public CommandFinished runCommand(CommandSender sender, String[] args)
	{
		try {
			// Display help if no args, or they use the help command
			if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
				return displayHelp(sender, args.length == 0 ? null : args);
			}
			else {
				Cmd bestFit      = null;
				int bestFit_i    = 0;
				int bestFit_args = -1;

				// Loop through commands until a suitable one is found
				for (int i = 0; i < commands.size(); i++) {
					Cmd cmd = commands.get(i);

					// Split the base command and check for a match
					String[] cmds = cmd.cmd().split(" ");
					if (args.length >= cmds.length) {
						boolean valid = true;
						for (int j = 0; j < cmds.length; j++) {
							if (!cmds[j].equalsIgnoreCase(args[j])) {
								valid = false;
								break;
							}
						}

						if (!valid) continue;
					}
					else {
						continue;
					}

					// Check if it's better than the best fit.
					if (cmd.cmd().split(" ").length > bestFit_args) {
						bestFit = cmd;
						bestFit_i = i;
						bestFit_args = cmd.cmd().split(" ").length;
					}
					else {
						continue;
					}
				}

				if (bestFit != null) {
					// Check the "only" argument
					if (sender instanceof Player) {
						if (bestFit.only() == CommandOnly.CONSOLE) return CommandFinished.NOPLAYER;
					}
					else if (bestFit.only() == CommandOnly.PLAYER) return CommandFinished.NOCONSOLE;

					// Check for the "op" argument and permission argument
					if ((bestFit.only() == CommandOnly.OP && !sender.isOp()) || (!bestFit.permission().equals("") && !sender.hasPermission(permissionScheme + "." + bestFit.permission()))) {
						return CommandFinished.PERMISSION;
					}

					// Split up the args; arguments in quotes count as a single argument.
					List<Object> cmdArgList = new ArrayList<>();
					Matcher      m          = Pattern.compile("(?:([^\"]\\S*)|\"(.+?)\")\\s*").matcher(StringUtils.join(args, " ").replaceFirst(bestFit.cmd(), "").trim());
					for (int j = 0; m.find(); j++) {
						// Apply the requested argument type.
						Class<? extends AbstractArg<?>> requestedType = (j < bestFit.argTypes().length ? bestFit.argTypes()[j] : Arg.ArgString.class);
						// Cache the instance.
						if (!argInstances.containsKey(requestedType)) {
							argInstances.put(requestedType, requestedType.newInstance());
						}
						AbstractArg<?> absArg = argInstances.get(requestedType);
						try {
							Object arg = absArg.parseArg(m.group(1) != null ? m.group(1) : m.group(2));
							if (arg == null)
							// Some argument parsers don't throw an exception. Just an extra precaution.
							{
								throw new CommandException(absArg.getFailure() + " (" + m.group(1) != null ? m.group(1) : m.group(2) + ")");
							}
							cmdArgList.add(arg);
						}
						catch (Exception e) {
							return CommandFinished.CUSTOM.replace(absArg.getFailure() + " (" + (m.group(1) != null ? m.group(1) : m.group(2)) + ")");
						}
					}

					// Check that all the required arguments have been fulfilled.
					Object[] cmdArgsPassed = cmdArgList.toArray(new Object[cmdArgList.size()]);
					if (StringUtils.countMatches(bestFit.args(), "<") > cmdArgsPassed.length) {
						return CommandFinished.BADCOMMAND.replace(command + " " + bestFit.cmd() + " " + bestFit.args());
					}

					// Run the command :D
					return (CommandFinished) commandMethods.get(bestFit_i).invoke(null, sender, (cmdArgsPassed != null ? cmdArgsPassed : null));
				}
			}
		}
		catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
			if (e.getCause() instanceof CommandException) {
				return CommandFinished.CUSTOM.replace(e.getCause().getMessage());
			}
			if (GLOBAL_DEBUG) Bukkit.broadcastMessage(ChatColor.RED + "Error: " + getTrace(e));
			return CommandFinished.EXCEPTION;
		}
		catch (Exception e) {
			e.printStackTrace();
			if (GLOBAL_DEBUG) Bukkit.broadcastMessage(ChatColor.RED + "Error: " + getTrace(e));
			return CommandFinished.EXCEPTION;
		}

		return CommandFinished.COMMAND.replace(command);
	}

	/**
	 * Display the help menu.
	 */
	public CommandFinished displayHelp(CommandSender sender, String[] args)
	{
		ArrayList<String> helpList = new ArrayList<>(); // The help message buffer
		boolean           specific = false; // If "help <command>"
		int               perPage  = 8; // How many commands to show per page
		int               page     = 0; // Which page

		if (args != null && args.length != 1) {
			try {
				page = Integer.parseInt(args[1]) - 1;
				if (page < 0) // Negative pages are bad juju.
				{
					page = 0;
				}
			}
			catch (Exception e) {
				specific = true;
			} // If this fails, it's probably a string. Check for a specific command.
		}

		String cmdLabel = null; // The label of the specific command.

		if (specific && args.length != 1) {
			perPage = 4; // Reduce the amount to show per page.
			cmdLabel = StringUtils.join(args, " ").split(" ", 2)[1]; // Because args = "help <command>" cut out "help".
		}

		for (Cmd cmd : commands) {
			// If looking for specific commands and it isn't the one we're looking for
			if (specific && !cmd.cmd().startsWith(cmdLabel)) continue;

			// Should it even show?
			if (cmd.showInHelp()) {
				// If it can't be used, don't show it! Simple! :D
				boolean canUse = cmd.permission().equals("") || (sender.hasPermission(permissionScheme + "." + cmd.permission()));

				// Is it op-only?
				if (cmd.only() == CommandOnly.OP) canUse = (cmd.permission().equals("") ? sender.isOp() : canUse);

				if (canUse) {
					helpList.add(ChatColor.GOLD + "/" + command + " " + cmd.cmd() + (!cmd.args().equals("") ? " " + cmd.args() : "") + ": " + ChatColor.WHITE + (specific ? (cmd.longhelp().equals("") ? cmd.help() : cmd.longhelp()) : cmd.help()));
				}
			}
		}

		// Make sure there's something to show.
		boolean badPage = true;
		if (helpList.size() >= page * perPage) {
			for (int j = 0; j < perPage; j++) {
				if (helpList.size() > (j + page * perPage)) {
					if (j == 0) {
						sender.sendMessage(ChatColor.YELLOW + "--------- " + ChatColor.WHITE + tag + " Help (" + (page + 1) + "/" + (int) Math.ceil(helpList.size() / (perPage * 1F)) + ")" + ChatColor.YELLOW + " ---------------------");
					}
					sender.sendMessage(helpList.get((j + page * perPage)));
					badPage = false;
				}
			}
		}

		if (badPage) {
			if (specific) {
				return CommandFinished.CUSTOM.replace("Command unrecognized.");
			}
			else {
				return CommandFinished.CUSTOM.replace("Page " + (page + 1) + " does not exist in help.");
			}
		}
		else if (helpList.size() > (page + 1) * perPage) {
			sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.YELLOW + "/" + command + " help " + (page + 2) + ChatColor.WHITE + " to see more help.");
		}
		return CommandFinished.DONE;
	}

	/**
	 * Returns the string version of an exception. Helps with in-game error checking.
	 */
	private String getTrace(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter  pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * Used to define who is allowed to use a command.
	 */
	public enum CommandOnly
	{
		/**
		 * Only players can use the command.
		 */
		PLAYER, /**
	 * Only op players can use the command. Can be overridden by a matched permission.
	 */
	OP, /**
	 * Only the console can use the command.
	 */
	CONSOLE, /**
	 * Anyone can use the command, given their permissions match.
	 */
	ALL
	}

	/**
	 * A set of command errors.
	 */
	public enum CommandFinished
	{
		/**
		 * Finished correctly
		 */
		DONE(false, "Done"), /**
	 * Command does not exist
	 */
	COMMAND(true, "Command does not exist. Use /%s for help."), /**
	 * Command does not exist
	 */
	BADCOMMAND(true, "Bad command usage: /%s "), /**
	 * Console not allowed to use
	 */
	NOCONSOLE(true, "This command cannot be run from the console."), /**
	 * Player not allowed to use
	 */
	NOPLAYER(true, "This command cannot be run by players."), /**
	 * Player does not exist
	 */
	EXISTPLAYER(true, "That player does not exist."), /**
	 * Incorrect permissions
	 */
	PERMISSION(true, getMessage("noPermission")),

		HOLDBLOCK(true, "You must be holding a block."), HOLDITEM(true, "You must be holding an item."),

		LONGSTRING(true, "String cannot be longer than %s!"),

		/**
		 * Custom error
		 */
		CUSTOM(true, "%s"), EXCEPTION(true, "An exception occured. Please contact a member of staff and tell them!");

		private boolean shouldPrint;
		private String  errorString;
		private String  extraString;

		CommandFinished(boolean par1ShouldPrint, String par1Error)
		{
			shouldPrint = par1ShouldPrint;
			errorString = par1Error;
		}

		public boolean shouldPrint()
		{
			return shouldPrint;
		}

		public String getErrorString()
		{
			if (extraString != null) {
				return errorString.replace("%s", extraString);
			}
			else {
				return errorString;
			}
		}

		public CommandFinished replace(String theString)
		{
			extraString = theString;
			return this;
		}
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	/**
	 * Attach to a function to denote it as a command. You must register the class it is in as a command class before it will be used.
	 */ public @interface Cmd
	{
		/**
		 * Takes in any amount of subcommands. The command handler always chooses the best-fit command.
		 * <br><br>
		 * Example:
		 * <br>
		 * cmd = "sub1 sub2 sub3 sub4 sub5"
		 */
		String cmd() default "";

		/**
		 * The arguments for the command. Required arguments must be enclosed in <>'s
		 * <br><br>
		 * Example:
		 * <br>
		 * args = "&lt;arg1&gt; &lt;arg2&gt; [arg3]"
		 * <br>
		 * Argument 1 and 2 are required; the third is optional.
		 */
		String args() default "";

		/**
		 * Specifies the type that an argument should be. Default is Arg.ArgString.
		 * <br><br>
		 * Example:
		 * <br>
		 * argTypes = { Arg.ArgInteger, Arg.ArgString, Arg.ArgPlayer }
		 * <ul>
		 * <li>Argument 1 <i>must</i> be an integer.</li>
		 * <li>Argument 2 <i>must</i> be a string(anything).</li>
		 * <li>Argument 3 <i>must</i> be an online player.</li>
		 * </ul>
		 */
		Class<? extends AbstractArg<?>>[] argTypes() default {};

		/**
		 * The text to show next to a command when a user does /cmd help.
		 */
		String help() default "Default help thingy... :(";

		/**
		 * The text to show next to a command when a user does /cmd help &lt;command&gt;.
		 * <br><br>
		 * Use to give more information about the command.
		 */
		String longhelp() default "";

		/**
		 * Should the command be shown in help at all?
		 */
		boolean showInHelp() default true;

		/**
		 * Specifies if the command should be restricted to CONSOLE, OP, or PLAYERS. Otherwise, ALL.
		 */
		CommandOnly only() default CommandOnly.ALL;

		/**
		 * The permission node to use.
		 * <br><br>
		 * Example:
		 * <br>
		 * new CommandManager(plugin, "Test", "testnode", "test");
		 * <br>
		 * permission = "edit"
		 * <br>
		 * This setup would require a player to have testnode.edit to use the command.
		 */
		String permission() default "";
	}

	/*
	 * Arguments
	 */
	public interface IArgParse<T>
	{
		T parseArg(String arg);

		List<String> getPredictions();

		String getFailure();
	}

	/**
	 * This can be used to immediately throw an error without returning a <code>CommandFinished</code>.
	 * It'll display the specified error.
	 */
	public static class CommandException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public CommandException(String message)
		{
			super(message);
		}
	}

	public static abstract class AbstractArg<T> implements IArgParse<T>
	{
		public AbstractArg() { }
	}

	public static class Arg
	{
		public static class ArgArray extends AbstractArg<List<String>>
		{
			public List<String> parseArg(String arg)
			{
				List<String> list = new ArrayList<>();
				for (String str : arg.split(","))
					list.add(str);
				return list;
			}

			public String getFailure()
			{
				return "Argument failure.";
			}

			public List<String> getPredictions() { return null; }
		}

		public static class ArgBoolean extends AbstractArg<Boolean>
		{
			public Boolean parseArg(String arg)
			{
				if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("yes") || arg.equalsIgnoreCase("on") || arg.equalsIgnoreCase("1")) {
					return true;
				}
				if (arg.equalsIgnoreCase("false") || arg.equalsIgnoreCase("no") || arg.equalsIgnoreCase("off") || arg.equalsIgnoreCase("0")) {
					return false;
				}
				return null;
			}

			public String getFailure()
			{
				return "Argument not a valid boolean.";
			}

			public List<String> getPredictions() { return Arrays.asList("true", "false"); }
		}

		public static class ArgByte extends AbstractArg<Byte>
		{
			public Byte parseArg(String arg)
			{
				return Byte.valueOf(arg);
			}

			public String getFailure()
			{
				return "Argument not a valid byte.";
			}

			public List<String> getPredictions() { return Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"); }
		}

		public static class ArgDouble extends AbstractArg<Double>
		{
			public Double parseArg(String arg)
			{
				return Double.valueOf(arg);
			}

			public String getFailure()
			{
				return "Argument not a real number.";
			}

			public List<String> getPredictions() { return null; }
		}

		public static class ArgFloat extends AbstractArg<Float>
		{
			public Float parseArg(String arg)
			{
				return Float.valueOf(arg);
			}

			public String getFailure()
			{
				return "Argument not a floating point number.";
			}

			public List<String> getPredictions() { return null; }
		}

		public static class ArgInteger extends AbstractArg<Integer>
		{
			public Integer parseArg(String arg)
			{
				return Integer.valueOf(arg);
			}

			public String getFailure()
			{
				return "Argument not an integer.";
			}

			public List<String> getPredictions() { return null; }
		}

		public static class ArgPlayer extends AbstractArg<OfflinePlayer>
		{
			public Player parseArg(String arg)
			{
				return Bukkit.getPlayer(arg);
			}

			public String getFailure()
			{
				return CommandFinished.EXISTPLAYER.getErrorString();
			}

			public List<String> getPredictions()
			{
				List<String> players = new ArrayList<>();
				for (Player p : Bukkit.getOnlinePlayers())
					players.add(p.getName());
				return players;
			}
		}

		public static class ArgOfflinePlayer extends AbstractArg<OfflinePlayer>
		{
			@SuppressWarnings("deprecation")
			public OfflinePlayer parseArg(String arg)
			{
				return Bukkit.getOfflinePlayer(arg);
			}

			public String getFailure()
			{
				return CommandFinished.EXISTPLAYER.getErrorString();
			}

			public List<String> getPredictions()
			{
				List<String> players = new ArrayList<>();
				for (Player p : Bukkit.getOnlinePlayers())
					players.add(p.getName());
				return players;
			}
		}

		public static class ArgString extends AbstractArg<String>
		{
			public String parseArg(String arg)
			{
				return arg;
			}

			public String getFailure()
			{
				return "Could not parse string.";
			}

			public List<String> getPredictions() { return null; }
		}
	}
}
package hu.tryharddevs.advancedkits.utils.localization;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;


public class I18n {
	private static final String         MESSAGES     = "messages";
	private static final Pattern        NODOUBLEMARK = Pattern.compile("''");
	private static final ResourceBundle NULL_BUNDLE  = new ResourceBundle() {
		public Enumeration<String> getKeys() {
			return null;
		}

		protected Object handleGetObject(String key) {
			return null;
		}
	};
	private static I18n instance;
	private final transient Locale defaultMessageLocale = Locale.getDefault();
	private final transient ResourceBundle   defaultBundle;
	private final transient AdvancedKitsMain plugin;
	private transient Locale currentMessageLocale = defaultMessageLocale;
	private transient ResourceBundle customBundle;
	private transient ResourceBundle localeBundle;
	private transient Map<String, MessageFormat> messageFormatCache = new HashMap<>();

	public I18n(final AdvancedKitsMain plugin) {
		this.plugin = plugin;

		defaultBundle = ResourceBundle.getBundle(MESSAGES, Locale.ENGLISH, new UTF8Control());
		localeBundle = defaultBundle;
		customBundle = NULL_BUNDLE;
	}

	public static String getMessage(final String string) {
		return getMessage(string, new Object[0]);
	}

	public static String getMessage(final String string, final Object... objects) {
		if (instance == null) {
			return "";
		}
		if (objects.length == 0) {
			return ChatColor.translateAlternateColorCodes('&', NODOUBLEMARK.matcher(instance.translate(string)).replaceAll("'"));
		} else {
			return ChatColor.translateAlternateColorCodes('&', instance.format(string, objects));
		}
	}

	public static String capitalCase(final String input) {
		return input == null || input.length() == 0 ? input : input.toUpperCase(Locale.ENGLISH).charAt(0) + input.toLowerCase(Locale.ENGLISH).substring(1);
	}

	public void onEnable() {
		instance = this;
	}

	public void onDisable() {
		instance = null;
	}

	public Locale getCurrentLocale() {
		return currentMessageLocale;
	}

	private String translate(final String string) {
		try {
			try {
				return customBundle.getString(string);
			} catch (MissingResourceException ex) {
				return localeBundle.getString(string);
			}
		} catch (MissingResourceException ex) {
			plugin.log(ChatColor.RED + ex.getMessage());
			plugin.log(ChatColor.RED + String.format("Missing translation key \"%s\" in translation file %s", ex.getKey(), localeBundle.getLocale().toString()));
			return defaultBundle.getString(string);
		}
	}

	private String format(final String string, final Object... objects) {
		String        format        = translate(string);
		MessageFormat messageFormat = messageFormatCache.get(format);
		if (messageFormat == null) {
			try {
				messageFormat = new MessageFormat(format);
			} catch (IllegalArgumentException e) {
				plugin.log(ChatColor.RED + e.getMessage());
				plugin.log(ChatColor.RED + "Invalid Translation key for '" + string + "': " + e.getMessage());
				format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
				messageFormat = new MessageFormat(format);
			}
			messageFormatCache.put(format, messageFormat);
		}
		return messageFormat.format(objects);
	}

	public void updateLocale(final String loc) {
		if (loc != null && !loc.isEmpty()) {
			final String[] parts = loc.split("[_\\.]");
			if (parts.length == 1) {
				currentMessageLocale = new Locale(parts[0]);
			}
			if (parts.length == 2) {
				currentMessageLocale = new Locale(parts[0], parts[1]);
			}
			if (parts.length == 3) {
				currentMessageLocale = new Locale(parts[0], parts[1], parts[2]);
			}
		}
		ResourceBundle.clearCache();
		messageFormatCache = new HashMap<>();
		plugin.log(ChatColor.GREEN + "Locale: \"" + currentMessageLocale.toString() + "\" | Locale file: " + "messages_" + currentMessageLocale.toString() + ".properties");

		try {
			localeBundle = ResourceBundle.getBundle(MESSAGES, currentMessageLocale, new UTF8Control());
		} catch (MissingResourceException ex) {
			localeBundle = NULL_BUNDLE;
		}

		try {
			customBundle = ResourceBundle.getBundle(MESSAGES, currentMessageLocale, new FileResClassLoader(I18n.class.getClassLoader(), plugin), new UTF8Control());
		} catch (MissingResourceException ex) {
			customBundle = NULL_BUNDLE;
		}
	}

	private static class FileResClassLoader extends ClassLoader {
		private final transient File dataFolder;

		FileResClassLoader(final ClassLoader classLoader, final JavaPlugin plugin) {
			super(classLoader);
			this.dataFolder = plugin.getDataFolder();
		}

		@Override public URL getResource(final String string) {
			final File file = new File(dataFolder, string);
			if (file.exists()) {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException ignored) {
				}
			}
			return null;
		}

		@Override public InputStream getResourceAsStream(final String string) {
			final File file = new File(dataFolder, string);
			if (file.exists()) {
				try {
					return new FileInputStream(file);
				} catch (FileNotFoundException ignored) {
				}
			}
			return null;
		}
	}
}
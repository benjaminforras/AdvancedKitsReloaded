package hu.tryharddood.advancedkits.Utils;

import hu.tryharddood.advancedkits.AdvancedKits;
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
import java.util.logging.Level;
import java.util.regex.Pattern;


public class I18n
{
    private static final String         MESSAGES     = "messages";
    private static final Pattern        NODOUBLEMARK = Pattern.compile("''");
    private static final ResourceBundle NULL_BUNDLE  = new ResourceBundle()
    {
        public Enumeration<String> getKeys()
        {
            return null;
        }

        protected Object handleGetObject(String key)
        {
            return null;
        }
    };
    private static I18n instance;
    private final transient Locale defaultLocale = Locale.getDefault();
    private final transient ResourceBundle defaultBundle;
    private final transient JavaPlugin     plugin;
    private transient Locale currentLocale = defaultLocale;
    private transient ResourceBundle customBundle;
    private transient ResourceBundle localeBundle;
    private transient Map<String, MessageFormat> messageFormatCache = new HashMap<>();

    public I18n(final JavaPlugin plugin)
    {
        this.plugin = plugin;
        defaultBundle = ResourceBundle.getBundle(MESSAGES, Locale.ENGLISH);
        localeBundle = defaultBundle;
        customBundle = NULL_BUNDLE;
    }

    public static String tl(final String string, final Object... objects)
    {
        return tl(string, false, objects);
    }

    public static String tl(final String string, boolean stripcolor)
    {
        return tl(string, stripcolor, new Object[0]);
    }

    public static String tl(final String string, boolean stripcolor, final Object... objects)
    {
        if (instance == null)
        {
            return "";
        }
        if (objects.length == 0)
        {
            return stripcolor ? NODOUBLEMARK.matcher(instance.translate(string)).replaceAll("'") : ChatColor.translateAlternateColorCodes('&', NODOUBLEMARK.matcher(instance.translate(string)).replaceAll("'"));
        }
        else
        {
            return stripcolor ? instance.format(string, objects) : ChatColor.translateAlternateColorCodes('&', instance.format(string, objects));
        }
    }

    public static String capitalCase(final String input)
    {
        return input == null || input.length() == 0 ? input : input.toUpperCase(Locale.ENGLISH).charAt(0) + input.toLowerCase(Locale.ENGLISH).substring(1);
    }

    public void onEnable()
    {
        instance = this;
    }

    public void onDisable()
    {
        instance = null;
    }

    public Locale getCurrentLocale()
    {
        return currentLocale;
    }

    private String translate(final String string)
    {
        try
        {
            try
            {
                return customBundle.getString(string);
            }
            catch (MissingResourceException ex)
            {
                return localeBundle.getString(string);
            }
        }
        catch (MissingResourceException ex)
        {
            plugin.getLogger().log(Level.WARNING, String.format("Missing translation key \"%s\" in translation file %s", ex.getKey(), localeBundle.getLocale().toString()), ex);
            return defaultBundle.getString(string);
        }
    }

    private String format(final String string, final Object... objects)
    {
        String        format        = translate(string);
        MessageFormat messageFormat = messageFormatCache.get(format);
        if (messageFormat == null)
        {
            try
            {
                messageFormat = new MessageFormat(format);
            }
            catch (IllegalArgumentException e)
            {
                plugin.getLogger().log(Level.SEVERE, "Invalid Translation key for '" + string + "': " + e.getMessage());
                format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
                messageFormat = new MessageFormat(format);
            }
            messageFormatCache.put(format, messageFormat);
        }
        return messageFormat.format(objects);
    }

    public void updateLocale(final String loc)
    {
        if (loc != null && !loc.isEmpty())
        {
            final String[] parts = loc.split("[_\\.]");
            if (parts.length == 1)
            {
                currentLocale = new Locale(parts[0]);
            }
            if (parts.length == 2)
            {
                currentLocale = new Locale(parts[0], parts[1]);
            }
            if (parts.length == 3)
            {
                currentLocale = new Locale(parts[0], parts[1], parts[2]);
            }
        }
        ResourceBundle.clearCache();
        messageFormatCache = new HashMap<>();
        AdvancedKits.log(ChatColor.GREEN + "- Using locale " + currentLocale.toString());

        try
        {
            localeBundle = ResourceBundle.getBundle(MESSAGES, currentLocale);
        }
        catch (MissingResourceException ex)
        {
            localeBundle = NULL_BUNDLE;
        }

        try
        {
            customBundle = ResourceBundle.getBundle(MESSAGES, currentLocale, new FileResClassLoader(I18n.class.getClassLoader(), plugin));
        }
        catch (MissingResourceException ex)
        {
            customBundle = NULL_BUNDLE;
        }
    }

    private static class FileResClassLoader extends ClassLoader
    {
        private final transient File dataFolder;

        FileResClassLoader(final ClassLoader classLoader, final JavaPlugin plugin)
        {
            super(classLoader);
            this.dataFolder = plugin.getDataFolder();
        }

        @Override
        public URL getResource(final String string)
        {
            final File file = new File(dataFolder, string);
            if (file.exists())
            {
                try
                {
                    return file.toURI().toURL();
                }
                catch (MalformedURLException ignored)
                {
                }
            }
            return null;
        }

        @Override
        public InputStream getResourceAsStream(final String string)
        {
            final File file = new File(dataFolder, string);
            if (file.exists())
            {
                try
                {
                    return new FileInputStream(file);
                }
                catch (FileNotFoundException ignored)
                {
                }
            }
            return null;
        }
    }
}
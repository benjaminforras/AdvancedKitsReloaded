package hu.tryharddood.advancedkits;

import org.bukkit.ChatColor;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class: Phrases
 *
 * @author jjkoletar
 */
public class Phrases
{
    private static Phrases instance;
    private static transient Map<String, MessageFormat> hMessageFormatCache = new HashMap<>();
    private ResourceBundle phrases;
    private Properties overrides;

    private Phrases()
    {
    }

    public static Phrases getInstance()
    {
        if (instance == null)
        {
            instance = new Phrases();
        }
        return instance;
    }

    public static String phrase(String key, Object... replacements)
    {
        if (getInstance() == null)
        {
            return "";
        }
        if (getInstance().phrases == null)
        {
            return "Phrase Error! Did you /reload? Don't!";
        }
        if (!getInstance().phrases.containsKey(key))
        {
            Logger.getLogger("Minecraft").warning("[AdvancedKitsReloaded] Unknown translation key! '" + key + "'");
            return "";
        }
        String format;
        if (getInstance().overrides != null && getInstance().overrides.containsKey(key))
        {
            format = getInstance().overrides.getProperty(key);
        }
        else
        {
            format = getInstance().phrases.getString(key);
        }
        MessageFormat messageFormat = hMessageFormatCache.get(format);

        if (messageFormat == null)
        {
            try
            {
                messageFormat = new MessageFormat(format);
            } catch (IllegalArgumentException e)
            {
                format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
                messageFormat = new MessageFormat(format);
            }

            hMessageFormatCache.put(format, messageFormat);
        }

        return ChatColor.translateAlternateColorCodes('&', messageFormat.format(replacements));
    }

    public void initialize(Locale l)
    {
        phrases = ResourceBundle.getBundle("messages", l);
    }

    public void overrides(Properties overrides)
    {
        this.overrides = overrides;
    }
}

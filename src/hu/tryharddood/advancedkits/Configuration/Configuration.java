package hu.tryharddood.advancedkits.Configuration;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Phrases;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class Configuration
{
    private AdvancedKits instance;

    private boolean economy;
    private String chatprefix;
    private String locale;

    private YamlConfiguration yamlConfig;

    public Configuration(AdvancedKits instance)
    {
        this.instance = instance;
    }

    private void copy(InputStream in, File file)
    {
        try
        {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getChatPrefix()
    {
        return ChatColor.translateAlternateColorCodes('&', this.chatprefix);
    }

    public String getLocale()
    {
        return this.locale;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    public boolean isEconomy()
    {
        return this.economy;
    }

    public void setEconomy(boolean economy)
    {
        this.economy = economy;
    }

    public void loadConfiguration()
    {
        File configFile = new File(this.instance.getDataFolder(), "config.yml");
        if (!configFile.exists())
        {
            configFile.getParentFile().mkdirs();
            copy(this.instance.getResource("config.yml"), configFile);
        }
        this.yamlConfig = YamlConfiguration.loadConfiguration(configFile);

        setEconomy(this.yamlConfig.getBoolean("use-economy"));
        setChatprefix(this.yamlConfig.getString("chat-prefix"));
        setLocale(this.yamlConfig.getString("locale"));

        if (isEconomy() && !instance.setupEconomy())
        {
            this.instance.setupVault(this.instance.getServer().getPluginManager());
        }

        loadLanguageFile();
        AdvancedKits.log(ChatColor.GREEN + "Configuration loaded successfully");
    }

    private void loadLanguageFile()
    {
        Locale locale = new Locale(getLocale());
        Phrases.getInstance().initialize(locale);
        File overrides = new File(instance.getDataFolder(), "messages_en.properties");
        if (overrides.exists())
        {
            Properties overridesProps = new Properties();
            try
            {
                overridesProps.load(new FileInputStream(overrides));
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            Phrases.getInstance().overrides(overridesProps);
        }
    }

    public void setChatprefix(String chatprefix)
    {
        this.chatprefix = chatprefix;
    }
}

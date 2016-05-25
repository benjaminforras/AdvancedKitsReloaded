package hu.tryharddood.advancedkits.Configuration;

import hu.tryharddood.advancedkits.AdvancedKits;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class Configuration {
    private AdvancedKits instance;

    private boolean economy;
    private String chatprefix;
    private String locale;

    private YamlConfiguration yamlConfig;

    public Configuration(AdvancedKits instance) {
        this.instance = instance;
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getChatPrefix() {
        return ChatColor.translateAlternateColorCodes('&', this.chatprefix);
    }

    private void setChatprefix(String chatprefix) {
        this.chatprefix = chatprefix;
    }

    private String getLocale() {
        return this.locale;
    }

    private void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isEconomy() {
        return this.economy;
    }

    public void setEconomy(boolean economy) {
        this.economy = economy;
    }

    public void loadConfiguration() {
        File configFile = new File(this.instance.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            copy(this.instance.getResource("config.yml"), configFile);
        }
        this.yamlConfig = YamlConfiguration.loadConfiguration(configFile);

        setEconomy(this.yamlConfig.getBoolean("use-economy", true));
        setChatprefix(this.yamlConfig.getString("chat-prefix", "&7[&6AdvancedKits&7]"));
        setLocale(this.yamlConfig.getString("locale", "en"));

        if (isEconomy() && !instance.setupEconomy()) {
            this.instance.setupVault(this.instance.getServer().getPluginManager());
        }

        instance.i18n.updateLocale(getLocale());
        AdvancedKits.log(ChatColor.GREEN + "Configuration loaded successfully");
    }
}

package hu.tryharddood.advancedkits.Utils;

import hu.tryharddood.advancedkits.AdvancedKits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class UpdateManager
{
    public static String CURRENT_VERSION = "";
    public static String LATEST_VERSION = "";
    public static String DOWNLOAD_LINK = "http://tryharddood.com/advancedkits/latest/AdvancedKitsReloaded_v3.jar";

    public static void check()
    {
        AdvancedKits plugin = AdvancedKits.getInstance();
        CURRENT_VERSION = plugin.getDescription().getVersion();
        try
        {
            URL url = new URL("http://tryharddood.com/advancedkits/version.txt");
            BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));
            LATEST_VERSION = input.readLine();
            input.close();
        } catch (Exception e)
        {
            LATEST_VERSION = CURRENT_VERSION;
        }
    }

    public static boolean isUpdated()
    {
        int currentVersion = 0;
        int latestVersion = 0;
        try
        {
            currentVersion = Integer.parseInt(CURRENT_VERSION.replaceAll("\\.", ""));
            latestVersion = Integer.parseInt(LATEST_VERSION.replaceAll("\\.", ""));
        } catch (Exception ignored)
        {
        }
        return currentVersion < latestVersion;
    }
}

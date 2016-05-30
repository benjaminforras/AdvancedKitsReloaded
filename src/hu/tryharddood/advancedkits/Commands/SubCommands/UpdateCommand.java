package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class UpdateCommand extends Subcommand
{
    @Override
    public String getPermission()
    {
        return Variables.KITADMIN_PERMISSION;
    }

    @Override
    public String getUsage()
    {
        return "/kit update";
    }

    @Override
    public String getDescription()
    {
        return "Downloads the latest version of this plugin.";
    }

    @Override
    public int getArgs()
    {
        return 1;
    }

    @Override
    public boolean playerOnly()
    {
        return false;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        AdvancedKits plugin = AdvancedKits.getInstance();
        try
        {
            URL download = new URL("http://tryharddood.szervere.eu/docs/advancedkitsreloaded/latest/AdvancedKitsReloaded.jar");

            BufferedInputStream bufferedInputStream = null;
            FileOutputStream    fileOutputStream    = null;
            try
            {
                AdvancedKits.log(ChatColor.GOLD + "Trying to download from: " + "http://tryharddood.szervere.eu/docs/advancedkitsreloaded/latest/AdvancedKitsReloaded.jar");
                HttpURLConnection httpURLConnection = (HttpURLConnection) download.openConnection();
                httpURLConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

                bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                fileOutputStream = new FileOutputStream("plugins" + File.separator + plugin.getDescription().getName() + ".jar");

                long downloadedFileSize = 0;
                long completeFileSize   = httpURLConnection.getContentLength();

                final byte data[] = new byte[1024];
                int        count;
                int        currentProgress;
                while ((count = bufferedInputStream.read(data, 0, 1024)) != -1)
                {
                    downloadedFileSize += count;

                    currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100d);
                    if(((int) (currentProgress * 0x55555556L >> 30) & 45) == 0)
                        AdvancedKits.log(ChatColor.GREEN + "Downloaded: " + currentProgress + "%");

                    fileOutputStream.write(data, 0, count);
                }
            }
            finally
            {
                if (bufferedInputStream != null)
                {
                    bufferedInputStream.close();
                }
                if (fileOutputStream != null)
                {
                    fileOutputStream.close();
                }
            }
            AdvancedKits.log(ChatColor.GREEN + "Succesfully downloaded file: " + plugin.getDescription().getName() + ".jar");
            AdvancedKits.log(ChatColor.GREEN + "Please restart your server!");
        }
        catch (IOException e)
        {
            AdvancedKits.log(ChatColor.RED + "Error! Couldn't download the update.");
            AdvancedKits.log(ChatColor.RED + "Please send this to the author of this plugin:");
            AdvancedKits.log(" -- StackTrace --");
            e.printStackTrace();
            System.out.println(" -- End of StackTrace --");
        }
    }
}

package hu.tryharddood.advancedkits.Utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*****************************************************
 *
 * Created by inventivetalent modified by TryHardDood
 * https://bitbucket.org/inventivetalent/reflectionhelper/src/ae0347f404aae69e9881e6f448b0759008f1c0a1/src/main/java/org/inventivetalent/reflection/minecraft/Minecraft.java
 *
 ****************************************************/

public class Minecraft {
	private static final JavaPlugin plugin                  = JavaPlugin.getProvidingPlugin(Minecraft.class);
	private static final Pattern    NUMERIC_VERSION_PATTERN = Pattern.compile("v([0-9])_([0-9])_R([0-9])");

	public enum Version {
		UNKNOWN(-1)
				{
					@Override
					public boolean matchesPackageName(String packageName) {
						return false;
					}
				},

		v1_7_R1(10701),
		v1_7_R2(10702),
		v1_7_R3(10703),
		v1_7_R4(10704),

		v1_8_R1(10801),
		v1_8_R2(10802),
		v1_8_R3(10803),

		v1_8_R4(10804),

		v1_9_R1(10901),
		v1_9_R2(10902),

		v1_10_R1(101001),
		v1_11_R1(101101);

		private int version;

		Version(int version) {
			this.version = version;
		}

		public static Version getVersion() {
			String name           = Bukkit.getServer().getClass().getPackage().getName();
			String versionPackage = name.substring(name.lastIndexOf('.') + 1) + ".";
			for (Version version : values())
			{
				if (version.matchesPackageName(versionPackage))
				{
					return version;
				}
			}

			System.err.println("[ " + plugin.getDescription().getName() + "] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");
			System.out.println("[ " + plugin.getDescription().getName() + "] Generating dynamic constant...");

			Matcher matcher = NUMERIC_VERSION_PATTERN.matcher(versionPackage);
			while (matcher.find())
			{
				if (matcher.groupCount() < 3)
				{
					continue;
				}
				return Enum.valueOf(Version.class, versionPackage.substring(0, versionPackage.length() - 1));
			}

			return UNKNOWN;
		}

		/**
		 * @return the version-number
		 */
		public int version() {
			return version;
		}

		/**
		 * @param version the version to check
		 * @return <code>true</code> if this version is older than the specified version
		 */
		public boolean olderThan(Version version) {
			return version() < version.version();
		}

		/**
		 * @param version the version to check
		 * @return <code>true</code> if this version is newer than the specified version
		 */
		public boolean newerThan(Version version) {
			return version() >= version.version();
		}

		/**
		 * @param oldVersion The older version to check
		 * @param newVersion The newer version to check
		 * @return <code>true</code> if this version is newer than the oldVersion and older that the newVersion
		 */
		public boolean inRange(Version oldVersion, Version newVersion) {
			return newerThan(oldVersion) && olderThan(newVersion);
		}

		public boolean matchesPackageName(String packageName) {
			return packageName.toLowerCase().contains(name().toLowerCase());
		}

		@Override
		public String toString() {
			return name() + " (" + version() + ")";
		}
	}
}
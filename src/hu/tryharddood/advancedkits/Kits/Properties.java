package hu.tryharddood.advancedkits.Kits;

/**
 * Class:
 *
 * @author TryHardDood
 */
public enum Properties
{
    LASTUSE,
    UNLOCKED,
    USES,
    FIRSTJOIN;

    static
    {
        int ids = 0;

        for (Properties prop : values())
        {
            prop.id = (ids++);
        }
    }

    private int id;

    public String getName()
    {
        if (this == LASTUSE) return "lastuse";
        if (this == UNLOCKED) return "unlocked";
        if (this == USES) return "uses";
        if (this == FIRSTJOIN) return "firstjoin";
        return "Unknown";
    }

    public String toString()
    {
        return super.toString().toLowerCase();
    }

    public Integer getId()
    {
        return this.id;
    }
}

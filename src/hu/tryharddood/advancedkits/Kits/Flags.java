package hu.tryharddood.advancedkits.Kits;

/**
 * Class:
 *
 * @author TryHardDood
 */
public enum Flags
{
    VISIBLE,
    PERMONLY,
    CLEARINV,
    FIRSTJOIN,
    PERMISSION,
    ICON,
    COST,
    USES,
    DELAY;

    static
    {
        int ids = 0;

        for (Flags flag : values())
        {
            flag.id = (ids++);
        }
    }
    private int id;

    public String getName()
    {
        if (this == VISIBLE) return "visible";
        if (this == PERMONLY) return "permonly";
        if (this == USES) return "uses";
        if (this == FIRSTJOIN) return "firstjoin";
        if (this == CLEARINV) return "clearinv";
        if (this == PERMISSION) return "permission";
        if (this == ICON) return "icon";
        if (this == COST) return "cost";
        if (this == DELAY) return "delay";
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

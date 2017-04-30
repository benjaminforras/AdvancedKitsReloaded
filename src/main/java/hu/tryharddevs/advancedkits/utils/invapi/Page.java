package hu.tryharddevs.advancedkits.utils.invapi;

public class Page
{
	private String pageName;
	private String pageTitle;

	public Page(String pageName)
	{
		this(pageName, pageName);
	}

	public Page(String pageName, String pageDisplayTitle)
	{
		this.pageTitle = pageDisplayTitle;
		this.pageName = pageName;
	}

	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Page other = (Page) obj;
		return getPageName().equals(other.getPageName());
	}

	public String getPageDisplayTitle()
	{
		return pageTitle;
	}

	public String getPageName()
	{
		return pageName;
	}

	@Override
	public int hashCode()
	{
		final int prime  = 31;
		int       result = 1;
		result = prime * result + getPageName().hashCode();
		return result;
	}

	public void setDisplayTitle(String newTitle)
	{
		this.pageTitle = newTitle;
	}

	@Override
	public String toString()
	{
		return "Page[Name=" + getPageName() + ", Title=" + getPageDisplayTitle() + "]";
	}

}

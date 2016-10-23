package hu.tryharddood.advancedkits.Kits;

import java.util.HashMap;

/*****************************************************
 *              Created by TryHardDood on 2016. 10. 22..
 ****************************************************/
public class Session {

	public static HashMap<String, Session> sessions = new HashMap<>();

	private Kit    kit;
	private Action action;

	public Session() {}

	public static Session getSession(String player) {
		if (sessions.containsKey(player))
		{
			return sessions.get(player);
		}

		return sessions.put(player, new Session());
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Kit getKit() {
		return kit;
	}

	public void setKit(Kit kit) {
		this.kit = kit;
	}

	public static enum Action {
		EDIT,
		CREATE,
		VIEW;
	}
}

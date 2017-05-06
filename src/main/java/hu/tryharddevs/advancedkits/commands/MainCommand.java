package hu.tryharddevs.advancedkits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import hu.tryharddevs.advancedkits.AdvancedKitsMain;

@SuppressWarnings("ConstantConditions")
@CommandAlias("kit|akit|advancedkits|kits|akits")
public class MainCommand extends BaseCommand {
	private AdvancedKitsMain instance;

	public MainCommand(AdvancedKitsMain instance) {
		this.instance = instance;
	}

}
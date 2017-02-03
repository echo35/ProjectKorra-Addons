package ch.carbogen.korra.lavasurge;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.chiblocking.ChiCombo;
import com.projectkorra.projectkorra.chiblocking.Paralyze;
import com.projectkorra.projectkorra.waterbending.Bloodbending;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Set;

/**
 * Created by Carbogen on 10/17/16.
 */
public class Trigger implements Listener {

	@EventHandler
	public void onPlayerSwing(PlayerAnimationEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer != null) {
				if (Suffocate.isBreathbent(player)) {
					event.setCancelled(true);
				} else if (!Bloodbending.isBloodbent(player) && !Paralyze.isParalyzed(player) && !ChiCombo.isParalyzed(player)) {
					if (bPlayer.isChiBlocked()) {
						event.setCancelled(true);
					} else if (GeneralMethods.isInteractable(player.getTargetBlock((Set) null, 5))) {
						event.setCancelled(true);
					} else {
						String abil = bPlayer.getBoundAbilityName();
						CoreAbility coreAbil = bPlayer.getBoundAbility();
						if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH)) {
							if (GeneralMethods.isWeapon(player.getItemInHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
								return;
							}

							if (abil.equalsIgnoreCase("lavasurge")) {
								LavaSurge.mudSurge(player);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (!event.isCancelled() && bPlayer != null) {
			String abilName = bPlayer.getBoundAbilityName();
			if (!Paralyze.isParalyzed(player) && !ChiCombo.isParalyzed(player) && !Bloodbending.isBloodbent(player)) {
				CoreAbility coreAbil = bPlayer.getBoundAbility();
				String abil = bPlayer.getBoundAbilityName();
				if (coreAbil != null) {
					if (bPlayer.isChiBlocked()) {
						event.setCancelled(true);
					} else {
						if (event.isSneaking() && bPlayer.canBendIgnoreCooldowns(coreAbil)) {
							if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH)) {
								if (GeneralMethods.isWeapon(player.getItemInHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
									return;
								}
								if (abil.equalsIgnoreCase("lavasurge")) {
									new LavaSurge(player);
								}
							}
						}
					}
				}
			}
		}
	}
}

package ru.tehkode.permissions.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEvent;
import ru.tehkode.permissions.events.PermissionSystemEvent;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
* Listener for events from the NetEvents plugin
*/
public class RemoteEventListener implements Listener {
	private final PermissionManager manager;

	public RemoteEventListener(PermissionManager manager) {
		this.manager = manager;
	}

	public boolean isLocal(PermissionEvent event) {
		return true;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityEvent(PermissionEntityEvent event) {
		if (isLocal(event)) {
			return;
		}
		final boolean reloadEntity, reloadAll;

		switch (event.getAction()) {
			case DEFAULTGROUP_CHANGED:
			case RANK_CHANGED:
			case INHERITANCE_CHANGED:
				reloadAll = true;
				reloadEntity = false;
				break;
			case SAVED:
			case TIMEDPERMISSION_EXPIRED:
				return;
			default:
				reloadEntity = true;
				reloadAll = false;
				break;
		}

		try {
		if (reloadEntity) {
				if (manager.getBackend() != null) {
					manager.getBackend().reload();
				}
			switch (event.getType()) {
				case USER:
					manager.resetUser(event.getEntityIdentifier());
					break;
				case GROUP:
					PermissionGroup group = manager.resetGroup(event.getEntityIdentifier());
					if (group != null) {
						for (PermissionUser user : group.getActiveUsers(true)) {
							manager.resetUser(user.getIdentifier());
						}
					}

					break;
			}
		} else if (reloadAll) {
			manager.reset();
		}
		} catch (PermissionBackendException e) {
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSystemEvent(PermissionSystemEvent event) {
		if (isLocal(event)) {
			return;
		}

		switch (event.getAction()) {
			case BACKEND_CHANGED:
			case DEBUGMODE_TOGGLE:
			case REINJECT_PERMISSIBLES:
				return;
		}

		try {
			manager.reset(false);
		} catch (PermissionBackendException e) {
			e.printStackTrace();
		}
	}
}

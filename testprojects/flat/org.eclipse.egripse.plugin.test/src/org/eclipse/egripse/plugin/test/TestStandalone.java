package org.eclipse.egripse.plugin.test;

import org.junit.Assert;
import org.junit.Test;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.egripse.plugin.Activator;

import java.lang.IllegalStateException;

/**
 * Standalone tests, checks if we are running in an ui environment and fails,
 * if we do
 * @author OleyMa
 *
 */
public class TestStandalone {

	@Test
	public void checkIfActivatorWasStarted () {
		Assert.assertNull(Activator.getDefault());
	}

	@Test(expected= IllegalStateException.class)
	public void checkIfWorkspaceIsOpened () {
		Assert.assertNotNull (ResourcesPlugin.getWorkspace().getRoot().getProject("hallo"));

	}

}

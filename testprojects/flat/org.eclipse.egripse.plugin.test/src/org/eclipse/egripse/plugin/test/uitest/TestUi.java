package org.eclipse.egripse.plugin.test.uitest;

import org.junit.Assert;
import org.junit.Test;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.egripse.plugin.Activator;

/**
 * ui tests, check if we run in a ui test environment and fails,
 * if we do not
 * @author OleyMa
 *
 */
public class TestUi {

	@Test
	public void checkIfActivatorWasStarted () {
		Assert.assertNotNull (Activator.getDefault());
	}

	@Test
	public void checkIfWorkspaceIsOpened () {
		Assert.assertNotNull (ResourcesPlugin.getWorkspace().getRoot().getProject("hallo"));

	}

}

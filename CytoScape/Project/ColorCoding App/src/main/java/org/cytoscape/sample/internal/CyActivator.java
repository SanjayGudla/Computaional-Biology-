package org.cytoscape.sample.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

    //getting the current bundle context and adding ColorCodingApp as an extension
	public void start(BundleContext bc) {
		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		ColorCodingApp ColorCodingAction = new ColorCodingApp(cyApplicationManager);
		registerService(bc,ColorCodingAction,CyAction.class,new Properties());
	}
}


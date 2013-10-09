/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.upgrade.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.vmmgr.ControllerUtils;
import org.opennms.upgrade.api.OnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeComparator;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * The Class Upgrade.
 * <p>This is the helper class that is going to be instantiated from outside OpenNMS to perform the upgrade operations.</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class Upgrade {

    /** The class scope. */
    private String classScope = "org/opennms/upgrade"; // To avoid issues with OSGi and other classes.

    /** The upgrade status object. */
    private UpgradeStatus upgradeStatus;

    /**
     * Gets the upgrade status.
     *
     * @return the upgrade status
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public UpgradeStatus getUpgradeStatus() throws OnmsUpgradeException {
        if (upgradeStatus == null) {
            upgradeStatus = new UpgradeStatus();
        }
        return upgradeStatus;
    }

    /**
     * Sets the upgrade status.
     *
     * @param upgradeStatus the new upgrade status
     */
    public void setUpgradeStatus(UpgradeStatus upgradeStatus) {
        this.upgradeStatus = upgradeStatus;
    }

    /**
     * Gets the class scope.
     *
     * @return the class scope
     */
    public String getClassScope() {
        return classScope;
    }

    /**
     * Sets the class scope.
     *
     * @param classScope the new class scope
     */
    public void setClassScope(String classScope) {
        this.classScope = classScope;
    }

    /**
     * Checks if is OpenNMS running.
     *
     * @return true, if is OpenNMS running
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected boolean isOpennmsRunning() {
        try {
            return ControllerUtils.getController().status() == 0;
        } catch (Exception e) {
            log("  Warning: can't retrieve OpeNNMS status (assuming it is not running).");
            return false;
        }
    }

    /**
     * Was executed.
     *
     * @param upg the upgrade implementation class
     * @return true, if successful
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected boolean wasExecuted(OnmsUpgrade upg) throws OnmsUpgradeException {
        return getUpgradeStatus().wasExecuted(upg);
    }

    /**
     * Execute upgrade.
     *
     * @param upg the upgrade implementation class
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected void executeUpgrade(OnmsUpgrade upg) {
        try {
            log("- Running pre-execution phase\n");
            upg.preExecute();
        } catch (OnmsUpgradeException e) {
            log("  Ignoring: %s\n", e.getMessage());
            return;
        }
        try {
            log("- Running execution phase\n");
            upg.execute();
            log("- Saving the execution state\n");
            markAsExecuted(upg);
        } catch (OnmsUpgradeException executeException) {
            log("  Warning: can't perform the upgrade operation because: %s\n", executeException.getMessage());
            try {
                log("- Executing rollback phase\n");
                upg.rollback();
            } catch (OnmsUpgradeException rollbackException) {
                log("  Warning: can't rollback the upgrade because: %s\n", rollbackException.getMessage());
                rollbackException.printStackTrace();
            }
        }
        try {
            log("- Running post-execution phase\n");
            upg.postExecute();
        } catch (OnmsUpgradeException e) {
            log("  Warning: can't run the post-execute phase because: %s\n", e.getMessage());
            return;
        }
    }

    /**
     * Mark as executed.
     *
     * @param upg the upgrade implementation class
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected void markAsExecuted(OnmsUpgrade upg) throws OnmsUpgradeException {
        getUpgradeStatus().markAsExecuted(upg);
    }

    /**
     * Log.
     *
     * @param msgFormat the message format
     * @param args the message's arguments
     */
    protected void log(String msgFormat, Object... args) {
        System.out.printf(msgFormat, args);
    }

    /**
     * Gets the upgrade objects.
     *
     * @return the upgrade objects
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected List<OnmsUpgrade> getUpgradeObjects() throws OnmsUpgradeException {
        List<OnmsUpgrade> upgrades = new ArrayList<OnmsUpgrade>();
        try {
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
            provider.addIncludeFilter(new AssignableTypeFilter(OnmsUpgrade.class));
            Set<BeanDefinition> components = provider.findCandidateComponents(getClassScope());
            for (BeanDefinition component : components) {
                Class<?> cls = Class.forName(component.getBeanClassName());
                OnmsUpgrade upgrade = (OnmsUpgrade) cls.newInstance();
                upgrades.add(upgrade);
            }
            Collections.sort(upgrades, new OnmsUpgradeComparator());
        } catch (Exception e) {
            throw new OnmsUpgradeException("  Can't find the upgrade classes because: " + e.getMessage(), e);
        }
        return upgrades;
    }

    /**
     * Execute.
     * <p>Perform the upgrade operations.</p>
     * 
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public void execute() throws OnmsUpgradeException {
        log("OpenNMS is currently %s\n", (isOpennmsRunning() ? "running" : "stopped"));
        List<OnmsUpgrade> upgradeObjects = getUpgradeObjects();
        for (OnmsUpgrade upg : upgradeObjects) {
            log("Processing %s : %s\n", upg.getId(), upg.getDescription());
            if (wasExecuted(upg)) {
                log("  Task %s was already executed at %s\n", upg.getId(), getUpgradeStatus().getLastExecutionTime(upg));
            } else {
                if (isOpennmsRunning()) {
                    if (upg.requiresOnmsRunning()) {
                        executeUpgrade(upg);
                    } else {
                        log("  Class %s requires OpenNMS to be stopped but it is running\n", upg.getId());
                    }
                } else {
                    if (upg.requiresOnmsRunning()) {
                        log("  Class %s requires OpenNMS to be running but it is stopped\n", upg.getId());
                    } else {
                        executeUpgrade(upg);
                    }
                }
            }
        }
    }

    /**
     * The main method.
     * <p>This is the class that must be called externally to perform the upgrade.</p>
     * 
     * TODO: be able to pass the class scope (package filter)
     * TODO: be able to pass the execution status file
     * 
     * @param args the arguments
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public static void main(String args[]) throws OnmsUpgradeException {
        long start = System.currentTimeMillis();
        Upgrade upgrade = new Upgrade();
        upgrade.execute();
        upgrade.log("Finished in %s milliseconds\n", (System.currentTimeMillis() - start));
    }
}
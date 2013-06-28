package org.opennms.features.vaadin.dashboard.ui.dashboard;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.Wallboard;
import org.slf4j.LoggerFactory;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class DashboardView extends DashboardLayout implements View {

    public DashboardView() {
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        LoggerFactory.getLogger(DashboardView.class).warn("enter(): " + event.getParameters());
        if (event.getParameters() != null) {
            Wallboard wallboard = WallboardProvider.getInstance().getWallboard(event.getParameters());
            if (wallboard != null) {
                setDashletSpecs(wallboard.getDashletSpecs());
            }
        }
    }
}
package org.delcom.ifs25025springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class LoadingIndicatorInitListener implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            var configuration = uiEvent.getUI().getLoadingIndicatorConfiguration();
            configuration.setFirstDelay(0);
            configuration.setSecondDelay(400);
            configuration.setThirdDelay(800);
        });
    }
}

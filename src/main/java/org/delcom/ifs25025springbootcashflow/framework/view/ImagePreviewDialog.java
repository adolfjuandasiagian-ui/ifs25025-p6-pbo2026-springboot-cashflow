package org.delcom.ifs25025springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;

public class ImagePreviewDialog extends Dialog {
    private final Image image = new Image();

    public ImagePreviewDialog(String title, String imageUrl) {
        setHeaderTitle(title);
        setWidth("700px");

        image.setSrc(imageUrl);
        image.setWidth("100%");
        image.getStyle().set("cursor", "zoom-in");

        Button close = new Button("Tutup", event -> close());
        close.getElement().getStyle().set("margin-left", "auto");
        getFooter().add(close);
        add(image);
    }

    public Image getImage() {
        return image;
    }
}

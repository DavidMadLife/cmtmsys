package org.chemtrovina.cmtmsys.model;

import javafx.scene.Parent;

/*public class FxmlPage {
    private Parent view;
    private Object controller;

    public FxmlPage(Parent view, Object controller) {
        this.view = view;
        this.controller = controller;
    }

    public Parent getView() {
        return view;
    }

    public Object getController() {
        return controller;
    }
}*/

public class FxmlPage {
    private final Parent root;
    private final Object controller;

    public FxmlPage(Parent root, Object controller) {
        this.root = root;
        this.controller = controller;
    }
    public Parent getRoot() { return root; }
    public Object getController() { return controller; }
}


module org.chemtrovina.cmtmsys {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires javafx.swing;
    requires spring.jdbc;
    requires org.apache.poi.ooxml;
    requires spring.tx;
    requires java.desktop;
    requires spring.context;
    requires spring.beans;
    requires javafx.media;
    requires static lombok;
    requires com.sun.jna.platform;
    requires com.sun.jna;

    opens org.chemtrovina.cmtmsys to javafx.fxml;
    opens org.chemtrovina.cmtmsys.controller; // ✅ mở full package controller
    opens org.chemtrovina.cmtmsys.helper;     // ✅ để Spring inject helper
    opens org.chemtrovina.cmtmsys.model to javafx.base, spring.core, spring.beans;

    opens org.chemtrovina.cmtmsys.dto to javafx.base, spring.beans;

    exports org.chemtrovina.cmtmsys;
    exports org.chemtrovina.cmtmsys.controller;
    exports org.chemtrovina.cmtmsys.config;
    exports org.chemtrovina.cmtmsys.repository.base;
    exports org.chemtrovina.cmtmsys.repository.Impl;
    exports org.chemtrovina.cmtmsys.service.base;
    exports org.chemtrovina.cmtmsys.service.Impl;
    exports org.chemtrovina.cmtmsys.utils;
    exports org.chemtrovina.cmtmsys.helper;
    exports org.chemtrovina.cmtmsys.controller.workorder;
    opens org.chemtrovina.cmtmsys.controller.workorder;
    exports org.chemtrovina.cmtmsys.controller.inventoryTransfer;
    opens org.chemtrovina.cmtmsys.controller.inventoryTransfer;
    exports org.chemtrovina.cmtmsys.controller.product;
    opens org.chemtrovina.cmtmsys.controller.product;

    opens org.chemtrovina.cmtmsys.controller.productionPlan to spring.core, spring.beans, spring.context;

}

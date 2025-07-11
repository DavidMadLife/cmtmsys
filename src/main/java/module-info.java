module org.chemtrovina.cmtmsys {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;

    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires spring.jdbc;
    requires org.apache.poi.ooxml;
    requires spring.tx;
    requires java.desktop;
    requires spring.context;
    requires spring.beans;
    requires javafx.media;

    opens org.chemtrovina.cmtmsys to javafx.fxml;
    exports org.chemtrovina.cmtmsys;
    exports org.chemtrovina.cmtmsys.controller;
    opens org.chemtrovina.cmtmsys.controller to javafx.fxml;

    //Inject
    opens org.chemtrovina.cmtmsys.model to javafx.base;
    exports org.chemtrovina.cmtmsys.config;
    exports org.chemtrovina.cmtmsys.repository.base;
    exports org.chemtrovina.cmtmsys.repository.Impl;
    exports org.chemtrovina.cmtmsys.service.base;
    exports org.chemtrovina.cmtmsys.service.Impl;
    opens org.chemtrovina.cmtmsys.dto to javafx.base;
    exports org.chemtrovina.cmtmsys.utils;

}
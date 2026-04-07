module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires org.apache.commons.codec;

    opens org.example to javafx.fxml;
    exports org.example;
}
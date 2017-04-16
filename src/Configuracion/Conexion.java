package Configuracion;

/**
 *  14/01/2017
 * @author Juan Ortega
 * Ing de Sistemas -Informatica
 */
import com.mysql.jdbc.Connection;
import static java.lang.Class.forName;
import static java.sql.DriverManager.getConnection;

import java.sql.SQLException;
import static javax.swing.JOptionPane.showMessageDialog;

public class Conexion {

    public String NombreDB = "portero";
    public String url = "jdbc:mysql://localhost/" + NombreDB;
    public String user = "root";
    public String pass = "123456";

    public Connection conectar() {

        Connection link = null;
        try {

            forName("org.gjt.mm.mysql.Driver");

            link = (Connection) getConnection(this.url, this.user, this.pass);

        } catch (ClassNotFoundException | SQLException ex) {

            showMessageDialog(null, ex);

        }

        return link;

    }

}

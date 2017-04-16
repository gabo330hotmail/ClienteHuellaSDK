
package clientehuellassdk;

import Controller.HuellasController;
import com.digitalpersona.onetouch.huellasclientes.UserInterface;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan
 */
public class ClienteHuellasSDK {

    private final static String DEFAULT_UI_FACTORY = "com.digitalpersona.onetouch.huellasclientes.ConsoleUserInterfaceFactory";

    public static void main(String[] args) {
     try {
            HuellasController hu=new HuellasController();
            String uiFactoryName =  DEFAULT_UI_FACTORY ; 
            System.out.println("Utilitarios inicializados");
     
            hu.cargarPersonal();
            hu.cargarHuellas();
            System.out.println("Cargadas huellas: "+hu.cantidadHuellas());
            
            UserInterface.Factory uiFactory = null;
            try {
                uiFactory = (UserInterface.Factory)Class.forName(uiFactoryName).newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ClienteHuellasSDK.class.getName()).log(Level.SEVERE, null, ex);
              
            }
            @SuppressWarnings("null")
            UserInterface userInterface = uiFactory.createUI(hu);

            ExecutorService exec = Executors.newCachedThreadPool();
            exec.execute(userInterface);

            exec.shutdown();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClienteHuellasSDK.class.getName()).log(Level.SEVERE, null, ex);
           
        }
    };   
        
        
        
    }
    


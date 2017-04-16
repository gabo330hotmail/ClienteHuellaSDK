package Controller;

import Configuracion.Conexion;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.mysql.jdbc.Connection;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


/**
 *
 * @author Juan
 */
public class HuellasController {

    public Connection co;
    public Statement com;
//    public ResultSet rs;
    private ResultSet rsIdentificar;
    private ResultSet rsPersonal;
    private Map<DPFPTemplate, Long> mHuellas;
    private List<Object[]> mHuellasList;
//    private Map<Integer, Object[]> mHuellasList;
    private Map<Long, Map<String, Object>> mPersonal;
    private final ServerSocket ss = null;
    private final Socket clientSocket = null;
    private Socket sock = null;
   

    /**
     * Metodo para la Conexion a la BD
     *
     * @throws SQLException
     */
    public void conexion() throws SQLException {
        Conexion mysql;
        mysql = new Conexion();
        co = mysql.conectar();
        com = co.createStatement();

    }

    /**
     * Metodo para desconectar o cerrar la BD
     *
     * @throws SQLException
     */
    public void cerrar() throws SQLException {
        co.close();
        com.close();
    }

    public void cargarHuellas() {
        try {
           /* String sql = ("Select * "
                        + " from pow_huellas "
                        + " inner join pow_persona on (huelpers=persindx)"
                        + " where huelestd='A' AND persesta='A'  ");*/
            String sql=("Select hue.HUELINDX,hue.HUELESTD,hue.HUELPERS,hue.HUELHUEL,hue.HUELFING,\n" +
"                               case when hue.HUELFING ='id' then\n" +
"                               1\n" +
"                               when hue.HUELFING='ii'then\n" +
"                               2\n" +
"                               when hue.HUELFING='cd'then\n" +
"                               3\n" +
"                                when hue.HUELFING='ci'then\n" +
"                                4\n" +
"                                when hue.HUELFING='pd'then\n" +
"                                 5\n" +
"                                when hue.HUELFING='pi'then\n" +
"                                6\n" +
"                                when hue.HUELFING='md'then\n" +
"                                7\n" +
"                                when hue.HUELFING='mi'then\n" +
"                                8\n" +
"                                when hue.HUELFING='ad'then\n" +
"                                9\n" +
"                                when hue.HUELFING='ai'then\n" +
"                                10\n" +
"                               end Valor, \n" +
"                               pers.PERSINDX,pers.PERSDOCU,pers.PERSNOMB,pers.PERSDEPE,pers.PERSESTA \n" +
"                          from pow_huellas hue\n" +
"                          inner join pow_persona pers on (huelpers=persindx)\n" +
"                          where huelestd='A' AND persesta='A' order by Valor asc");
            System.out.println("Inicio de cargar huellas");
            conexion();
            this.rsIdentificar = com.executeQuery(sql);
            this.mHuellas = new HashMap<>();
            this.mHuellasList=new ArrayList<>();
//            this.mHuellasList = new HashMap<>();
            Long cedula;
            while (rsIdentificar.next()) {
                //Lee la plantilla de la base de datos
                @SuppressWarnings("MismatchedReadAndWriteOfArray")
                byte[] templateBuffer = rsIdentificar.getBytes("HUELHUEL");
                InputStream templateString = rsIdentificar.getBinaryStream("HUELHUEL");

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                try {
                    while ((nRead = templateString.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();

                } catch (IOException e) {
                }

                byte[] templateBuffer2 = buffer.toByteArray();

                cedula = rsIdentificar.getLong("huelpers");
                DPFPTemplate templateTmp = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer2);

                this.mHuellas.put(templateTmp, cedula);
                @SuppressWarnings("MismatchedReadAndWriteOfArray")
                Object[] dato = new Object[2];
                dato[0] = templateTmp;
                dato[1] = cedula;

                this.mHuellasList.add(dato);
//                 this.mHuellasList.put(jj++, dato);
            }
           
//             System.out.println("Total de Huellas "+this.mHuellasList.size());
        } catch (SQLException ex) {
            Logger.getLogger(HuellasController.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }

    ;
    
    
    public void cargarPersonal() {
        try {
            String sql = ("Select persindx,persnomb,persdepe,fotocode,fotoimag,persdocu"
                        + " from pow_persona"
                        + " left join pow_foto on (persindx=fotopers) "
                        + " where persesta='A' ");
            conexion();
            this.rsPersonal = com.executeQuery(sql);

            this.mPersonal = new HashMap<>();
            while (rsPersonal.next()) {
                  //Lee la plantilla de la base de datos

                Long id =            rsPersonal.getLong("persindx");
                String nombre =      rsPersonal.getString("persnomb");
                String dependencia = rsPersonal.getString("persdepe");
                String code =        rsPersonal.getString("fotocode");
                String imag =        rsPersonal.getString("fotoimag");
                String cedula =      rsPersonal.getString("persdocu");

                String foto = code + "," + imag;

                Map<String, Object> persona = new HashMap<>();
                persona.put("nombre", nombre);
                persona.put("dependencia", dependencia);
                persona.put("foto", foto);
                persona.put("cedula", cedula);

                this.mPersonal.put(id, persona);
          }
            System.out.println("Total Personas " + this.mPersonal.size());
        } catch (SQLException ex) {
            Logger.getLogger(HuellasController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ;
   
      @SuppressWarnings({"ImplicitArrayToString", "empty-statement"})
    public Long buscarHuella(DPFPFeatureSet huellaVer, DPFPVerification Verificador, DPFPSample sample) {

        Long cedulaBus = (long) 0;
        if (huellaVer != null && Verificador != null) {
            try {
                
               
                String persona = "NN";
                boolean encontrado = false;
                for(Object[] valor:mHuellasList){
                   
                    Object[] data =valor;
                    DPFPTemplate key = (DPFPTemplate) data[0];
                   
                  if( Verificador.verify(huellaVer, key).isVerified()){
                      persona=data[1].toString();
                       System.out.println("Id Hallado: "+ persona);
                       encontrado = true;
                         break;
                  };

                }

                if (encontrado == false) {
                    System.out.println("No existe Huella: " + persona);
                }

                Image imagenHuella = CrearImagenHuella(sample);
                responderDatos(persona, imagenHuella);

                   //Si encuentra correspondencia dibuja el mapa
                //e indica el nombre de la persona que coincidió.
            } catch (IOException ex) {
                System.out.println("Error en Conexion:  " + ex);

            }

        }

//        System.err.println("cedulaBus-- " + cedulaBus);
        return cedulaBus;
    }

    ;
       public void responderDatos(String persona, Image imagenHuella) throws IOException {
        System.out.println("Persona en ResponderDatos: " + persona);
        
       
        Map<String, Object> personaMap;
        if(!persona.equals("NN")){
         personaMap = this.mPersonal.get(Long.parseLong(persona));
        }else{
           personaMap=null; 
        }

            byte[] imageString = imageToString(imagenHuella);

            Object[] objetos = new Object[3];
            objetos[0] = persona;
            objetos[1] = imageString;
            objetos[2] = personaMap;
           
           this.sock = new Socket("localhost", 2200);
           ObjectOutputStream outputStream = new ObjectOutputStream(this.sock.getOutputStream());
           outputStream.writeObject(objetos);
           sock.close();
    }

    public byte[] imageToString(Image image) {

        MediaTracker tracker = new MediaTracker(new Container());
        tracker.addImage(image, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
        }
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), 1);
        Graphics gc = bufferedImage.createGraphics();
        gc.drawImage(image, 0, 0, null);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(bufferedImage, "jpeg", bos);
        } catch (IOException ex) {

        }

        return bos.toByteArray();
    };

    public Image CrearImagenHuella(DPFPSample sample) {
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    };

    public long cantidadHuellas() {
         return mHuellas.size();

    };
    

}

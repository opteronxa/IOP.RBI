package iop;

import process.C_Logger;
import process.C_Session;
import files.S_Configuracion;

/**
 *
 * @author ByXanum
 */
public class Iop {

    public static String S_Version = "1.2";
    public static S_Configuracion CONF;
    public static boolean S_StopReq=false;
    public static C_Condicion PAUSA=new C_Condicion();

    // === Main loop ===
    public static void main(String[] args) {
        var LOG = C_Logger.getLogger(Iop.class);
        LOG.info("<<IOP START: ver. " + S_Version + ">>");
        try {
            CONF = S_Configuracion.getInstancia();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("Deteniendo IOP...");
                PAUSA.apagar();
            })); 
            boolean y_first=true;
            C_Session y_sess = new C_Session();
            do {
                LOG.info(">>Ejecutando Ciclo IOP-RBI");
                if (y_sess.login()) {
                    if (y_first) y_first=false;
                    LOG.info("<<Ciclo IOP-RBI Completado con EXITO!");
                } else {
                    if (y_first) break;
                    LOG.info("<<Ciclo IOP-RBI Completado con ERROR!");
                }   
                LOG.info("IOP entra en Hibernacion, Espere Proxima Iteracion en " + CONF.getFrecuencia() + " minutos, Gracias!");
                LOG.info("zzz, zzz, zzzzz...");
            } while (PAUSA.esperar(CONF.getFrecuencia())); 
        } catch (Exception ex) {
            System.getLogger(Iop.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }     
        LOG.info("<<IOP END...>>");
    }

}


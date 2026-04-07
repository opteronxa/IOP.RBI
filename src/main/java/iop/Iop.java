package iop;

import process.C_Logger;
import process.C_Session;
import files.S_Configuracion;

/**
 *
 * @author ByXanum
 */
public class Iop {

    public static String S_Version = "1.0";
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
                System.out.println("Deteniendo...");
                PAUSA.apagar();
            })); 
            do {
                LOG.info("** IOP RBI ** ");
                new C_Session().login();
            } while (PAUSA.esperar(CONF.getFrecuencia())); 
        } catch (Exception e) {
            LOG.error("Terminacion Abrupta de la IOP: " + e.getMessage());
            System.exit(0);
        }
        LOG.info("<<IOP END...>>");
    }

}


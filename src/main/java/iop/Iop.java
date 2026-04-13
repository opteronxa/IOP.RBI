package iop;

import process.C_Logger;
import process.C_Session;
import files.S_Configuracion;

/**
 *
 * @author ByXanum
 */
public class Iop {

    public static String S_Version = "1.1";
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
            do {
                LOG.info("** Iniciando Ciclo IOP RBI ** ");
                new C_Session().login();
            } while (PAUSA.esperar(CONF.getFrecuencia())); 
        } catch (Exception e) {
            LOG.error("Terminacion Abrupta de la IOP: " + e.getMessage());
        }
        LOG.info("<<IOP END...>>");
    }

}


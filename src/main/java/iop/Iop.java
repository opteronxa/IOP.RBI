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

    // === Main loop ===
    public static void main(String[] args) {
        var LOG = C_Logger.getLogger(Iop.class);
        LOG.info("<<IOP START: ver. " + S_Version + ">>");
        try {
            CONF = S_Configuracion.getInstancia();
            LOG.info("** IOP RBI ** ");
            new C_Session().login();
        } catch (Exception e) {
            LOG.error("Terminacion Abrupta de la IOP: " + e.getMessage());
            System.exit(0);
        }
        LOG.info("<<IOP END...>>");
        
/*        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Deteniendo...");
            stopRequested = true;
        }));
*/

/*
        while (!stopRequested) {
            iteration++;
            System.out.println("\n=== Iteración " + iteration + " ===");
            
            System.out.println("TAGs: " + tags);

            for (String t : tags) {
                if (stopRequested) break;
                // Aquí se llamaría processTag(t) con lógica similar
            }

            Thread.sleep(INTERVAL_MINUTES * 60 * 1000);
        }
*/
    }

}


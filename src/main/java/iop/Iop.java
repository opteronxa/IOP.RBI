package iop;

import process.C_Logger;
import process.C_Session;
import files.S_Configuracion;
import java.time.Instant;

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
        LOG.info("<<IOP START>> version " + S_Version);
        try {
            CONF = S_Configuracion.getInstancia();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("Atendiendo su peticion, Deteniendo IOP... por favor espere!");
                PAUSA.apagar();
            })); 
            boolean y_first=true;
            long y_time=0;
            C_Session y_sess = new C_Session();
            do {
                y_time=Instant.now().getEpochSecond();
                LOG.info("<<WAKE UP>> Ejecutando Ciclo IOP-RBI");
                if (y_sess.login()) {
                    if (y_first) y_first=false;
                    LOG.info("<<SUCCESS> Ciclo IOP-RBI Completado con EXITO!");
                } else {
                    if (y_first) {
                        LOG.info("<<ERROR>> Ciclo IOP-RBI no se pudo establecer el primer intento de conexion a Gateway!");
                        LOG.info("<<COLD STOP>> Siendo el primer ciclo debo Terminando el programa para que Verifique Condiciones Remotas!");
                        break;
                    }
                    LOG.info("<<WARNING> Ciclo IOP-RBI no Completado!>>");
                }  
                LOG.info("Tiempo transcurrido en esta Iteracion: " + (Instant.now().getEpochSecond() - y_time) + " segundos");
                LOG.info("IOP entra en Hibernacion, Espere Proxima Iteracion en " + CONF.getFrecuencia() + " minutos, Gracias!");
                LOG.info("<<SLEEP>> zzz, zzz, zzzzz...");
            } while (PAUSA.esperar(CONF.getFrecuencia())); 
        } catch (Exception ex) {
            System.getLogger(Iop.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }     
        LOG.info("<<IOP END>> Gracias... Bye!");
    }

}


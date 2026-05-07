package iop;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import process.C_Logger;

/**
 *
 * @author ByXanum
 */
public class C_Condicion {
    
    private final Logger LOG = C_Logger.getLogger(C_Condicion.class);
    private final Lock LOCK = new ReentrantLock();
    private final Condition CONDICION;
    private volatile boolean x_listo = true;
        
    public C_Condicion() {
        this.CONDICION = this.LOCK.newCondition();
    }

    public boolean esperar(int _frec) {
        this.x_listo=true;
        var y_ns=0;
        var y_nm=0;
        this.LOCK.lock();
        try {
            while (x_listo) {
                this.CONDICION.await(1, TimeUnit.SECONDS);
                if (++y_ns>=60) {
                    y_ns=0;
                    if (++y_nm >= _frec) break;
                }
            }
        }catch (InterruptedException e) {
            LOG.error("Problema con el await de esperar: " + e.getMessage());
        } finally {
            this.LOCK.unlock();
        }
        return this.x_listo;
    }

    public void apagar() {
        this.LOCK.lock();
        try {
            x_listo = false;
            this.CONDICION.signalAll();
        } finally {
            this.LOCK.unlock();
        }
    }
    
}

package process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author xanum
 */
public class C_Logger {
    
    public static Logger getLogger(Object obj) {
        return LoggerFactory.getLogger(obj.getClass());
    }

    public static Logger getLogger(Class<?> _class) {
        return LoggerFactory.getLogger(_class);
    }

}
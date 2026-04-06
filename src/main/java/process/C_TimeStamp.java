package process;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author ByXanum
 */
public class C_TimeStamp {    // === Utilidades de fecha ===
    
    public String formatLocalDateTime(long ms) {
        LocalDateTime dt = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String formatIsoDateTime(long ms) {
        return Instant.ofEpochMilli(ms).toString();
    }
    
}

package files;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author R.Cuevas
 */
public enum E_Parametros {

    IOP_NOMBRE("TSIGO_RBI"),
    IOP_SAMPLE("5"),
    API_CID("bMsmOTeAw/fj3Fng/ieoQWBmyeNvqWAX"),
    API_USER("Rbitech2026"),
    API_PASS("Rb1t3ch2026"),
    API_URL("https://device.vernal.ltd/tagapi"),
    API_LOGIN("/user/login"),
    API_DEVICE("/device/getDeviceList"),
    API_LOCATION("/device/getLocationList"),
    RABBITMQ_URI("192.168.1.65"),
    RABBITMQ_PORT("5672"),
    RABBITMQ_VHOST("/"),
    RABBITMQ_USER("prueba"),
    RABBITMQ_PASS("1234"),
    RABBITMQ_EXCHANGE("change"),
    RABBITMQ_PROVIDER("T-SIGO-02"),  
    RABBITMQ_TOPIC("iop.vid.custom"),
    REDIS_URI("192.168.1.65"),
    REDIS_PORT("6379"),
    REDIS_PASS("");
            
    private String e_valor;

    private E_Parametros(String _val) {
        this.e_valor=_val;
    }

    public void setPara(String _pa) {
        if (_pa==null) return;
        if (this.e_valor.equals(_pa)) return;
        this.e_valor=_pa;
    }

    public String getParaSTR() {
        return e_valor; 
    }
    
    public int getParaINT() {
        try {
            return Integer.parseInt(this.e_valor); 
        } catch (NumberFormatException e) {
            return -1;
        }    
    }
    
    public List<String> getParaLIST() {
        List<String> y_prl=new ArrayList<>();
        StringBuilder y_tg=new StringBuilder("");
        this.e_valor.chars().forEach(C->{
            if ((C==44)||(C==59)) {
                y_prl.add(y_tg.toString());
                if (!y_tg.isEmpty()) y_tg.setLength(0);
            } else y_tg.append((char)C);
        });
        y_prl.add(y_tg.toString());
        return y_prl;
    }
  

}

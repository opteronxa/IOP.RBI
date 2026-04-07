package files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author Xanum
 */

//S-SINGLETON
public final class S_Configuracion {
    
    private static S_Configuracion S_CONF;
    private final Configuracion AI;

//Instancia
    public static S_Configuracion getInstancia() throws Exception {
        if (S_CONF==null) S_CONF = new S_Configuracion();
        else throw new Exception ("No puede intanciar doble config.properties");
        return S_CONF;
    }

//Constructor
    private S_Configuracion() throws Exception { 
        this.AI=new Configuracion();
        if (AI.getParametroSTR("IOP_NOMBRE").isEmpty()) throw new Exception ("Error de Parametro [iop.nombre]");
        this.getMQ();
    }
    
    public String getNombre() {
        return AI.getParametroSTR("IOP_NOMBRE");
    }    
    
    public JSONObject getAPI_URL() {
        try {
            var y_url=AI.getParametroSTR("API_URL");
            if (y_url.isEmpty()) throw new Exception ("URL gateway Device No Solicitado");
            if (!y_url.startsWith("https://")) y_url="https//".concat(y_url);
            URI y_u=new URI(y_url);
            if (!y_u.getScheme().equals("https")) throw new Exception ("URL gateway Device No definio https://");
            JSONObject y_http = new JSONObject().put("url", y_u.getScheme() + "://" + y_u.getHost() + y_u.getPath()); 
            if (AI.getParametroSTR("API_USER").isEmpty()) throw new Exception ("URL API USER No definido");
            y_http.put("user", AI.getParametroSTR("API_USER"));
            if (AI.getParametroSTR("API_PASS").isEmpty()) throw new Exception ("URL API PASS No definido");
            y_http.put("pass", AI.getParametroSTR("API_PASS"));
            if (AI.getParametroSTR("API_CID").isEmpty()) throw new Exception ("URL API CID No definido");
            y_http.put("cid", AI.getParametroSTR("API_CID"));
            if (AI.getParametroSTR("API_LOGIN").isEmpty()) throw new Exception ("URL API LOGIN No definido");
            y_http.put("login", AI.getParametroSTR("API_LOGIN"));
            if (AI.getParametroSTR("API_DEVICE").isEmpty()) throw new Exception ("URL API DEVICE No definido");
            y_http.put("device", AI.getParametroSTR("API_DEVICE"));
            if (AI.getParametroSTR("API_LOCATION").isEmpty()) throw new Exception ("URL API LOCATION No definido");
            y_http.put("location", AI.getParametroSTR("API_LOCATION"));
            y_http.put("ok", true);
            return y_http;
        } catch (Exception e) {
            return new JSONObject().put("ok", false);
        }   
    }

    
    public JSONObject getMQ() throws Exception {
        var y_rabbit=new JSONObject();
        var y_uri=AI.getParametroSTR("RABBITMQ_URI");
        if (!y_uri.startsWith("//")) y_uri="//".concat(y_uri);
        if (y_uri.isEmpty()) throw new Exception ("Error de Parametro [rabbitmq.url]");     
        if (AI.getParametroSTR("RABBITMQ_VHOST").isEmpty())    throw new Exception ("Error de Parametro [rabbitmq.vhost]");  
        if (AI.getParametroSTR("RABBITMQ_USER").isEmpty())     throw new Exception ("Error de Parametro [rabbitmq.user]");    
        if (AI.getParametroSTR("RABBITMQ_PASS").isEmpty())     throw new Exception ("Error de Parametro [rabbitmq.pass]");    
        if (AI.getParametroSTR("RABBITMQ_EXCHANGE").isEmpty()) throw new Exception ("Error de Parametro [rabbitmq.exchange]");
        if (AI.getParametroSTR("RABBITMQ_TOPIC").isEmpty())    throw new Exception ("Error de Parametro [rabbitmq.topic]");
        String[] y_topic=AI.getParametroSTR("RABBITMQ_TOPIC").trim().split("\\.");
        if (y_topic.length<1) throw new Exception ("Error de Parametro [rabbitmq.topic]");
        var y_pto=AI.getParametroINT("RABBITMQ_PORT");
        if (y_pto<10) y_pto=5672;
        URI y_u=new URI(y_uri);
        if (y_u.getPort()>10) y_pto=y_u.getPort();
        y_rabbit.put("uri", y_u.getHost()); 
        y_rabbit.put("pto", y_pto);
        y_rabbit.put("vhost", AI.getParametroSTR("RABBITMQ_VHOST")); 
        y_rabbit.put("exchange", AI.getParametroSTR("RABBITMQ_EXCHANGE"));  
        y_rabbit.put("user", AI.getParametroSTR("RABBITMQ_USER")); 
        y_rabbit.put("pass", AI.getParametroSTR("RABBITMQ_PASS"));    
        var y_urlvalidator = new UrlValidator();
        var y_ipvalidator = InetAddressValidator.getInstance();
        if (!y_urlvalidator.isValid(y_rabbit.getString("uri"))) {
            if (!y_ipvalidator.isValid(y_rabbit.getString("uri"))) throw new Exception ("Error de Parametro [rabbitmq.uri]");
        } 
        if (!AI.getParametroSTR("RABBITMQ_PROVIDER").isEmpty()) y_rabbit.put("prov", AI.getParametroSTR("RABBITMQ_PROVIDER"));   
        JSONArray y_top=new JSONArray();
        for (String y_topic1 : y_topic) {
            switch (y_topic1.trim().toLowerCase()) {
                case "iop"     -> y_top.put(0);
                case "vid"     -> y_top.put(1); 
                case "custom"  -> y_top.put(2);
                default -> throw new Exception ("Error de Parametro [rabbitmq.topic] (nombre,custom,vid)"); 
            }
        }
        y_rabbit.put("topic", y_top);
        return y_rabbit;
    }
    
    public JSONObject getRedis() {
        var y_red=new JSONObject();
        try {
            String y_uri=AI.getParametroSTR("REDIS_URI");
            if (!y_uri.startsWith("//")) y_uri="//".concat(y_uri);
            if (y_uri.isEmpty()) throw new Exception ("REDIS No Solicitado");
            int y_pto = AI.getParametroINT("REDIS_PORT");
            if (y_pto<10) y_pto=6379;
            URI y_u=new URI(y_uri);
            if (y_u.getPort()>10) y_pto=y_u.getPort();
            y_red.put("uri", y_u.getHost()); 
            y_red.put("pto", y_pto);
            var y_urlvalidator = new UrlValidator();
            var y_ipvalidator = InetAddressValidator.getInstance();
            if (!y_urlvalidator.isValid(y_red.getString("uri"))) {
                if (!y_ipvalidator.isValid(y_red.getString("uri"))) throw new Exception ("Invalido [redis.uri]");
            } 
            if (!AI.getParametroSTR("REDIS_PASS").isEmpty()) y_red.put("pass", AI.getParametroSTR("REDIS_PASS"));
            y_red.put("ok", true);
            return y_red;
        } catch (Exception e) {
            y_red.put("ok", false).put("msg", e.getMessage());
        }   
        return y_red;
    }
    
    public int getRedisFrec() {
        if (AI.getParametroINT("REDIS_SAMPLE")<=10)  return 10;
        if (AI.getParametroINT("REDIS_SAMPLE")>=3600) return 3600;
        return AI.getParametroINT("REDIS_SAMPLE");
    }
    
    public JSONObject Actualizar(String _pa, String _val) {
        if (_pa.trim().toLowerCase().equals("query")) {
            JSONObject y_qry=new JSONObject();
            Arrays.stream(E_Parametros.values()).forEach(E->{
               y_qry.put(AI.getLLave(E.name()), AI.getParametroSTR(E.name()));
            });
            return new JSONObject().put("ok", true).put("config", y_qry);
        }
        if (this.AI.setParametro(_pa.trim(), _val.trim())) return new JSONObject().put("ok", true).put("msg", "Cambio de Parametro [config.properties] " + _pa.toUpperCase() + "=" + _val);
        return new JSONObject().put("ok", false).put("msg", "No se pudo realizar Cambio de Parametro [config.properties] " + _pa.toLowerCase() + "=" + _val);
    }
    
//*****Internal ClASS CONFIGURACION*********************************************/
   
    /**
     * sub class Leer Configuracion
     */
    public class Configuracion {
        
        private String ACONF=(new File(".").getCanonicalPath()).concat(File.separator).concat("config.properties"); 
        
        public Configuracion() throws Exception {
            var y_prop = new Properties();
            if (!(new File(this.ACONF).exists())) this.WriteParam();
            try (FileInputStream y_input = new FileInputStream(this.ACONF)) {
                y_prop.load(y_input);
                Arrays.stream(E_Parametros.values()).forEach(E->E.setPara(y_prop.getProperty(this.getLLave(E.name()))));
            } catch (IOException e) {
                throw new Exception ("Lectura Archivo de Configuracion " + e.getMessage());
            }
        }         

        private void WriteParam() throws Exception {
            var y_prop = new Properties();
            Arrays.stream(E_Parametros.values()).forEach(E->y_prop.setProperty(this.getLLave(E.name()), E.getParaSTR()));
            try (FileOutputStream output = new FileOutputStream(this.ACONF)) {
                y_prop.store(output, "Configuración IOP");
            } catch (IOException e) {
                throw new Exception ("Escritura Archivo config.properties " + e.getMessage());
            }
        }
    
        private String getLLave(String _pa) {
            return switch (_pa.toUpperCase()) {
                case "IOP_NOMBRE"        -> "iop.nombre";
                case "API_CID"           -> "api.cid";
                case "API_USER"          -> "api.user";
                case "API_PASS"          -> "api.pass";
                case "API_URL"           -> "api.url";
                case "API_LOGIN"         -> "api.endpoint.login";
                case "API_DEVICE"        -> "api.endpoint.device";
                case "API_LOCATION"      -> "api.endpoint.location";
                case "RABBITMQ_URI"      -> "rabbitmq.uri";
                case "RABBITMQ_PORT"     -> "rabbitmq.port";
                case "RABBITMQ_VHOST"    -> "rabbitmq.vhost";
                case "RABBITMQ_USER"     -> "rabbitmq.user";
                case "RABBITMQ_PASS"     -> "rabbitmq.pass";
                case "RABBITMQ_EXCHANGE" -> "rabbitmq.exchange";
                case "RABBITMQ_PROVIDER" -> "rabbitmq.provider";
                case "RABBITMQ_TOPIC"    -> "rabbitmq.topic";
                case "REDIS_URI"         -> "redis.uri";
                case "REDIS_PORT"        -> "redis.port";
                case "REDIS_PASS"        -> "redis.pass";
                case "REDIS_SAMPLE"      -> "redis.frecuencia";
                default -> "";     
            };
        }  
        
        public String getParametroSTR(String _pa) {
            if (this.getLLave(_pa).isEmpty()) return "";
            return files.E_Parametros.valueOf(_pa.toUpperCase()).getParaSTR();
        }
        
        public int getParametroINT(String _pa) {
            if (this.getLLave(_pa).isEmpty()) return -1;
            return files.E_Parametros.valueOf(_pa.toUpperCase()).getParaINT();
        }
        
        public List<String> getParametroList(String _pa) {
            return files.E_Parametros.valueOf(_pa.toUpperCase()).getParaLIST();
        }
        

        public boolean setParametro(String _pa, String _val) {
            String y_epa=this.getReversa(_pa);
            if (y_epa.isEmpty()) return false;
            try { 
                E_Parametros.valueOf(y_epa).setPara(_val);
                this.WriteParam();
                return true;
            } catch (Exception e) {
                return false;
            }    
        }
        
        private String getReversa(String _pa) {
            return switch (_pa.toLowerCase()) {
                case "iop.nombre"            -> "IOP_NOMBRE";
                case "api.cid"               -> "API_CID";
                case "api.user"              -> "API_USER";
                case "api.pass"              -> "API_PASS";
                case "api.url"               -> "API_URL";
                case "api.endpoint.login"    -> "API_LOGIN";
                case "api.endpoint.device"   -> "API_DEVICE";
                case "api.endpoint.location" -> "API_LOCATION";
                case "rabbitmq.uri"          -> "RABBITMQ_URI";
                case "rabbitmq.port"         -> "RABBITMQ_PORT";
                case "rabbitmq.vhost"        -> "RABBITMQ_VHOST";
                case "rabbitmq.user"         -> "RABBITMQ_USER";
                case "rabbitmq.pass"         -> "RABBITMQ_PASS";
                case "rabbitmq.exchange"     -> "RABBITMQ_EXCHANGE";
                case "rabbitmq.provider"     -> "RABBITMQ_PROVIDER";
                case "rabbitmq.topic"        -> "RABBITMQ_TOPIC";
                case "redis.uri"             -> "REDIS_URI";
                case "redis.port"            -> "REDIS_PORT";
                case "redis.pass"            -> "REDIS_PASS";
                case "redis.frecuencia"      -> "REDIS_SAMPLE";
                default -> "";     
            };
        }  
        
    }    
    
}

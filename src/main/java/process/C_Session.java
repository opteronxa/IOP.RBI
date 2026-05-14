package process;

import iop.Iop;
import database.C_Redis;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import publisher.C_PublisRabbitMQ;

/**
 *
 * @author ByXanum
 */
public class C_Session {    // === Login ===
    
    private final Logger LOG = C_Logger.getLogger(C_Session.class);
    private final C_Criptor CRIPTO =new C_Criptor();
    private final Map<String, JSONObject> INFOMAP = new HashMap<>();
    private final JSONObject API;
    private final String IOP;
    private C_ClientHTTP x_post; 
    private int x_userid;
    private byte[] x_key; 
    private int x_page=1;
    private int x_pages=1;
    private int x_recs=0;
    private int x_rin=0;

    public C_Session() throws Exception {
        try {
            this.IOP = Iop.CONF.getNombre();
            this.API = Iop.CONF.getAPI_URL();
        } catch (Exception e) {
            LOG.error("Parametros de Configuracion para la Sesion: " + e.getMessage());
            throw new Exception (e.getMessage());
        }    
    }
     
    // === Login ===
    public boolean login() {
        LOG.info("= LOGIN =");
        try (C_PublisRabbitMQ y_rabb = new C_PublisRabbitMQ()) {
            try (C_Redis y_reds = new C_Redis()) {
                this.x_post = new C_ClientHTTP(this.API.getString("url"));
                try {
                    LoginResponse();
                    LOG.info("Login correcto"); 
                    var y_now = System.currentTimeMillis();
                    do {
                        try {
                            this.loadDevices(y_reds);
                            this.INFOMAP.forEach((D, I) ->  this.getEvent(y_rabb, y_reds, D, I, y_now));
                            ++this.x_page;
                        } catch (Exception e4) {
                            throw new Exception (e4.getMessage());
                        }    
                    } while (this.x_page<this.x_pages);    
                    LOG.info("Total Regitros Publicados y confirmado en RabbitMQ: " + this.x_rin);
                } catch (Exception e3) {
                    LOG.error("No pudo concretarse el Login: " + e3.getMessage());
                }     
            } catch (Exception e2) {
                throw new Exception (e2.getMessage());
            }
        } catch (Exception e1) {
            LOG.error(e1.getMessage());
            return false;
        }    
        return true;
    }

    public void LoginResponse() throws Exception {
        try {
            JSONObject y_body = new JSONObject().put("cid",  this.API.getString("cid"))
                                                .put("name", this.API.getString("user"))
                                                .put("pwd",  this.API.getString("pass"));
            var y_resJson = this.x_post.post(this.API.getString("login"), y_body);
            if (y_resJson.isEmpty()) throw new Exception ("JSON Response del  Gateway esta Vacio"); 
            if (!y_resJson.has("items")) throw new Exception ("JSON Response del  Gateway no tiene nodo [items]: " + y_resJson.toString()); 
            var y_items =  y_resJson.getJSONObject("items");
            if (!y_items.has("id")) throw new Exception ("JSON Response del Gateway no tiene nodo [id]: " + y_resJson.toString());
            if (!y_items.has("token")) throw new Exception ("JSON Response del Gateway no tiene nodo [token]" + y_resJson.toString());
            this.x_userid = y_items.getInt("id");
            this.x_key = y_items.getString("token").substring(0, 24).getBytes("UTF-8");
        } catch (Exception e) {
            throw new Exception ("->LonginResponse: " + e.getMessage());
        }    
    }
    
// === loadDevices con JSONObject ===
    public void loadDevices(C_Redis _reds) throws Exception {
        LOG.info("= GET DEVICE LIST " + this.x_page + " =");
        try {
            var y_plain = new JSONObject().put("pageNo", this.x_page)
                                          .put("pageSize", 200)
                                          .put("queryFilter", new JSONObject().put("userId", this.x_userid));
            var y_enc = this.CRIPTO.encrypt3DES_ECB_Base64(y_plain.toString(), this.x_key);
            var y_body = new JSONObject().put("userId", this.x_userid).put("data", y_enc);
            var y_resJson = this.x_post.post(this.API.getString("device"), y_body);
            if (!y_resJson.has("items")) throw new Exception ("Sin nodo [items] en devices");
            var y_itemsEnc = y_resJson.getString("items");
            var y_decrypted = this.CRIPTO.decrypt3DES_ECB_Base64(y_itemsEnc, this.x_key);
            var y_recordsObj = new JSONObject(y_decrypted);
            if (!y_recordsObj.has("records")) throw new Exception ("Sin nodo [records] en Desencriptado devices");
            var y_records = y_recordsObj.getJSONArray("records");
            if ((this.x_page==1)&&(!y_recordsObj.has("pages"))) this.x_pages = y_recordsObj.getInt("pages");
            this.INFOMAP.clear();
            for (int i = 0; i < y_records.length(); i++) {
                var y_record = y_records.getJSONObject(i);
                if (!y_record.has("deviceNum")) continue;
                JSONObject y_redis = _reds.getVId(y_record.getString("deviceNum"));
                if (!y_redis.getBoolean("ok")) continue;
                y_record.put("iop",new JSONObject().put("custom", y_redis.getString("custom"))
                                                   .put("consec", y_redis.getInt("consec"))
                                                   .put("beginTime", y_redis.getLong("time")));
                this.INFOMAP.put(y_record.getString("deviceNum"), y_record);
                LOG.info("DevicesNum: " + y_record.getString("deviceNum"));
                ++this.x_recs;
            }
            LOG.info("Devices en secuencia " + this.x_page + " Cargados en total: " + this.x_recs);
        } catch (Exception e) {
            throw new Exception ("->LoadDevice: " + e.getMessage());
        }    
    }
    
    // === Get Event ===
    public void getEvent(C_PublisRabbitMQ _rabb,
                         C_Redis _reds,
                         String _devicenum,
                         JSONObject _info,
                         long _now) {
        if (!_info.has("iop")) return;
        try {
            JSONObject y_iop = _info.getJSONObject("iop");
            var y_conse = y_iop.optInt("consec");
            long y_beg = y_iop.optLong("beginTime");
            try {
                if (y_beg<(_now - 86400000)) {
                    y_beg =(_now - 86400000);
                    y_iop.put("beginTime", y_beg);
                }
                JSONObject y_plain = new JSONObject().put("pageNo", 1)
                                                     .put("pageSize", 1)
                                                     .put("queryFilter", new JSONObject().put("deviceNum", _devicenum)
                                                                                         .put("userId", this.x_userid)
                                                                                         .put("beginTime", y_beg)
                                                                                         .put("endTime", _now));
                var y_enc = this.CRIPTO.encrypt3DES_ECB_Base64(y_plain.toString(), this.x_key);
                var y_body=new JSONObject().put("userId", this.x_userid).put("data", y_enc);
                var y_resJson = this.x_post.post(this.API.getString("location"), y_body);
                if (!y_resJson.has("items")) return;
                var y_dec=new JSONObject(this.CRIPTO.decrypt3DES_ECB_Base64(y_resJson.getString("items"), this.x_key));
                if (!y_dec.has("records")) return;
                JSONArray y_record=y_dec.getJSONArray("records");
                if (y_record.isEmpty()) return;
                y_iop.put("name",this.IOP).put("endTime", _now);
                _info.put("records", y_record);
                _info.put("iop", y_iop);
                if (_rabb.PublicarMQ(_info)) {
                    _reds.setVId(_devicenum, new JSONObject().put("custom", y_iop.getString("custom"))
                                                             .put("consec", y_conse)
                                                             .put("time", _now+1));
                    ++this.x_rin;
                }    
            } catch (Exception e2) {
                LOG.info("->Devices Num: " + _devicenum + " : " + e2.getMessage());
            }  
        } catch (JSONException e1) {
            LOG.error("->GetEvent: " + e1.getMessage());
        }    
    }

}

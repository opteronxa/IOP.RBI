package process;

import database.C_Redis;
import iop.Iop;
import java.util.HashMap;
import java.util.Map;
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
    private final C_ClientHTTP POST;
    private final JSONObject API;

    public C_Session() throws Exception {
        this.API = Iop.CONF.getAPI_URL();
        this.POST = new C_ClientHTTP(this.API.getString("url"));
    }
     
    // === Login ===
    public void login() throws Exception {
        LOG.info("= LOGIN =");
        JSONObject y_body = new JSONObject().put("cid",  this.API.getString("cid"))
                                            .put("name", this.API.getString("user"))
                                            .put("pwd",  this.API.getString("pass"));
        var y_resJson = POST.post(this.API.getString("login"), y_body);
        LOG.info("Login correcto");
        try (C_PublisRabbitMQ y_rabb = new C_PublisRabbitMQ()) {
            try (C_Redis y_reds = new C_Redis()) {
                C_LoginResponse y_lr = new C_LoginResponse(y_resJson);
                this.loadDevices(y_reds, y_lr.getUserID(), y_lr.getKey());
                for (String s: this.INFOMAP.keySet()) {
                    this.getEvent(y_rabb, y_reds, y_lr.getUserID(), y_lr.getKey(), s);
                }
            } catch (Exception e2) {
                throw new Exception ("Secuencia2 Redis " + e2.getMessage());
            }
        } catch (Exception e1) {
            throw new Exception ("Secuencia1 Rabbit " + e1.getMessage());
        }    
    }
    
    // === loadDevices con JSONObject ===
    public void loadDevices(C_Redis _reds, int _userId, byte[] _key) throws Exception {
        LOG.info("= GET DEVICE LIST =");
        var y_plain = new JSONObject().put("pageNo", 1)
                                      .put("pageSize", 200)
                                      .put("queryFilter", new JSONObject().put("userId", _userId));
        var y_enc = this.CRIPTO.encrypt3DES_ECB_Base64(y_plain.toString(), _key);
        var y_body = new JSONObject().put("userId", _userId).put("data", y_enc);
        var y_resJson = this.POST.post(this.API.getString("device"), y_body);
        if (!y_resJson.has("items")) throw new Exception ("Sin nodo [items] en devices");
        var y_itemsEnc = y_resJson.getString("items");
        var y_decrypted = this.CRIPTO.decrypt3DES_ECB_Base64(y_itemsEnc, _key);
        var y_recordsObj = new JSONObject(y_decrypted);
        if (!y_recordsObj.has("records")) throw new Exception ("Sin nodo [records] en Desencriptado devices");
        var y_records = y_recordsObj.getJSONArray("records");
        this.INFOMAP.clear();
        for (int i = 0; i < y_records.length(); i++) {
            var d = y_records.getJSONObject(i);
            JSONObject y_red = _reds.getVId(d.getString("deviceNum"));
            if (!y_red.getBoolean("ok")) continue;
            this.INFOMAP.put(d.getString("deviceNum"), new JSONObject().put("mac", d.getString("mac"))
                                                                       .put("sn",  d.getString("sn"))
                                                                       .put("custom", y_red.getString("custom"))
                                                                       .put("consec", y_red.getInt("consec"))
                                                                       .put("time", y_red.getLong("time")));
        }
        LOG.info("Devices Cargados: " + y_records.length());
    }
    
    // === Get Event ===
    public void getEvent(C_PublisRabbitMQ _rabb, C_Redis _reds, int _userId, byte[] _key, String _deviceNum) throws Exception {
        var y_info = this.INFOMAP.get(_deviceNum);
        var y_conse = y_info.getInt("consec");
        long y_now = System.currentTimeMillis();
        long y_bef = y_info.getLong("time");
        if (y_bef<(y_now - 86400000)) y_bef =(y_now - 86400000);
               JSONObject y_plain = new JSONObject().put("pageNo", 1)
                                             .put("pageSize", 1)
                                             .put("queryFilter", new JSONObject().put("deviceNum", _deviceNum)
                                                                                 .put("userId", _userId)
                                                                                 .put("beginTime", y_bef)
                                                                                 .put("endTime", y_now));
        var y_enc = this.CRIPTO.encrypt3DES_ECB_Base64(y_plain.toString(), _key);
        var y_body=new JSONObject().put("userId", _userId).put("data", y_enc);
        var y_resJson = this.POST.post(this.API.getString("location"), y_body);
        String y_decry = this.CRIPTO.decrypt3DES_ECB_Base64(y_resJson.getString("items"), _key);
        var y_dec=new JSONObject(y_decry);
        if (!y_dec.has("records")) return;
        var y_rec=y_dec.getJSONArray("records");
        for (int i=0; i<y_rec.length();i++) {
            var y_mob=y_rec.getJSONObject(i).put("vid", _deviceNum)
                                            .put("custom", y_info.getString("custom"))
                                            .put("consec", ++y_conse);
            _rabb.PublicarMQ(y_mob);
        } 
        _reds.setVId(_deviceNum, new JSONObject().put("custom", y_info.getString("custom"))
                                                 .put("consec", y_conse)
                                                 .put("time", ++y_now));
    }

}

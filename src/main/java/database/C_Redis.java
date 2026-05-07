package database;

import iop.Iop;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import process.C_Logger;
import redis.clients.jedis.Jedis;

/**
 *
 * @author ByXanum
 */
public class C_Redis implements AutoCloseable {

    private final Logger LOG = C_Logger.getLogger(C_Redis.class);
    private final C_JedisConnect REDI ;

    public C_Redis() throws Exception {
        try {
            JSONObject y_redispa = Iop.CONF.getRedis();
            if (!y_redispa.getBoolean("ok")) {
                if (y_redispa.has("msg")) throw new Exception(y_redispa.getString("msg"));
                throw new Exception("Falla de los paramteros REDIS");
            }
            this.REDI = new C_JedisConnect(Iop.CONF.getNombre(), y_redispa);
            this.REDI.Connect();
            LOG.info("Conexion database REDIS OK!");
        } catch (Exception e) {
            throw new Exception("SIN CONEXION Estable a REDIS: " + e.getMessage());
        }
    }

    // *************VID**************************************************************
    public JSONObject getVId(String _vid) throws Exception {
        try {
            Jedis y_jed = this.REDI.getJE();
            if (y_jed.hexists(this.REDI.getKEY(), _vid)) {
                String y_j = y_jed.hget(this.REDI.getKEY(), _vid);
                JSONObject y_vid = new JSONObject(y_j);
                if (!y_vid.has("consec")) y_vid.put("consec", 0);
                if (!y_vid.has("time")) y_vid.put("time", 0);
                return y_vid.put("ok", y_vid.has("custom"));
            }    
        } catch (JSONException | NullPointerException e) {
            LOG.error("Error parseando JSON para VId " + _vid + ": " + e.getMessage(), e);
        }
        return new JSONObject().put("ok", false);
    }

    public boolean setVId(String _vid, JSONObject _rec) throws Exception {
        try {
            Jedis y_jed = this.REDI.getJE();
            if (_rec.has("ok")) _rec.remove("ok");
            y_jed.hset(this.REDI.getKEY(), _vid, _rec.toString());
            return true;
        } catch (Exception e) {
            LOG.error("Error en Jedis setVId para " + _vid + ": " + e.getMessage(), e);
            return false;
        }
    }

    public String setVIdAB(String _vid, String _custom) throws Exception {
        try {
            String y_vid=_vid;
            JSONObject y_a;
            if (_vid.startsWith("!")) {
                y_vid = _vid.substring(1);
                y_a = this.getVId(y_vid);
                if (y_a.getBoolean("ok")) {
                    this.REDI.getJE().hdel(this.REDI.getKEY(), _vid.substring(1));
                    return "Eliminado el Movil " + _vid.substring(1);
                }
                return "Movil No Registrado: " + _vid.substring(1);
            }
            y_a = this.getVId(y_vid);
            if (y_a.getBoolean("ok")) return "VId Movil Ya Existe: " + _vid;
            y_a.clear();
            y_a.put("custom", _custom);
            y_a.put("consec", 0);
            this.setVId(y_vid, y_a);
            return "VId Registro de Movil Exitoso: " + _vid;
        } catch (Exception e) {
            LOG.error("Error en setVIdAB para " + _vid + ": " + e.getMessage(), e);
            return "Error de Registro de Movil: " + _vid;
        }
    }

    public JSONObject ListVIds() throws Exception {
        try {
            var y_vids = this.REDI.getJE().hgetAll(this.REDI.getKEY());
            JSONArray y_v = new JSONArray();
            y_vids.forEach((V, H) -> {
                y_v.put(new JSONObject().put("vid", V).put("hash", H));
            });
            return new JSONObject().put("list", y_v);
        } catch (JSONException e) {
            LOG.error("Error creando lista de VIds: " + e.getMessage(), e);
            return new JSONObject().put("err", "Error de Listado de Moviles");
        }
    }
    
    // ******************************************************************************
    @Override
    public void close() {
        this.REDI.Disconnect();
        LOG.info("Desconexion Redis " + this.REDI.getKEY());
    }

}



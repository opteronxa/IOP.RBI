package database;

import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author ByXanum
 */
public class C_JedisConnect {
    

    private final String NOMBRE;
    private final JedisPoolConfig CONFIG = new JedisPoolConfig();
    private Jedis x_jedis = null;
    private JedisPool x_jedisPool = null;
    private final String PASS;
    private final int PTO;    
    private final String URI;   
    private boolean x_status=false;
    
    public C_JedisConnect(String _iop, JSONObject _redispa) throws Exception {
        if (_redispa==null) throw new Exception("No se tiene cadena de conexion Redis");
        if (!_redispa.has("uri")) throw new Exception("falta URL de conexion Redis");
        if (!_redispa.has("pto")) throw new Exception("Falta puerto de conexion Redis");
        if (_redispa.getString("uri")==null) throw new Exception("falta URL de conexion Redis");
        this.NOMBRE = "server:".concat(_iop).concat(":vid");
        this.PASS = _redispa.has("pass")?_redispa.getString("pass"):null;
        this.URI = _redispa.getString("uri");
        this.PTO = _redispa.getInt("pto");
        CONFIG.setMaxTotal(10);
        CONFIG.setMaxIdle(5);
        CONFIG.setMinIdle(1);
    }

    public void Connect() throws Exception {
        try {
            if ((this.x_jedisPool == null)&&(this.x_jedis == null)) {
                this.x_jedisPool = this.PASS==null
                        ?new JedisPool(this.CONFIG, this.URI, this.PTO, 2000)
                        :new JedisPool(this.CONFIG, this.URI, this.PTO, 2000, this.PASS);
                this.x_jedis = this.x_jedisPool.getResource();
            }    
        } catch (JSONException e) {
            this.Disconnect();
            throw new Exception("Falla Cadena Conexion JSON en Redis: " + e.getMessage(), e);
        }
        this.x_status=true;
    }
    
    public void Disconnect() {
        this.x_status=false;
        if (this.x_jedis != null) this.x_jedis.close();
        this.x_jedis = null;
        if (this.x_jedisPool != null) this.x_jedisPool.close();
        this.x_jedisPool = null;  
    }
    
    public String getKEY() {
        return this.NOMBRE;
    }
    
    public Jedis getJE() throws Exception {
        if (!this.x_status) this.Connect();
        return this.x_jedis;
    }
    
        
}

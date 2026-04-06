package process;

import java.io.UnsupportedEncodingException;
import org.json.JSONObject;

/**
 *
 * @author ByXanum
 */
public class C_LoginResponse {

    private int USERID;
    private String TOKEN;
    private byte[] KEY; 

    public C_LoginResponse(JSONObject _res) throws UnsupportedEncodingException {
        if (_res.has("items")) {
            if (_res.getJSONObject("items").has("id") && (_res.getJSONObject("items").has("token"))) {
                this.USERID = _res.getJSONObject("items").getInt("id");
                this.TOKEN = _res.getJSONObject("items").getString("token");
                this.KEY = this.TOKEN.substring(0, 24).getBytes("UTF-8");
            } 
        }            
    }
    
    public int getUserID() {
        return this.USERID;
    }
    
    public byte[] getKey() {
        return this.KEY;
    }
    
}

package publisher;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import process.C_Logger;
import iop.Iop;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author ByXanum
 */
public class C_PublisRabbitMQ implements AutoCloseable {

    private final Logger LOG = C_Logger.getLogger(C_PublisRabbitMQ.class);
    private final Connection CONN;
    private final Channel CHAN;
    private final String EXCHANGE;
    private final Set<Integer> TOPIC = new TreeSet<>();
    private final StringBuilder KEYTOPIC = new StringBuilder();
    private final String NOMBRE;

    public C_PublisRabbitMQ() throws Exception {
        this.NOMBRE = Iop.CONF.getNombre();
        try {
            JSONObject y_jmq=Iop.CONF.getMQ();
            ConnectionFactory faconn= new ConnectionFactory();
            faconn.setHost(y_jmq.getString("uri"));
            faconn.setPort(y_jmq.getInt("pto"));
            faconn.setUsername(y_jmq.getString("user"));
            faconn.setPassword(y_jmq.getString("pass"));
            if (!y_jmq.getString("vhost").equals("/")) faconn.setVirtualHost(y_jmq.getString("vhost"));
            if (y_jmq.has("prov")) {
                Map<String, Object> y_prop=new HashMap<>();
                y_prop.put("connection_name", y_jmq.getString("prov"));
                faconn.setClientProperties(y_prop);
            }    
            this.EXCHANGE=y_jmq.getString("exchange").concat(".topic");
            this.CONN=faconn.newConnection();
            this.CHAN=this.CONN.createChannel();
            this.CHAN.exchangeDeclare(this.EXCHANGE, BuiltinExchangeType.TOPIC, true);
            if (!this.CHAN.isOpen()) throw new Exception("Alerta: No se Establecio Conexion Servidor RabbitMQ");
            for (int y_i = 0; y_i < y_jmq.getJSONArray("topic").length(); y_i++) {
                TOPIC.add(y_jmq.getJSONArray("topic").getInt(y_i));
            }
        }catch (IOException e) {
            throw new Exception("RabbitMQ: " + e.getMessage());
        }
    }

    public void PublicarMQ(JSONObject _pay) throws Exception {
        try {
            if (!this.KEYTOPIC.isEmpty())  this.KEYTOPIC.setLength(0);
            for (int y_i : this.TOPIC) {
                switch (y_i) {
                    case 0 -> { this.KEYTOPIC.append(this.NOMBRE).append("."); }
                    case 1 -> { this.KEYTOPIC.append(_pay.getString("custom")).append("."); }
                    case 2 -> { this.KEYTOPIC.append(_pay.getString("vid")).append("."); }
                }
            }
            if (this.KEYTOPIC.isEmpty())  this.KEYTOPIC.append(this.NOMBRE).append(".");
            this.KEYTOPIC.delete(this.KEYTOPIC.length() - 1, this.KEYTOPIC.length());
            this.Publicar(_pay);
        } catch (JSONException e) {
            LOG.error("RabbitMQ Fallida por sintaxis de JSON incorrecta: " + _pay);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private void Publicar(JSONObject _pay) throws Exception {
        try {
/**/  System.out.println("RABBIT: " + _pay.toString() );
            this.CHAN.basicPublish(this.EXCHANGE, this.KEYTOPIC.toString(), null, _pay.toString().getBytes());
        } catch (Exception e1) {
            LOG.error("RabbitMQ publish failed for key {} payload {}", this.KEYTOPIC, _pay, e1);
            throw new Exception("RabbitMQ: " + (e1.getMessage() != null ? e1.getMessage() : "Desconocido"), e1);
        }
    }

    @Override
    public void close() {
        try {
            if (this.CHAN != null && this.CHAN.isOpen()) this.CHAN.close();
        } catch (IOException | TimeoutException e) {
            LOG.error("Error closing RabbitMQ channel", e);
        }
        try {
            if (this.CONN != null && this.CONN.isOpen()) this.CONN.close();
        } catch (IOException e) {
            LOG.error("Error closing RabbitMQ connection", e);
        }
    }

}

package files;

import iop.Iop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ByXanum
 */
public class C_Files {   
    
    private final List<String> TAGS = new ArrayList<>();
    private File x_arch;
    private boolean x_all=true;

    public C_Files() throws Exception {
        this.x_arch=new File((new File(".").getCanonicalPath()).concat(File.separator).concat(Iop.CONF.getFileTag())); 
        if (this.x_arch.exists()) {
             try (BufferedReader y_linea=new BufferedReader(new FileReader(this.x_arch))) {
                String y_tag=y_linea.readLine();
                while (y_tag!=null) {
                    if (this.x_all) this.x_all=false;
                    this.TAGS.add(y_tag);
                    y_tag=y_linea.readLine();
                }
            } catch (IOException e) {
                throw new Exception ("Escritura Archivo config.properties " + e.getMessage());
            }
        }    
    }
    
    public boolean getAll() {
        return this.x_all;
    }
    
    public List<String> getTags() {
        return this.TAGS;
    }
    
    
    
}

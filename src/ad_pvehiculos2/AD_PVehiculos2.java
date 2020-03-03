package ad_pvehiculos2;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.bson.Document;

/**
 *
 * @author oracle
 */
public class AD_PVehiculos2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs;
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("basevehiculos");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb/db/finalveh.odb");
        EntityManager em = emf.createEntityManager();
        conn = SqlConnection.getConnection();
        String sql_SELECT = "SELECT * from vendas";
        stmt = conn.prepareStatement(sql_SELECT);
        rs = stmt.executeQuery();

        //
        String nomveh = "";
        Double prezoorixe = 0d;
        Double anomatricula = 0d;
        String nomec = "";
        Double ncompras = 0d;
        Double pf = 0d;

        // loop through the result set
        while (rs.next()) {
            double id = rs.getDouble("id");
            String dni = rs.getString("dni");
            java.sql.Struct x = (java.sql.Struct) rs.getObject(3);
            Object[] campos = x.getAttributes();
            String codveh = (String) campos[0];
            java.math.BigDecimal tasas = (java.math.BigDecimal) campos[1];

            System.out.println("id: " + id + " dni: " + dni + " codveh: " + codveh + " tasas: " + tasas);

            //Conectarse a MONGODB VEHICULOS
            MongoCollection collection = database.getCollection("vehiculos");
            BasicDBObject selectVehiculos = new BasicDBObject();
            selectVehiculos.put("_id", codveh);
            Document doc = (Document) collection.find(selectVehiculos).first();
            nomveh = doc.getString("nomveh");
            prezoorixe = doc.getDouble("prezoorixe");
            anomatricula = doc.getDouble("anomatricula");
            System.out.println("id: " + doc.getString("_id") + " nomveh: " + doc.getString("nomveh") + " prezoorixe: " + doc.getDouble("prezoorixe") + " anomatricula: " + doc.getDouble("anomatricula"));

            //Conectarse a MONGODB CLIENTES
            collection = database.getCollection("clientes");
            BasicDBObject selectClientes = new BasicDBObject();
            selectClientes.put("_id", dni);
            doc = (Document) collection.find(selectClientes).first();
            nomec = doc.getString("nomec");
            ncompras = doc.getDouble("ncompras");
            System.out.println("id: " + doc.getString("_id") + " nomec: " + doc.getString("nomec") + " ncompras: " + doc.getDouble("ncompras"));
            System.out.println("");

            if (ncompras == 0) {
                pf = prezoorixe - (2019 - anomatricula) * 500 + tasas.intValue();
                System.out.println(pf);
            } else {
                pf = prezoorixe - (2019 - anomatricula) * 500 - 500 + tasas.intValue();
                System.out.println(pf);
            }

            //INSERT EN OBJECTDB
            em.getTransaction().begin();
            Venfin p = new Venfin(id, dni, nomec, nomveh, pf);
            em.persist(p);
            em.getTransaction().commit();
        }
        SqlConnection.close(rs);
        SqlConnection.close(stmt);
        SqlConnection.close(conn);
        mongoClient.close();
        em.close();
        emf.close();

    }

}

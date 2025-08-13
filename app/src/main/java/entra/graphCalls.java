package entra;

//JOptionPane to display errors
//import javax.swing.JOptionPane;

//jackson desrialization
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

//HTTP request libraies
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.URI;



import com.microsoft.graph.beta.models.QrCodePinAuthenticationMethod;

//import com.azure.core.credential.AccessToken;
//import com.azure.core.credential.TokenRequestContext;

public class graphCalls {

    private final static String GRAPH_QRCODE_ENDPOINT_TEMPLATE = "users/%S/authentication/qrCodePinMethod";
    private final static String baseaddress = "https://graph.microsoft.com/beta/";

    public static QrCodePinAuthenticationMethod createQrCodeMethod(QrCodePinAuthenticationMethod qrCode) throws IOException, InterruptedException{
        QrCodePinAuthenticationMethod newMethod = null;
        
        
        String endpoint = String.format(GRAPH_QRCODE_ENDPOINT_TEMPLATE, App.activeUser.getId());
        String payload = "{\"@odata.type\": \"#microsoft.graph.qrCodePinAuthenticationMethod\"," +
            "\"standardQRCode\": {" +
            "    \"expireDateTime\": \"" + qrCode.getStandardQRCode().getExpireDateTime() + "\"," +
            "    \"startDateTime\": \"" + qrCode.getStandardQRCode().getStartDateTime() + "\"" +
            "}," +
            "\"pin\": {" +
            "    \"code\": \"" + qrCode.getPin().getCode() + "\"" +
            "}" +
            "}\"";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseaddress + endpoint))
            .PUT(BodyPublishers.ofString(payload))
            .header("Authorization", "Bearer " + App.accessToken.getToken())
            .header("Content-Type", "application/json")
            .build();
        
        HttpResponse<String> response = null;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            throw e;
        }

        if(null != response) {
            if(200 <= response.statusCode() && 300 > response.statusCode()) {
                //System.out.println(response.body());
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);

                try {
                    newMethod = mapper.readValue(response.body(), QrCodePinAuthenticationMethod.class);
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        else {
            App.outputArea.append("Error getting QRCode response.");
        }
           
        return newMethod;
    }
    
}

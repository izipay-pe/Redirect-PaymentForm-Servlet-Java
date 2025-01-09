package com.example;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.io.UnsupportedEncodingException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.TreeMap;
import java.util.Date;
import java.text.SimpleDateFormat;

public class McwController {
    
    private McwProperties properties = new McwProperties();

    // Método para generar un orderNumer basado en la hora
    public String generateOrderId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Order-'yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }

    // Método para obtener datos para el formulario
    public Map<String, String> dataForm(Map<String, String> parameters) {
        
	// Obteniendo claves API
	String shop_id = properties.getProperty("SHOP_ID");
	
	// Convirtiendo el valor del amount
	String amountStr = parameters.get("amount");
	BigDecimal amountUnit = new BigDecimal(amountStr);
	BigDecimal amountCent = amountUnit.multiply(BigDecimal.valueOf(100));
	long amount = amountCent.longValue();

    	Map<String, String> newParams = new TreeMap<>();

    	// Definir los parámetros vads_ y sus valores
    	newParams.put("vads_action_mode", "INTERACTIVE");
    	newParams.put("vads_ctx_mode", "TEST");  // TEST O PRODUCTION
    	newParams.put("vads_page_action", "PAYMENT");
    	newParams.put("vads_payment_config", "SINGLE");
    	newParams.put("vads_url_success", "http://127.0.0.1:8080/Redirect-PaymentForm-Servlet-Java/result");
    	newParams.put("vads_return_mode", "POST");
    	newParams.put("vads_site_id", shop_id); // ID de tienda
							
    	newParams.put("vads_cust_first_name", parameters.get("firstName"));
    	newParams.put("vads_cust_last_name", parameters.get("lastName"));
    	newParams.put("vads_cust_email", parameters.get("email"));
    	newParams.put("vads_cust_cell_phone", parameters.get("phoneNumber"));
    	newParams.put("vads_cust_address", parameters.get("address"));
    	newParams.put("vads_cust_country", parameters.get("country"));
    	newParams.put("vads_cust_state", parameters.get("state"));
    	newParams.put("vads_cust_city", parameters.get("city"));
    	newParams.put("vads_cust_zip", parameters.get("zipCode"));
    	newParams.put("vads_order_id", parameters.get("orderId"));
    	newParams.put("vads_amount", String.valueOf(amount));
    	newParams.put("vads_currency", parameters.get("currency"));
	newParams.put("vads_cust_national_id", parameters.get("identityCode"));	

        SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	utcDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
	String transDate = utcDateFormat.format(new Date());
	newParams.put("vads_trans_date", transDate);	
    		
    	String transId = Integer.toHexString((int) System.currentTimeMillis()).substring(0, 6);
    	newParams.put("vads_trans_id", transId);
	
    	newParams.put("vads_version", "V2");
    	newParams.put("vads_redirect_success_timeout", "5");  // Tiempo de redirección
	
	// Calcula el signature con los datos del Map
	String signature = calculateSignature(newParams);

	// Agrega el signature calulado al Map
	newParams.put("signature", signature);

	// Retorna el Map
	return newParams;
    }

    // Método para calcular el signature
    public String calculateSignature(Map<String, String> parameters) {
	Map<String, String> sortedParams = new TreeMap<>(parameters);	
	
	// Obtener la Key
	String key =  properties.getProperty("KEY");
	// Crear un StringBuilder para construir el contenido de la firma.
	StringBuilder contentSignature = new StringBuilder();
	
	// Ordena los parametros e intera sobre los mismos
    	for (Map.Entry<String, String> entry : sortedParams.entrySet()) {

        	String paramKey = entry.getKey();
        	String value = entry.getValue();

        	// Verificar si el nombre del parámetro comienza con 'vads_'
        	if (paramKey.startsWith("vads_")) {
			// Agregar el valor del parámetro seguido de un "+" al contenido
            		contentSignature.append(value).append("+");
       		}
    	}
	
	// Agregar la key al final del contenido
	contentSignature.append(key);
	
	// Generar y retornar la firma 
	return HmacSha256(contentSignature.toString(), key);
    } 
    
    // Generar un hash HMAC-SHA256
    public String HmacSha256(String data, String key) {
    try {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
        e.printStackTrace();
        throw new RuntimeException("Error generando HMAC SHA256", e);
    	}
    }


    public boolean checkSignature(Map<String, String> parameters){
    	// Obtener el signature de la respuesta
    	String signature = parameters.get("signature");
	
	return signature.equals(calculateSignature(parameters));
	
    }

}

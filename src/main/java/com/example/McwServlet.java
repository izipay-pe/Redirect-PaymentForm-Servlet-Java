package com.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import java.io.BufferedReader;
import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@WebServlet({"/", "/checkout", "/result"})
public class McwServlet extends HttpServlet {
    
    // Componentes principales para la plantilla, las propiedades y el controlador
    private TemplateEngine templateEngine;
    private McwProperties properties;
    private McwController mcwController;

    @Override
    public void init() throws ServletException {
	// Configuración para las plantillas Thymeleaf
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
	templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setTemplateMode("HTML");
	
	// Iniciando Thymeleaf
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);

	// Iniciando las propiedades y el controlador
	properties = new McwProperties();
	mcwController = new McwController();
    }

    /**
     * @@ Manejo de rutas GET @@
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
	// Obtiene la ruta solicitada
        String path = request.getServletPath(); 
	// Creación del contexto para pasar los valores para la plantilla
	WebContext context = new WebContext(request, response, getServletContext());
	
	// Generar orderId
	String orderId = mcwController.generarOrderId();
        
	switch (path) {
	    // Renderiza la plantilla 'index' al solicitar la ruta raíz, checkout y result
            case "/":
		// Agregando el orderId al contexto
		context.setVariable("orderId", orderId);
		// Renderizando el template y enviando los datos agregados al contexto
                templateEngine.process("index", context, response.getWriter());
                break;
            case "/checkout":
                templateEngine.process("index", context, response.getWriter());
                break;
            case "/result":
                templateEngine.process("index", context, response.getWriter());
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }
    
    /**
     * @@ Manejo de rutas POST @@
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
	// Obtiene la ruta solicitada
        String path = request.getServletPath();
	// Creación del contexto para pasar los valores para la plantilla
        WebContext context = new WebContext(request, response, getServletContext());
	
	// Definición de las variables a usar
	String krHash = null;
    	String krHashAlgorithm = null;
    	String krAnswerType = null;
    	String krAnswer = null;
    	String krHashKey = null;
	String currency = null;
	String currencyType = null;

        switch (path) {
            case "/checkout":
		// Procesando datos POST enviados de la ruta raíz y almacenándolos en un Map	
		Map<String, String> parameters = new HashMap<>();
            	for (String param : new String[]{"firstName", "lastName", "email", "phoneNumber", "identityType", 
                                              "identityCode", "address", "country", "state", "city", "zipCode", 
                                              "orderId", "amount", "currency"}) {
                	parameters.put(param, request.getParameter(param));
            	}

		// Calcular el Signature y los valores dinámicos para el formulario		
		Map<String, String> result = mcwController.dataForm(parameters);
		
		// Obtener el valor de la moneda
		currency = parameters.get("currency");
		
		// Asignar el tipo de moneda correspondiente
		if ("604".equals(currency)){
			currencyType = "Soles";
		} else {
			currencyType = "Dólares";
		}
		
		// Agrerar valores para ser usado en el template
		context.setVariable("parameters", result);
		context.setVariable("amount", parameters.get("amount"));
		context.setVariable("currency", currencyType);
		templateEngine.process("checkout", context, response.getWriter());	

		break;		

            case "/result":
	         // Procesando datos POST enviados de la respuesta de Izipay y almacenándolos en un Map	
		 Map<String, String> resultParameters = request.getParameterMap().entrySet().stream()
        	.filter(entry -> entry.getValue().length > 0)
        	.collect(Collectors.toMap(
            		Map.Entry::getKey,
            		entry -> entry.getValue()[0]
        	));
		
		// Almacenar el signature de la respuesta
		String resultPostSignature = resultParameters.get("signature");
		// Calcular el valor del signature
		String resultSignature = mcwController.calcularSignature(resultParameters);
		
		
		currency = resultParameters.get("vads_currency");

		if("604".equals(currency)) {
			currencyType = "PEN";
		} else {
			currencyType = "USD";
		}
		
		// Procesa la condicional si el signature calculado con el que recibimos son iguales
		if (resultSignature.equals(resultPostSignature)) {
			// Almacena algunos datos de la respuesta en variables
			String orderTotalAmount = resultParameters.get("vads_amount");
			double orderAmountdouble = Double.parseDouble(orderTotalAmount) / 100;
    			String orderAmount = String.format("%.02f", orderAmountdouble);

			// Agrega los datos al contexto
			context.setVariable("amount", orderAmount);
			context.setVariable("parameters", resultParameters);
			context.setVariable("currency", currencyType);

			// Renderiza el template y enviando los datos agregados al contexto
			templateEngine.process("result", context, response.getWriter());
		}

		break;
	    
	    case "/ipn":
		// Asignando los valores de la respuesta IPN en un Map
		Map<String, String> ipnParameters = request.getParameterMap().entrySet().stream()
        	.filter(entry -> entry.getValue().length > 0)
        	.collect(Collectors.toMap(
            		Map.Entry::getKey,
            		entry -> entry.getValue()[0]
        	));
		
		// Almacenar el signature de la respuesta IPN 
		String ipnPostSignature = ipnParameters.get("signature");
		// Calcular el valor del signature
		String ipnSignature = mcwController.calcularSignature(ipnParameters);
		
		// Almacena algunos datos de la respuesta IPN en variables
		String orderStatus = ipnParameters.get("vads_trans_status");
		String orderId = ipnParameters.get("vads_order_id");
		String uuid = ipnParameters.get("vads_trans_uuid");
		
		// Procesa la condicional si el signature calculado con el que recibimos en la IPN son iguales
		if (ipnSignature.equals(ipnPostSignature)) {
			// Imprimiendo en el log el Order Status
			System.out.println("OK! Order Status is " + orderStatus);

		} else {
			System.out.println("Notification Error");
		}

		break;
	    
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }
}


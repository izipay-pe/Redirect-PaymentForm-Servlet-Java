<p align="center">
  <img src="https://github.com/izipay-pe/Imagenes/blob/main/logos_izipay/logo-izipay-banner-1140x100.png?raw=true" alt="Formulario" width=100%/>
</p>

# Redirect-PaymentForm-Servlet-Java

## Índice

➡️ [1. Introducción](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#%EF%B8%8F-1-introducci%C3%B3n)  
🔑 [2. Requisitos previos](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#-2-requisitos-previos)  
🚀 [3. Ejecutar ejemplo](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#-3-ejecutar-ejemplo)  
🔗 [4. Pasos de integración](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#4-pasos-de-integraci%C3%B3n)  
💻 [4.1. Desplegar pasarela](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#41-desplegar-pasarela)  
💳 [4.2. Analizar resultado de pago](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#42-analizar-resultado-del-pago)  
📡 [4.3. Pase a producción](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#43pase-a-producci%C3%B3n)  
🎨 [5. Personalización](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#-5-personalizaci%C3%B3n)  
📚 [6. Consideraciones](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#-6-consideraciones)

## ➡️ 1. Introducción

En este manual podrás encontrar una guía paso a paso para configurar un proyecto de **[JAVA]** con la pasarela de pagos de IZIPAY. Te proporcionaremos instrucciones detalladas y credenciales de prueba para la instalación y configuración del proyecto, permitiéndote trabajar y experimentar de manera segura en tu propio entorno local.
Este manual está diseñado para ayudarte a comprender el flujo de la integración de la pasarela para ayudarte a aprovechar al máximo tu proyecto y facilitar tu experiencia de desarrollo.


<p align="center">
  <img src="https://github.com/izipay-pe/Imagenes/blob/main/formulario_redireccion/Imagen-Formulario-Redireccion.png?raw=true" alt="Formulario" width="750"/>
</p>

## 🔑 2. Requisitos Previos

- Comprender el flujo de comunicación de la pasarela. [Información Aquí](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/guide/start.html)
- Extraer credenciales del Back Office Vendedor. [Guía Aquí](https://github.com/izipay-pe/obtener-credenciales-de-conexion)
- Para este proyecto utilizamos Apache Tomcat 9.
- Apache Maven 3.9.9
- Java 17 o superior
> [!NOTE]
> Tener en cuenta que, para que el desarrollo de tu proyecto, eres libre de emplear tus herramientas preferidas.

## 🚀 3. Ejecutar ejemplo

### Instalar Apache Tomcat 9

Apache Tomcat es una implementación libre y de código abierto de las tecnologías Jakarta Servlet, Jakarta Expression Language y WebSocket.

1. Dirigirse a la página web de [Apache Tomcat](https://tomcat.apache.org/download-90.cgi).
2. Descargarlo e instalarlo.
3. Inicia los servicios de Apache Tomcart.


### Clonar el proyecto
```sh
git clone https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/
``` 

### Datos de conexión 

Reemplace **[CHANGE_ME]** con sus credenciales de `API REST` extraídas desde el Back Office Vendedor, revisar [Requisitos previos](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#-2-requisitos-previos).

- Editar el archivo `config.properties` en la ruta `src/main/resources/`:
```java
# Archivo para la configuración de las crendeciales de comercio
#
# Identificador de su tienda
merchantCode=CHANGE_ME_USER_ID
# Clave de Test o Producción
key=CHANGE_ME_USER_ID
```

1. Compilar el proyecto usando Maven

```sh
mvn package 
``` 

2.  Mover el archivo `Embedded-PaymentForm-Java.war` creado en la carpeta `/target` a la ruta `/webapps` de Apache Tomcat: `/opt/tomcat/webapps(Linux)` - `C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps(Windows)`

3.  Abrir el navegador web(Chrome, Mozilla, Safari, etc) con el puerto 8080 que abrió Tomcat : `http://127.0.0.1:8080/Redirect-PaymentForm-Servlet-Java/` y realizar una compra de prueba.


## 🔗4. Pasos de integración

<p align="center">
  <img src="https://i.postimg.cc/pT6SRjxZ/3-pasos.png" alt="Formulario" />
</p>

## 💻4.1. Desplegar pasarela
### Autentificación
Extraer las claves de `identificador de tienda` y `clave de test o producción` del Backoffice Vendedor y agregarlo en los parámetros `vads_site_id` y en método `calcularSignature(Map<String, String> parameters)`. Este último permite calcular la firma transmitida de los datos de pago. Podrás encontrarlo en el archivo `src/main/java/com/example/McwController.java`.
```java
public Map<String, String> dataForm(Map<String, String> parameters) {

  // Obteniendo claves API
	String merchantCode = properties.getProperty("merchantCode");

  // Definir los parámetros vads_ y sus valores
  newParams.put("vads_action_mode", "INTERACTIVE");
  ...
  ...
  newParams.put("vads_site_id", merchantCode); // ID de tienda
  	
	// Calcula el signature con los datos del Map
	String signature = calcularSignature(newParams);

	// Agrega el signature calulado al Map
	newParams.put("signature", signature);

	// Retorna el Map
	return newParams;
  }

public String calcularSignature(Map<String, String> parameters) {
	Map<String, String> sortedParams = new TreeMap<>(parameters);	
	
	// Obtener la clave de test o producción
	String key =  properties.getProperty("key");
	...
  ...
	contentSignature.append(key);
	
	// Generar y retornar la firma 
	return HmacSha256(contentSignature.toString(), key);

} 
```

ℹ️ Para más información: [Autentificación](https://secure.micuentaweb.pe/doc/es-PE/form-payment/quick-start-guide/identificarse-durante-los-intercambios.html)
### Visualizar formulario
Para desplegar la pasarela, crea un formulario **HTML** de tipo **POST** con el valor del **ACTION** con la url de servidor de la pasarela de pago y agregale los parámetros de pago como etiquetas `<input type="hidden" th:name="..." th:value="...">`. Como se muestra el ejemplo en la ruta del archivo `src/main/webapp/WEB-INF/templates/checkout.html` 

```html
          <!-- Formulario con los datos de pago -->
          <form class="from-checkout" action="https://secure.micuentaweb.pe/vads-payment/" method="post">
  		<!-- Inputs generados dinámicamente -->
		<input type="hidden" th:name="vads_action_mode" th:value="${parameters.vads_action_mode}" />
  		...
		...
		<input type="hidden" th:name="signature" th:value="${parameters.signature}" />
		<button class="btn btn-checkout" type="submit" name="pagar">Pagar</button>
          </form>	
```
ℹ️ Para más información: [Formulario de pago en POST](https://secure.micuentaweb.pe/doc/es-PE/form-payment/quick-start-guide/enviar-un-formulario-de-pago-en-post.html)

## 💳4.2. Analizar resultado del pago

### Validación de firma
Se configura el método `calcularSignature(Map<String, String> parameters)` que generará la firma de los datos de la respuesta de pago. Podrás encontrarlo en el archivo `src/main/java/com/example/McwController.java`.

```java
// Método para calcular el signature
public String calcularSignature(Map<String, String> parameters) {
	Map<String, String> sortedParams = new TreeMap<>(parameters);
	
	// Obtener la clave de test o producción
	String key =  properties.getProperty("key");
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
```

Se valida que la firma recibida es correcta en el archivo `src/main/java/com/example/McwServlet.java`.

```java
case "/result":
     // Procesando datos POST enviados de la respuesta de Izipay y almacenándolos en un Map	
		 Map<String, String> resultParameters = request.getParameterMap().entrySet().stream()
     .filter(entry -> entry.getValue().length > 0)
     .collect(Collectors.toMap(
         Map.Entry::getKey,
         entry -> entry.getValue()[0]
      ));

		 ...
		 ...

		 // Calcular el valor del signature
		 String resultSignature = mcwController.calcularSignature(resultParameters);
		
		 ...
     		 ...

		 // Procesa la condicional si el signature calculado con el que recibimos son iguales
		 if (resultSignature.equals(resultPostSignature)) {
       			 ...
       			 ...
			 templateEngine.process("result", context, response.getWriter());
		 }

		break;
```
En caso que la validación sea exitosa, se renderiza el template con los valores. Como se muestra en el archivo `src/main/webapp/WEB-INF/templates/result.html`.

```html
<p><strong>Estado:</strong> <span th:text="${parameters.vads_trans_status}"></span></p>
<p><strong>Monto:</strong> <span th:text="${currency}"></span> <span th:text="${amount}"></span></p>
<p><strong>orderId:</strong> <span th:text="${parameters.vads_order_id}"></span></p>
```
ℹ️ Para más información: [Analizar resultado del pago](https://secure.micuentaweb.pe/doc/es-PE/form-payment/quick-start-guide/recuperar-los-datos-devueltos-en-la-respuesta.html)

### IPN
La IPN es una notificación de servidor a servidor (servidor de Izipay hacia el servidor del comercio) que facilita información en tiempo real y de manera automática cuando se produce un evento, por ejemplo, al registrar una transacción.

Se realiza la verificación de la firma y se imprime en el log el estado del pago. Podrás encontrarlo en el archivo `src/main/java/com/example/McwServlet.java`.

```java
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
		...
    ...
		
		// Procesa la condicional si el signature calculado con el que recibimos en la IPN son iguales
		if (ipnSignature.equals(ipnPostSignature)) {
			// Imprimiendo en el log el Order Status
			System.out.println("OK! Order Status is " + orderStatus);
    ...
    ...

		break;
```

La IPN debe ir configurada en el Backoffice Vendedor, en `Configuración -> Reglas de notificación -> URL de notificación al final del pago`

<p align="center">
  <img src="https://github.com/izipay-pe/Imagenes/blob/main/formulario_redireccion/Url-Notificacion-Redireccion.png?raw=true" alt="Url de notificacion en redireccion" width="650" />
</p>

ℹ️ Para más información: [Analizar IPN](https://secure.micuentaweb.pe/doc/es-PE/form-payment/quick-start-guide/implementar-la-ipn.html)

## 5. Transacción de prueba

Antes de poner en marcha su pasarela de pago en un entorno de producción, es esencial realizar pruebas para garantizar su correcto funcionamiento. 

Puede intentar realizar una transacción utilizando una tarjeta de prueba (en la parte inferior del formulario).

<p align="center">
  <img src="https://github.com/izipay-pe/Imagenes/blob/main/formulario_redireccion/Imagen-Formulario-Redireccion-testcard.png?raw=true" alt="Tarjetas de prueba" width="450"/>
</p>

- También puede encontrar tarjetas de prueba en el siguiente enlace. [Tarjetas de prueba](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/api/kb/test_cards.html)

## 📡4.3.Pase a producción

Reemplace **[CHANGE_ME]** con sus credenciales de PRODUCCIÓN extraídas desde el Back Office Vendedor, revisar [Requisitos Previos](https://github.com/izipay-pe/Redirect-PaymentForm-Servlet-Java/tree/main?tab=readme-ov-file#-2-requisitos-previos).

- Editar el archivo `config.properties` en la ruta `src/main/resources/`:
```java
# Archivo para la configuración de las crendeciales de comercio
#
# Identificador de su tienda
merchantCode=CHANGE_ME_USER_ID
# Clave de Test o Producción
key=CHANGE_ME_USER_ID
```

## 🎨 5. Personalización

Si deseas aplicar cambios específicos en la apariencia de la página de pago, puedes lograrlo mediante las opciones de personalización en el Backoffice. En este enlace [Personalización - Página de pago](https://youtu.be/hy877zTjpS0?si=TgSeoqw7qiaQDV25) podrá encontrar un video para guiarlo en la personalización.

<p align="center">
  <img src="https://github.com/izipay-pe/Imagenes/blob/main/formulario_redireccion/Personalizacion-formulario-redireccion.png?raw=true" alt="Personalizacion de formulario en redireccion"  width="750" />
</p>

## 📚 6. Consideraciones

Para obtener más información, echa un vistazo a:

- [Formulario incrustado: prueba rápida](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/quick_start_js.html)
- [Primeros pasos: pago simple](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/guide/start.html)
- [Servicios web - referencia de la API REST](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/api/reference.html)

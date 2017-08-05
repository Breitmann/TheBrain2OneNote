
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;


/*
 * https://msdn.microsoft.com/en-us/office/office365/howto/onenote-landing
 * https://msdn.microsoft.com/en-us/library/hh826543.aspx#rest
 * 
 * https://apps.dev.microsoft.com/#/appList
 */

public class TheBrain2OneNote {

	public static String ACCESS_TOKEN = System.getenv("access_token" );
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	
	public static void main(String[] args) 		throws Exception {

		System.out.println("Starting");
		
		System.out.println(ACCESS_TOKEN);
		listNotebooks();
		
		//createNotebook("Test4");
		
		//createSection("0-F13DBFDBA2CC865A!8007", "Section1");
		
		createPage("0-F13DBFDBA2CC865A!8011", "Title1");
		
		System.out.println("Finished");
	}


	private static void createPage(String sectionId, String pageTitle )
			throws Exception
	{
        GenericUrl url = new GenericUrl("https://www.onenote.com/api/v1.0/me/notes/sections/" + sectionId + "/pages" );
        System.out.println(url);
        String requestBody = "<html><head><title>"+ pageTitle +"</title></head><body>Test</body></html>";
		HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(url, ByteArrayContent.fromString("text/html", requestBody));
        request.getHeaders().setContentType("text/html");
		request.getHeaders().setAuthorization("Bearer " + ACCESS_TOKEN);
        HttpResponse response = request.execute();
		
        System.out.println(response.getStatusCode());
        
        System.out.println(response.parseAsString() );	
	}
	
	
	
	private static void createSection(String notebookId, String sectionName )
			throws Exception
	{
        GenericUrl url = new GenericUrl("https://www.onenote.com/api/v1.0/me/notes/notebooks/" + notebookId + "/sections" );
        System.out.println(url);
        String requestBody = "{'name': '" + sectionName + "'}";
		HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(url, ByteArrayContent.fromString("application/json", requestBody));
        request.getHeaders().setContentType("application/json");
		request.getHeaders().setAuthorization("Bearer " + ACCESS_TOKEN);
        HttpResponse response = request.execute();
		
        System.out.println(response.getStatusCode());
        
        System.out.println(response.parseAsString() );	
	}
	
	private static void createNotebook(String name)
		throws Exception
	{
        GenericUrl url = new GenericUrl("https://www.onenote.com/api/v1.0/me/notes/notebooks");
        String requestBody = "{'name': '" + name + "'}";
		HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(url, ByteArrayContent.fromString("application/json", requestBody));
        request.getHeaders().setContentType("application/json");
		request.getHeaders().setAuthorization("Bearer " + ACCESS_TOKEN);
        HttpResponse response = request.execute();
		
        System.out.println(response.getStatusCode());
        
        System.out.println(response.parseAsString() );	
	}
	
	private static void listNotebooks()
			throws Exception
	{
        GenericUrl url = new GenericUrl("https://www.onenote.com/api/v1.0/me/notes/notebooks");
        HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildGetRequest(url);
        HttpHeaders headers = new HttpHeaders();
        headers.setAuthorization("Bearer " + ACCESS_TOKEN);
        request.setHeaders(headers);
        HttpResponse response = request.execute();
        System.out.println(response.getStatusCode());
        
        String responseString = response.parseAsString();
		JSONObject document = new JSONObject(responseString);
		
		JSONArray notebooks = document.getJSONArray("value");
		
		for( int i = 0; i < notebooks.length(); i++ )
		{
			JSONObject notebook = notebooks.getJSONObject(i);
			
			System.out.println(notebook.get("name"));
		}

		response.disconnect();	
	}

}

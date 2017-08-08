
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.MultipartContent;
import com.google.api.client.http.javanet.NetHttpTransport;



/*
 * https://msdn.microsoft.com/en-us/office/office365/howto/onenote-landing
 * https://msdn.microsoft.com/en-us/library/hh826543.aspx#rest
 * 
 * https://apps.dev.microsoft.com/#/appList
 */

public class TheBrain2OneNote {

	static String ACCESS_TOKEN = System.getenv("access_token" );
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static String BRZ_PATH = System.getenv("brz_path");
    
    static Map<String, JSONObject> thoughts = new HashMap<String, JSONObject>();
	
	public static void main(String[] args) 		throws Exception {

		System.out.println("Starting");
		
		System.out.println(ACCESS_TOKEN);
		// listNotebooks();
		
		//createNotebook("Test4");
		
		//createSection("0-F13DBFDBA2CC865A!8007", "Section1");
		
		// createPage("0-F13DBFDBA2CC865A!8011", "Title1");
		
		
		processBRZ();
		
		System.out.println("Finished");
	}


	private static void processBRZ()
		throws Exception
	{
		
		// read thoughts
		BufferedReader br = new BufferedReader(new FileReader(new File(BRZ_PATH + File.separator + "thoughts.json")));
		while( true )
		{
			String line = br.readLine();
			if( line == null ) break;
			
			int charCode = line.charAt(0);
			
			if( charCode == 65279  ) line = line.substring(1);

			JSONObject thought = new JSONObject(line);
			//System.out.println(thought.get("Name"));
			
			thoughts.put(thought.getString("Id"), thought);
		}
		
		br.close();
		
		// 0b541b81-25db-42a6-aa45-835e7388dd6a
		createThoughtInOneNote(thoughts.get("0b541b81-25db-42a6-aa45-835e7388dd6a") );
		
	}
	
	private static void createThoughtInOneNote(JSONObject thought)
		throws Exception
	{
		String thoughtId = thought.getString("Id");
		String path = BRZ_PATH + File.separator + thoughtId + File.separator + "Notes" + File.separator + "notes.html";
		String htmlContent = readFile( path, Charset.defaultCharset() );

		String filePath = BRZ_PATH + File.separator + thoughtId + File.separator + "Notes" + File.separator + "992e3dc7-61bc-4e7d-9e78-f680ffe13ec2.png";		
		
		createPageMultipart("0-F13DBFDBA2CC865A!8011", thought.getString("Name"), htmlContent, filePath);
	}
	
	private static void createPageMultipart(String sectionId, String pageTitle, String htmlContent, String...paths )
			throws Exception
	{
        GenericUrl url = new GenericUrl("https://www.onenote.com/api/v1.0/me/notes/sections/" + sectionId + "/pages" );
        System.out.println(url);
        String requestBody = "\n<!DOCTYPE html>\n<html><head><title>"+ pageTitle +"</title></head><body>" + htmlContent + "</body></html>";

        MultipartContent content = new MultipartContent().setMediaType(
                new HttpMediaType("multipart/form-data")
                        .setParameter("boundary", "__END_OF_PART__"));
        
        
        MultipartContent.Part part1 = new MultipartContent.Part(ByteArrayContent.fromString("text/html", requestBody));
        part1.setHeaders(new HttpHeaders().set(
                "Content-Disposition", String.format("form-data; name=\"%s\"", "Presentation")));
        part1.getHeaders().setContentType("text/html");
        part1.getHeaders().setAcceptEncoding(null);
        content.addPart(part1);
        
        File file1 = new File(paths[0]);
        FileContent fileContent = new FileContent(
                "image/png", file1 );
        MultipartContent.Part part2 = new MultipartContent.Part(fileContent);
        part2.setHeaders(new HttpHeaders().set(
                "Content-Disposition", 
                String.format("form-data; name=\"content\"; filename=\"%s\"", file1.getName())));
        part2.getHeaders().setAcceptEncoding(null);        
        content.addPart(part2);        

        //content.writeTo(System.out);
        
        HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(url, content);
        
        request.getHeaders().setContentType("multipart/form-data; boundary=__END_OF_PART__");
		request.getHeaders().setAuthorization("Bearer " + ACCESS_TOKEN);
        
		HttpResponse response = request.execute();
		
        System.out.println(response.getStatusCode());
        
        System.out.println(response.parseAsString() );	
	}	
	
	private static void createPage(String sectionId, String pageTitle, String htmlContent )
			throws Exception
	{
        GenericUrl url = new GenericUrl("https://www.onenote.com/api/v1.0/me/notes/sections/" + sectionId + "/pages" );
        System.out.println(url);
        String requestBody = "<html><head><title>"+ pageTitle +"</title></head><body>" + htmlContent + "</body></html>";
		HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(url, ByteArrayContent.fromString("text/html", requestBody));
        request.getHeaders().setContentType("text/html");
		request.getHeaders().setAuthorization("Bearer " + ACCESS_TOKEN);
        HttpResponse response = request.execute();
		
        System.out.println(response.getStatusCode());
        
        System.out.println(response.parseAsString() );	
	}
	
	static String readFile(String path, Charset encoding) 
			  throws IOException 
	{
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded, encoding);
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

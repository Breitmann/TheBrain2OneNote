import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpEncoding;
import com.google.api.client.http.HttpEncodingStreamingContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.MultipartContent;
import com.google.api.client.http.MultipartContent.Part;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StreamingContent;

public class MyMultipartContent extends AbstractHttpContent{

	  static final String NEWLINE = "\r\n";

	  private static final String TWO_DASHES = "--";

	  /** Parts of the HTTP multipart request. */
	  private ArrayList<Part> parts = new ArrayList<Part>();

	  public MyMultipartContent() {
		    super(new HttpMediaType("multipart/related").setParameter("boundary", "__END_OF_PART__"));
		  }
/*
	  public void writeTo(OutputStream out) throws IOException {
		    Writer writer = new OutputStreamWriter(out, getCharset());
		    
		    writer.write("--__END_OF_PART__");
		    writer.write(NEWLINE);
//		    		writer.write("Accept-Encoding: gzip");
//				    writer.write(NEWLINE);
		    		writer.write("Content-type:text/html");
				    writer.write(NEWLINE);
		    		writer.write("content-disposition:form-data; name=\"Presentation\"");
				    writer.write(NEWLINE);
		    		writer.write("content-transfer-encoding: binary");
				    writer.write(NEWLINE);
		    		writer.write("");
				    writer.write(NEWLINE);
				    writer.write(NEWLINE);
				    
				    writer.write( "\n<!DOCTYPE html>\n<html><head><title>"+ "xxx" +"</title></head><body>" + "test" + "</body></html>" );
//		    		writer.write("<html>");
//				    writer.write(NEWLINE);
//		    		writer.write("  <head>");
//				    writer.write(NEWLINE);
//		    		writer.write("    <title>Title of the captured OneNote page</title>");
//				    writer.write(NEWLINE);
//		    		writer.write("    <meta name=\"created\" content=\"2013-06-11T12:45:00.000-8:00\"/>");
//				    writer.write(NEWLINE);
//		    		writer.write("  </head>");
//				    writer.write(NEWLINE);
//		    		writer.write("  <body>");
//				    writer.write(NEWLINE);
//		    		writer.write("    <p>This is a simple Presentation block.</p>");
//				    writer.write(NEWLINE);
//		    		writer.write("  </body>");
//				    writer.write(NEWLINE);
//		    		writer.write("</html>");
				    writer.write(NEWLINE);
		    		writer.write("");
				    writer.write(NEWLINE);
		    		writer.write("--__END_OF_PART__--");	    
		    writer.write(NEWLINE);
		    writer.flush();
	  }
*/	  
	 /* 
	  public void writeTo(OutputStream out) throws IOException {
		    Writer writer = new OutputStreamWriter(out, getCharset());
		    
		    writer.write("--__END_OF_PART__");
		    writer.write(NEWLINE);
		    		writer.write("Content-Disposition:form-data; name=\"Presentation\"");
				    writer.write(NEWLINE);
		    		writer.write("Content-type:text/html");
				    writer.write(NEWLINE);
		    		writer.write("");
				    writer.write(NEWLINE);
				    
				    writer.write( "\n<!DOCTYPE html>\n<html><head><title>"+ "xxx" +"</title></head><body>" + "test" + "</body></html>" );
//		    		writer.write("<html>");
//				    writer.write(NEWLINE);
//		    		writer.write("  <head>");
//				    writer.write(NEWLINE);
//		    		writer.write("    <title>Title of the captured OneNote page</title>");
//				    writer.write(NEWLINE);
//		    		writer.write("    <meta name=\"created\" content=\"2013-06-11T12:45:00.000-8:00\"/>");
//				    writer.write(NEWLINE);
//		    		writer.write("  </head>");
//				    writer.write(NEWLINE);
//		    		writer.write("  <body>");
//				    writer.write(NEWLINE);
//		    		writer.write("    <p>This is a simple Presentation block.</p>");
//				    writer.write(NEWLINE);
//		    		writer.write("  </body>");
//				    writer.write(NEWLINE);
//		    		writer.write("</html>");
				    writer.write(NEWLINE);
		    		writer.write("");
				    writer.write(NEWLINE);
		    		writer.write("--__END_OF_PART__--");	    
		    writer.write(NEWLINE);
		    writer.flush();
	  }
	  */
	  
	  
	  public void writeTo(OutputStream out) throws IOException {
	    Writer writer = new OutputStreamWriter(out, getCharset());
	    String boundary = getBoundary();
	    for (Part part : parts) {
	      HttpHeaders headers = new HttpHeaders().setAcceptEncoding(null);
	      if (part.headers != null) {
	        headers.fromHttpHeaders(part.headers);
	      }
	      headers.setContentEncoding(null)
	          .setUserAgent(null)
	          .setContentType(null)
	          .setContentLength(null)
	          .set("Content-Transfer-Encoding", null);
	      // analyze the content
	      HttpContent content = part.content;
	      StreamingContent streamingContent = null;
	      if (content != null) {
	        headers.set("Content-Transfer-Encoding", Arrays.asList("binary"));
	        headers.setContentType(content.getType());
	        HttpEncoding encoding = part.encoding;
	        long contentLength;
	        if (encoding == null) {
	          contentLength = content.getLength();
	          streamingContent = content;
	        } else {
	          headers.setContentEncoding(encoding.getName());
	          streamingContent = new HttpEncodingStreamingContent(content, encoding);
	          contentLength = AbstractHttpContent.computeLength(content);
	        }
	        if (contentLength != -1) {
	          headers.setContentLength(contentLength);
	        }
	      }
	      // write multipart-body from RFC 1521 §7.2.1
	      // write encapsulation
	      // write delimiter
	      writer.write(TWO_DASHES);
	      writer.write(boundary);
	      writer.write(NEWLINE);
	      // write body-part; message from RFC 822 §4.1
	      // write message fields
	      HttpHeaders.serializeHeadersForMultipartRequests(headers, null, null, writer);
	      if (streamingContent != null) {
	        writer.write(NEWLINE);
	        writer.flush();
	        // write message text/body
	        streamingContent.writeTo(out);
	      }
	      // terminate encapsulation
	      writer.write(NEWLINE);
	    }
	    // write close-delimiter
	    writer.write(TWO_DASHES);
	    writer.write(boundary);
	    writer.write(TWO_DASHES);
	    writer.write(NEWLINE);
	    writer.flush();
	  }

	  @Override
	  public boolean retrySupported() {
	    for (Part part : parts) {
	      if (!part.content.retrySupported()) {
	        return false;
	      }
	    }
	    return true;
	  }

	  @Override
	  public MyMultipartContent setMediaType(HttpMediaType mediaType) {
	    super.setMediaType(mediaType);
	    return this;
	  }

	  /** Returns an unmodifiable view of the parts of the HTTP multipart request. */
	  public final Collection<Part> getParts() {
	    return Collections.unmodifiableCollection(parts);
	  }

	  /**
	   * Adds an HTTP multipart part.
	   *
	   * <p>
	   * Overriding is only supported for the purpose of calling the super implementation and changing
	   * the return type, but nothing else.
	   * </p>
	   */
	  public MyMultipartContent addPart(Part part) {
	    parts.add(Preconditions.checkNotNull(part));
	    return this;
	  }

	  /**
	   * Sets the parts of the HTTP multipart request.
	   *
	   * <p>
	   * Overriding is only supported for the purpose of calling the super implementation and changing
	   * the return type, but nothing else.
	   * </p>
	   */
	  public MyMultipartContent setParts(Collection<Part> parts) {
	    this.parts = new ArrayList<Part>(parts);
	    return this;
	  }

	  /**
	   * Sets the HTTP content parts of the HTTP multipart request, where each part is assumed to have
	   * no HTTP headers and no encoding.
	   *
	   * <p>
	   * Overriding is only supported for the purpose of calling the super implementation and changing
	   * the return type, but nothing else.
	   * </p>
	   */
	  public MyMultipartContent setContentParts(Collection<? extends HttpContent> contentParts) {
	    this.parts = new ArrayList<Part>(contentParts.size());
	    for (HttpContent contentPart : contentParts) {
	      addPart(new Part(contentPart));
	    }
	    return this;
	  }

	  /** Returns the boundary string to use. */
	  public final String getBoundary() {
	    return getMediaType().getParameter("boundary");
	  }

	  /**
	   * Sets the boundary string to use.
	   *
	   * <p>
	   * Defaults to {@code "END_OF_PART"}.
	   * </p>
	   *
	   * <p>
	   * Overriding is only supported for the purpose of calling the super implementation and changing
	   * the return type, but nothing else.
	   * </p>
	   */
	  public MyMultipartContent setBoundary(String boundary) {
	    getMediaType().setParameter("boundary", Preconditions.checkNotNull(boundary));
	    return this;
	  }

	  /**
	   * Single part of a multi-part request.
	   *
	   * <p>
	   * Implementation is not thread-safe.
	   * </p>
	   */
	  public static final class Part {

	    /** HTTP content or {@code null} for none. */
	    HttpContent content;

	    /** HTTP headers or {@code null} for none. */
	    HttpHeaders headers;

	    /** HTTP encoding or {@code null} for none. */
	    HttpEncoding encoding;

	    public Part() {
	      this(null);
	    }

	    /**
	     * @param content HTTP content or {@code null} for none
	     */
	    public Part(HttpContent content) {
	      this(null, content);
	    }

	    /**
	     * @param headers HTTP headers or {@code null} for none
	     * @param content HTTP content or {@code null} for none
	     */
	    public Part(HttpHeaders headers, HttpContent content) {
	      setHeaders(headers);
	      setContent(content);
	    }

	    /** Sets the HTTP content or {@code null} for none. */
	    public Part setContent(HttpContent content) {
	      this.content = content;
	      return this;
	    }

	    /** Returns the HTTP content or {@code null} for none. */
	    public HttpContent getContent() {
	      return content;
	    }

	    /** Sets the HTTP headers or {@code null} for none. */
	    public Part setHeaders(HttpHeaders headers) {
	      this.headers = headers;
	      return this;
	    }

	    /** Returns the HTTP headers or {@code null} for none. */
	    public HttpHeaders getHeaders() {
	      return headers;
	    }

	    /** Sets the HTTP encoding or {@code null} for none. */
	    public Part setEncoding(HttpEncoding encoding) {
	      this.encoding = encoding;
	      return this;
	    }

	    /** Returns the HTTP encoding or {@code null} for none. */
	    public HttpEncoding getEncoding() {
	      return encoding;
	    }
	  
	  }
}

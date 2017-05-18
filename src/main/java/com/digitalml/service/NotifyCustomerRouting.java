package com.digitalml.service;

import static spark.Spark.*;
import spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Context.Builder;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;

import static net.logstash.logback.argument.StructuredArguments.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class NotifyCustomerRouting {

    private static final Logger logger = LoggerFactory.getLogger("notifycustomer:1");

    public static void main(String[] args) {
    
        port(4567);
    
        get("/ping", (req, res) -> {
            return "pong";
        });
        
        get("/halt", (request, response) -> {
			stop();
			response.status(202);
			return "";
		});
		
        // Handle timings
        
        Map<Object, Long> timings = new ConcurrentHashMap<>();
        
        before(new Filter() {
        	@Override
        	public void handle(Request request, Response response) throws Exception {
        		timings.put(request, System.nanoTime());
        	}
        });
        
        afterAfter(new Filter() {
        	@Override
        	public void handle(Request request, Response response) throws Exception {
        		long start = timings.remove(request);
        		long end =  System.nanoTime();
        		logger.info("log message {} {} {} {} ns", value("apiname", "notifycustomer"), value("apiversion", "1"), value("apipath", request.pathInfo()), value("response-timing", (end-start)));
        	}
        });
        
        get("/contact/:name", (req, res) -> {
        
			Handlebars handlebars = new Handlebars();
			Map<String, Map> content = new HashMap<>();
			List<HttpResponse<String>> responses = new ArrayList<>();

			Map inputs = new HashMap<>();
			for (Map.Entry<String, String> x : req.params().entrySet()) {
                inputs.put(x.getKey().substring(1), x.getValue());
            }
			for (Map.Entry<String, String[]> x : req.queryMap().toMap().entrySet()) {
				inputs.put(x.getKey(), x.getValue()[0]);
			}

            {
                // Call GetUser

                String callURL = "http://ignite-runtime:8000/test/userlookup/1/user/{name}";

                GetRequest call = Unirest.get(callURL);

    			

    			Map<String, Object> params = new HashMap<>();
    			Map<String, Object> routes = new HashMap<>();
    			
    			{
    			
    			    Template template = handlebars.compileInline("{"+"{"+"name"+"}"+"}");
    			
					Builder builder = Context.newBuilder(inputs).resolver(MapValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE);
					for (String key  : content.keySet()) {
    					builder = builder.combine(key, content.get(key));
					}

    			    String value = template.apply(builder.build());
    			
                    if (callURL.contains("{" + "name" + "}"))
                        routes.put("name", value);
                    else
                        params.put("name", value);
                }
    			{
                }
                
    			for (Map.Entry<String, Object> entry : routes.entrySet()) {
    				call.routeParam(entry.getKey(), String.valueOf(entry.getValue()));
    			}
    			
    			System.out.println("For call to " + callURL);
    			for (Map.Entry<String, Object> entry : routes.entrySet()) {
    				System.out.println("RouteParam " + entry.getKey() + "="+ String.valueOf(entry.getValue()));
    			}
    			for (Map.Entry<String, Object> entry : params.entrySet()) {
    				System.out.println("QueryParam " + entry.getKey() + "="+ String.valueOf(entry.getValue()));
    			}
    			

                
				HttpRequest singleCall = call.queryString(params);
    
        

				HttpResponse<String> response = singleCall.asString();

                try {				
				    content.put("a", new Gson().fromJson(response.getBody(), Map.class));
                } catch (JsonSyntaxException e) {}
                
    			responses.add(response);
    		}
            {
                // Call Send

                String callURL = "http://ignite-runtime:8000/test/sendtext/2/send";

                GetRequest call = Unirest.get(callURL);

    			

    			Map<String, Object> params = new HashMap<>();
    			Map<String, Object> routes = new HashMap<>();
    			
    			{
    			
    			    Template template = handlebars.compileInline("{{a.userDetails.phoneNumber}}");
    			
					Builder builder = Context.newBuilder(inputs).resolver(MapValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE);
					for (String key  : content.keySet()) {
    					builder = builder.combine(key, content.get(key));
					}

    			    String value = template.apply(builder.build());
    			
                    if (callURL.contains("{" + "PhoneNumber" + "}"))
                        routes.put("PhoneNumber", value);
                    else
                        params.put("PhoneNumber", value);
                }
    			{
    			
    			    Template template = handlebars.compileInline("Hello {{name}}");
    			
					Builder builder = Context.newBuilder(inputs).resolver(MapValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE);
					for (String key  : content.keySet()) {
    					builder = builder.combine(key, content.get(key));
					}

    			    String value = template.apply(builder.build());
    			
                    if (callURL.contains("{" + "Message" + "}"))
                        routes.put("Message", value);
                    else
                        params.put("Message", value);
                }
    			{
                }
                
    			for (Map.Entry<String, Object> entry : routes.entrySet()) {
    				call.routeParam(entry.getKey(), String.valueOf(entry.getValue()));
    			}
    			
    			System.out.println("For call to " + callURL);
    			for (Map.Entry<String, Object> entry : routes.entrySet()) {
    				System.out.println("RouteParam " + entry.getKey() + "="+ String.valueOf(entry.getValue()));
    			}
    			for (Map.Entry<String, Object> entry : params.entrySet()) {
    				System.out.println("QueryParam " + entry.getKey() + "="+ String.valueOf(entry.getValue()));
    			}
    			

                
				HttpRequest singleCall = call.queryString(params);
    
        

				HttpResponse<String> response = singleCall.asString();

                try {				
				    content.put("b", new Gson().fromJson(response.getBody(), Map.class));
                } catch (JsonSyntaxException e) {}
                
    			responses.add(response);
    		}

            if (responses.size()>0) {
            
                System.out.println(responses.get(0).getStatus());
                System.out.println(responses.get(0).getStatusText());
                System.out.println(responses.get(0).getBody());
            
			    return responses.get(0).getStatus() + " " + responses.get(0).getStatusText() + " " + responses.get(0).getBody();
            }

            StringBuffer sb = new StringBuffer();
            sb.append("name = " + req.params("name"));
            return "Contact " + sb.toString();
        });
    }
}
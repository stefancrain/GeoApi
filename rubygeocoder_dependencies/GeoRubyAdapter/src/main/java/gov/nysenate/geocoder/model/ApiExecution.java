package gov.nysenate.geocoder.model;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import gov.nysenate.geocoder.api.exceptions.ApiException;
import gov.nysenate.geocoder.api.exceptions.ApiFormatException;

public class ApiExecution implements AbstractApiExecute{

	@Override
	public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> strings) throws ApiException {
		return null;
	}

	@Override
	public String toJson(Object obj) {
		Gson gson = new Gson();
		return gson.toJson(obj);
	}

	@Override
	public String toXml(Object obj, ArrayList<Class<?>> xstreamClasses) {
		XStream xstream = new XStream(new DomDriver());
		for(Class<?> clazz:xstreamClasses) {
			xstream.processAnnotations(clazz);
		}
		return xstream.toXML(obj);
	}

	@Override
	public String toOther(Object obj, String format) throws ApiFormatException {
		return null;
	}
}
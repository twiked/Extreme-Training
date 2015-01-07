package fr.cpe.gae;

import java.io.IOException;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class Index extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
		String description = (String) cache.get("motd");
		if (description == null){
			DatastoreService store = DatastoreServiceFactory.getDatastoreService();
			Filter f = new FilterPredicate("type", Query.FilterOperator.EQUAL,"motd");
			Query q = new Query("Messages").setFilter(f);
            PreparedQuery pq = store.prepare(q);
            String message = (String) pq.asSingleEntity().getProperty("content");
            resp.getWriter().println(message);
            cache.put("motd", message);
		}
	}
}

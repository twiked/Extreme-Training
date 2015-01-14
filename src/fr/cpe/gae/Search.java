package fr.cpe.gae;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@SuppressWarnings("serial")
public class Search extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException {
		
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		
		
		String searchBar =(String)req.getParameter("searchBar");
		String domainSelection = (String)req.getParameter("domain");
		Filter fsearch = null;
		Filter fdomain = null;
		Query q1 = new Query("");
		
		if (searchBar != null){
			
			fsearch = new FilterPredicate("title", Query.FilterOperator.EQUAL,searchBar);
			if (domainSelection == null)
				q1 = new Query("Training").setFilter(fsearch);
			
		}
		if (domainSelection != null){
			
			Filter fk = new FilterPredicate("title", Query.FilterOperator.EQUAL,domainSelection);
			Query qk = new Query("Domain").setFilter(fk);
	        PreparedQuery pqk = store.prepare(qk);
	        Entity domainEntity = pqk.asSingleEntity();
	        
	        if (domainEntity != null){
	        	fdomain = new FilterPredicate("domain", Query.FilterOperator.EQUAL, KeyFactory.keyToString(domainEntity.getKey()));
	        	if (searchBar == null)
	        		q1 = new Query("Training").setFilter(fdomain);
	        }
	        
		}
		
		if (fsearch != null && fdomain != null){
			Filter fc = CompositeFilterOperator.and(fsearch, fdomain);
			q1 = new Query("Training").setFilter(fc);
		}
		
		
        PreparedQuery pq1 = store.prepare(q1);
        FetchOptions fo1 = FetchOptions.Builder.withDefaults();
        List <Entity> trainings = pq1.asList(fo1);
        
        JsonArray trainingArray = new JsonArray();
        JsonArray exerciseArray = new JsonArray();
        
        for (Entity entity : trainings) {
        	Filter fe = new FilterPredicate("training", Query.FilterOperator.EQUAL, KeyFactory.keyToString(entity.getKey()));
    		Query qe = new Query("Exercise").setFilter(fe);
            PreparedQuery pqe = store.prepare(qe);
            FetchOptions foe = FetchOptions.Builder.withDefaults();
            List <Entity> trainingExercises = pqe.asList(foe);
            
            JsonObject trainObj = new JsonObject();
            trainObj.addProperty("name", (String) entity.getProperty("title"));
            trainObj.addProperty("key", KeyFactory.keyToString(entity.getKey()));
            
            JsonArray trainingExercisesJSON = new JsonArray();
            for (Entity entity2 : trainingExercises) {
            	JsonObject trainingExercise = new JsonObject();
            	trainingExercise.addProperty("name", (String) entity2.getProperty("title"));
            	trainingExercise.addProperty("duration", (String) entity2.getProperty("duration"));
            	trainingExercise.addProperty("key", KeyFactory.keyToString(entity2.getKey()));
            	trainingExercise.addProperty("keytraining", (String) entity2.getProperty("training"));
            	trainingExercisesJSON.add(trainingExercise);
			}
            trainObj.add("exercises", trainingExercisesJSON);
            trainingArray.add(trainObj);
		}
        
        
        Filter f2 = new FilterPredicate("title", Query.FilterOperator.EQUAL,searchBar);
		Query q2 = new Query("Exercise").setFilter(f2);
        PreparedQuery pq2 = store.prepare(q2);
        FetchOptions fo2 = FetchOptions.Builder.withDefaults();
        List <Entity> exercises = pq2.asList(fo2);
        
        
        for (Entity entity : exercises) {
			JsonObject exerciseObject = new JsonObject();
			exerciseObject.addProperty("name", (String) entity.getProperty("title"));
			exerciseObject.addProperty("duration", (String) entity.getProperty("duration"));
			exerciseObject.addProperty("key", KeyFactory.keyToString(entity.getKey()));
			exerciseArray.add(exerciseObject);
		}
        
        JsonArray feedItems = new JsonArray();
        
        for (String s : RSSParser.parseRSS()) {
        	feedItems.add(new JsonPrimitive(s));
        }
        
        JsonObject resultat= new JsonObject();
        resultat.add("trainings", trainingArray);
        resultat.add("exercises", exerciseArray);
        resultat.add("news", feedItems);
        
        Gson gson = new Gson();
        gson.toJson(resultat);
        String jsonRes = gson.toJson(resultat);
        resp.getWriter().write(jsonRes);
        
	}
}

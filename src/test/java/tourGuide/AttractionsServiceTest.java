package tourGuide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.jsoniter.output.JsonStream;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.model.User;
import tourGuide.service.AttractionsService;
import tourGuide.service.LocationsService;
import tourGuide.service.RewardsService;
import tourGuide.service.TestService;


public class AttractionsServiceTest {
		GpsUtil gpsUtil = new GpsUtil();
		RewardCentral rewardCentral = new RewardCentral();
		RewardsService rewardsService = new RewardsService(gpsUtil, rewardCentral);
		TestService testService = new TestService(gpsUtil, rewardsService);
		
		@MockBean
		LocationsService locationsService = new LocationsService();
		
		AttractionsService attractionsService = new AttractionsService();
		
		@Before
		  public void init() {
			Locale.setDefault(Locale.US);		
		  }
		
		@Test
		public void testReturnOfSerializeGetFiveNearAttractions() throws JSONException, InterruptedException, ExecutionException {
			// GIVEN
			User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
			VisitedLocation visitedLocation = locationsService.trackUserLocation(user);
	
			Map<Attraction, Double> mapSorted = attractionsService.getAttractionsByDistance(visitedLocation);
			
			// WHEN
			JSONArray attractionsWanted = new JSONArray();	
			Iterator<Entry<Attraction, Double>> iterator = mapSorted.entrySet().iterator();
			int j =0;
			while(iterator.hasNext() &&  j<5) {
				Map.Entry<Attraction, Double> mapentry = iterator.next();
				
				Attraction key = (Attraction) mapentry.getKey();
				Double value = (Double) mapentry.getValue();			
				
				JSONObject attraction = new JSONObject();	
				attraction.put("name", key.attractionName);
				attraction.put("latitude", key.latitude);
				attraction.put("longitude", key.longitude);
				attraction.put("rewardPoints", JsonStream.serialize(rewardCentral.getAttractionRewardPoints(
						key.attractionId, user.getUserId())));
				attraction.put("distance", value);
				attractionsWanted.put(attraction);
				
				j++;
			
			}
				
			JSONObject result = new JSONObject();
			result.put("user's location",JsonStream.serialize(visitedLocation.location));
			result.put("attraction", attractionsWanted);
			
			// THEN
			System.out.println(result.getJSONArray("attraction"));
			assertEquals(5, result.getJSONArray("attraction").length());	
		}
	
		
		@Test
		public void testGetAttractionsByDistance() throws InterruptedException, ExecutionException {
			// GIVEN
			String URL = "http://localhost:5000";
			User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
			VisitedLocation visitedLocation = locationsService.trackUserLocation(user);
			// WHEN
			List<Attraction> attractions = gpsUtil.getAttractions();
			Map<Attraction, Double> attractionDistance = new HashMap<Attraction, Double>();
			
			////// service externe
			int nombre = 1000;
			ExecutorService executorService = Executors.newFixedThreadPool(nombre);
			List<Future<ResponseEntity<Double>>> futures = new ArrayList<>();
			
			
			//attractions.parallelStream().forEach((attraction) -> {
			for(Attraction attraction : attractions) {
				Location loc1 = new Location(attraction.longitude, attraction.latitude);
				String location1 = JsonStream.serialize(loc1);
				String location2 = JsonStream.serialize(visitedLocation.location);
				
				futures.add(executorService.submit( (
						) -> 				
				 new RestTemplate().getForEntity(
						URL + "/distance?location1={location1}&location2={location2}", Double.class, location1, location2)));
			
				for(Future<ResponseEntity<Double>> future : futures) {
					double reponse;
					try {
						reponse = future.get().getBody();
						attractionDistance.put(attraction, reponse);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			

			};
			executorService.shutdown();
			
	        Map<Attraction, Double> mapSortedByValue = new LinkedHashMap<>();
	        attractionDistance.entrySet().stream()
	                .sorted(Map.Entry.<Attraction, Double>comparingByValue())
	                .forEachOrdered(x -> mapSortedByValue.put(x.getKey(), x.getValue()));
			
			// THEN
			//récupère les valeurs de distance dans une liste pour valider le test
			List<Double> distanceSorted = new ArrayList<Double>();		
			for (Map.Entry<Attraction, Double> entry : mapSortedByValue.entrySet()) {
				Double value = entry.getValue();
				distanceSorted.add(value);
			}
			List<Double> fiveDistances = new ArrayList<Double>(distanceSorted.subList(0, 5));	
			
			assertThat(distanceSorted.get(0)<distanceSorted.get(1));
			assertEquals(fiveDistances.size(), 5);
		}

}

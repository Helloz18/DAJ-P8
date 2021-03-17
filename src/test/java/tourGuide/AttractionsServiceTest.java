package tourGuide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import java.util.stream.Collectors;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jsoniter.output.JsonStream;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.service.AttractionsService;
import tourGuide.service.LocationsService;
import tourGuide.service.RewardsService;
import tourGuide.service.TestService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;


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
			////V2
			JSONArray attractionsWanted = new JSONArray();
//			int i=0;
//			for (Map.Entry<Attraction, Double> entry : mapSorted.entrySet()) {		
//				while( i<5) { // le nombre d'attractionsWanted pourrait être géré dans une variable
//					Attraction key = entry.getKey();
//					Double value = entry.getValue();			
//					
//					JSONObject attraction = new JSONObject();	
//					attraction.put("name", key.attractionName);
//					attraction.put("latitude", key.latitude);
//					attraction.put("longitude", key.longitude);
//					attraction.put("rewardPoints", JsonStream.serialize(rewardCentral.getAttractionRewardPoints(
//							key.attractionId, user.getUserId())));
//					attraction.put("distance", value);
//					attractionsWanted.put(attraction);
//					
//					i++;
//				}		
			//}		
			//V3 car V2 pb de nom : les key sauvées étaient toujours les mêmes
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
			////V1
//			 //on sépare les attractions et les distances dans deux listes distinctes
//					List<Attraction> attractionsSorted = new ArrayList<Attraction>(); 
//					List<Double> distanceSorted = new ArrayList<Double>();
//					
//			for (Map.Entry<Attraction, Double> entry : mapSorted.entrySet()) {
//				Attraction key = entry.getKey();
//				Double value = entry.getValue();
//				attractionsSorted.add(key);
//				distanceSorted.add(value);
//			}
	//
//			// on ne garde que les 5 premiers éléments
//			List<Attraction> fiveAttractions = new ArrayList<Attraction>(attractionsSorted.subList(0, 5));
//			List<Double> fiveDistances = new ArrayList<Double>(distanceSorted.subList(0, 5));	
	//
//			// création d'un array JSON contenant les attractions sous forme d'objets JSON
//			JSONArray attractionsWanted = new JSONArray();
//			for(int i=0; i<5; i++) { // le nombre d'attractionsWanted pourrait être géré dans une variable
//			JSONObject attraction = new JSONObject();	
//			attraction.put("name", fiveAttractions.get(i).attractionName);
//			attraction.put("latitude", fiveAttractions.get(i).latitude);
//			attraction.put("longitude", fiveAttractions.get(i).longitude);
//			attraction.put("rewardPoints", JsonStream.serialize(rewardCentral.getAttractionRewardPoints(
//					fiveAttractions.get(i).attractionId, user.getUserId())));
//			attraction.put("distance", fiveDistances.get(i));
//			attractionsWanted.put(attraction);
//			}
			
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
			User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
			VisitedLocation visitedLocation = locationsService.trackUserLocation(user);
			// WHEN
			List<Attraction> attractions = gpsUtil.getAttractions();
			Map<Attraction, Double> attractionDistance = new HashMap<Attraction, Double>();
			
			//////v3 - service externe
			attractions.parallelStream().forEach((attraction) -> {
				Location loc1 = new Location(attraction.longitude, attraction.latitude);
				String location1 = JsonStream.serialize(loc1);
				String location2 = JsonStream.serialize(visitedLocation.location);
				ResponseEntity<Double> reponse = new RestTemplate()
						.getForEntity(
						"http://localhost:5000"+"/distance?location1={location1}&location2={location2}", 
						Double.class, 
						location1,location2);
				attractionDistance.put(attraction, reponse.getBody());			
			
			//////v2 - parallelStream
			//	attractionDistance.put(attraction, rewardsService.getDistance(attraction, visitedLocation.location));
			});		
				
			//////v1 - boucle for
//			for (Attraction attraction : attractions) {
//				Double distance = rewardsService.getDistance(attraction, visitedLocation.location);
//				attractionDistance.put(attraction, distance);
//			}
			
			
			// trie le map en fonction des distances, de la plus petite à la plus grande
//			Map<Attraction, Double> mapSortedByValue = attractionDistance.entrySet().stream()
//					.sorted(Map.Entry.comparingByValue())
//					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
//			
//			Map result = map.entrySet().stream()
//				    .sorted(Map.Entry.comparingByKey()) 			
//				    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//				    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//			
			   //Alternative way
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

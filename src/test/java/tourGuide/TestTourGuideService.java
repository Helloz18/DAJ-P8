package tourGuide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jsoniter.output.JsonStream;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

public class TestTourGuideService {
	

	GpsUtil gpsUtil = new GpsUtil();
	RewardCentral rewardCentral = new RewardCentral();
	RewardsService rewardsService = new RewardsService(gpsUtil, rewardCentral);
	TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

	
	@Before
	  public void init() {
		Locale.setDefault(Locale.US);		
	  }

	@Test
	public void getUserLocation() {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}
	
	@Test
	public void addUser() {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	@Test
	public void getAllUsers() {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void trackUser() {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocation.userId);
	}
	
	//TODO (à corriger)
	@Test
	public void getTripDeals() {
//		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
//		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
//				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
//		user.setTripDeals(providers);
	
		
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(10, providers.size());
	}
	
	@Test
	public void testReturnOfSerializeGetFiveNearAttractions() throws JSONException, InterruptedException, ExecutionException {
		// GIVEN
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

		Map<Attraction, Double> mapSorted = tourGuideService.getAttractionsByDistance(visitedLocation);
		
		// WHEN
		////V2
		JSONArray attractionsWanted = new JSONArray();
		int i=0;
		for (Map.Entry<Attraction, Double> entry : mapSorted.entrySet()) {		
			while( i<5) { // le nombre d'attractionsWanted pourrait être géré dans une variable
				Attraction key = entry.getKey();
				Double value = entry.getValue();			
				
				JSONObject attraction = new JSONObject();	
				attraction.put("name", key.attractionName);
				attraction.put("latitude", key.latitude);
				attraction.put("longitude", key.longitude);
				attraction.put("rewardPoints", JsonStream.serialize(rewardCentral.getAttractionRewardPoints(
						key.attractionId, user.getUserId())));
				attraction.put("distance", value);
				attractionsWanted.put(attraction);
				
				i++;
			}		
		}			
		////V1
//		 //on sépare les attractions et les distances dans deux listes distinctes
//				List<Attraction> attractionsSorted = new ArrayList<Attraction>(); 
//				List<Double> distanceSorted = new ArrayList<Double>();
//				
//		for (Map.Entry<Attraction, Double> entry : mapSorted.entrySet()) {
//			Attraction key = entry.getKey();
//			Double value = entry.getValue();
//			attractionsSorted.add(key);
//			distanceSorted.add(value);
//		}
//
//		// on ne garde que les 5 premiers éléments
//		List<Attraction> fiveAttractions = new ArrayList<Attraction>(attractionsSorted.subList(0, 5));
//		List<Double> fiveDistances = new ArrayList<Double>(distanceSorted.subList(0, 5));	
//
//		// création d'un array JSON contenant les attractions sous forme d'objets JSON
//		JSONArray attractionsWanted = new JSONArray();
//		for(int i=0; i<5; i++) { // le nombre d'attractionsWanted pourrait être géré dans une variable
//		JSONObject attraction = new JSONObject();	
//		attraction.put("name", fiveAttractions.get(i).attractionName);
//		attraction.put("latitude", fiveAttractions.get(i).latitude);
//		attraction.put("longitude", fiveAttractions.get(i).longitude);
//		attraction.put("rewardPoints", JsonStream.serialize(rewardCentral.getAttractionRewardPoints(
//				fiveAttractions.get(i).attractionId, user.getUserId())));
//		attraction.put("distance", fiveDistances.get(i));
//		attractionsWanted.put(attraction);
//		}
		
		JSONObject result = new JSONObject();
		result.put("user's location",JsonStream.serialize(visitedLocation.location));
		result.put("attraction", attractionsWanted);
		
		// THEN
		assertEquals(5, result.getJSONArray("attraction").length());	
	}

	
	@Test
	public void testGetAttractionsByDistance() throws InterruptedException, ExecutionException {
		// GIVEN
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		// WHEN
		List<Attraction> attractions = gpsUtil.getAttractions();
		Map<Attraction, Double> attractionDistance = new HashMap<Attraction, Double>();
		
		//////v2 - parallelStream
		attractions.parallelStream().forEach((attraction) -> {
			attractionDistance.put(attraction, rewardsService.getDistance(attraction, visitedLocation.location));
		});		
			
		//////v1 - boucle for
//		for (Attraction attraction : attractions) {
//			Double distance = rewardsService.getDistance(attraction, visitedLocation.location);
//			attractionDistance.put(attraction, distance);
//		}
		
		
		// trie le map en fonction des distances, de la plus petite à la plus grande
		Map<Attraction, Double> mapSortedByValue = attractionDistance.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
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
	
	@Test
	public void getAllCurrentLocationOfUsers() throws JSONException, JsonProcessingException {
		// GIVEN
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "bob", "000", "bob@tourGuide.com");
		User user3 = new User(UUID.randomUUID(), "don", "000", "don@tourGuide.com");
				
		List<User> users = new ArrayList<>();
		users.add(user);
		users.add(user2);
		users.add(user3);
		
		// WHEN
		JSONObject result = new JSONObject();
		for(User u : users) {
			result.put(u.getUserId().toString(), JsonStream.serialize(tourGuideService.trackUserLocation(u).location));
		}
		//	{
		//     "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
		//     ...
		//  }
		// THEN
		assertEquals(3, result.length());
	}
	
}

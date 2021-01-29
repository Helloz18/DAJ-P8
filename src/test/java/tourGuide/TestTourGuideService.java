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
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jsoniter.output.JsonStream;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
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
		InternalTestHelper.setInternalUserNumber(0);
		
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
	
	@Test
	public void getTripDeals() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(10, providers.size());
	}
	
	@Test
	public void testReturnOfSerializeGetFiveNearAttractions() throws JSONException {
		// GIVEN
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

		Map<Attraction, Double> mapSorted = tourGuideService.getAttractionsByDistance(visitedLocation);
		// on sépare les attractions et les distances dans deux listes distinctes
		List<Attraction> attractionsSorted = new ArrayList<Attraction>(); 
		List<Double> distanceSorted = new ArrayList<Double>();
		for (Map.Entry<Attraction, Double> entry : mapSorted.entrySet()) {
			Attraction key = entry.getKey();
			Double value = entry.getValue();
			attractionsSorted.add(key);
			distanceSorted.add(value);
		}

		// on ne garde que les 5 premiers éléments
		List<Attraction> fiveAttractions = new ArrayList<Attraction>(attractionsSorted.subList(0, 5));
		List<Double> fiveDistances = new ArrayList<Double>(distanceSorted.subList(0, 5));	

		// création d'un array JSON contenant les attractions sous forme d'objets JSON
		JSONArray attractionsWanted = new JSONArray();
		for(int i=0; i<5; i++) { // le nombre d'attractionsWanted pourrait être géré dans une variable
		JSONObject attraction = new JSONObject();	
		attraction.put("name", fiveAttractions.get(i).attractionName);
		attraction.put("latitude", fiveAttractions.get(i).latitude);
		attraction.put("longitude", fiveAttractions.get(i).longitude);
		attraction.put("rewardPoints", JsonStream.serialize(rewardCentral.getAttractionRewardPoints(
				fiveAttractions.get(i).attractionId, user.getUserId())));
		attraction.put("distance", fiveDistances.get(i));
		attractionsWanted.put(attraction);
		}
		
		JSONObject result = new JSONObject();
		result.put("user's location",JsonStream.serialize(visitedLocation.location));
		result.put("attraction", attractionsWanted);
		
		System.out.println(result.get("attraction"));
		
		assertEquals(5, result.getJSONArray("attraction").length());	
	}
	
	@Test
	public void testGetAttractionsByDistance() {
		// GIVEN
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		//WHEN
		List<Attraction> attractions = gpsUtil.getAttractions();
		Map<Attraction, Double> attractionDistance = new HashMap<Attraction, Double>();
		
		for (Attraction attraction : attractions) {
			Double distance = rewardsService.getDistance(attraction, visitedLocation.location);
			attractionDistance.put(attraction, distance);
		}
		// trie le map en fonction des distances, de la plus petite à la plus grande
		Map<Attraction, Double> mapSortedByValue = attractionDistance.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		//récupère les valeurs de distance dans une liste
		List<Double> distanceSorted = new ArrayList<Double>();		
		for (Map.Entry<Attraction, Double> entry : mapSortedByValue.entrySet()) {
			Double value = entry.getValue();
			distanceSorted.add(value);
		}
		// THEN
		assertThat(distanceSorted.get(0)<distanceSorted.get(1));	
	}
}

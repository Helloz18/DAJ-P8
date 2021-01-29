package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jsoniter.output.JsonStream;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	
	int attractionsProposedToUser = 5;
	
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}
	
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}
	
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
	
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}
	
	public VisitedLocation trackUserLocation(User user) {

		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	//m�thode d'origine � modifier
//	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
//		List<Attraction> nearbyAttractions = new ArrayList<>();
//		for(Attraction attraction : gpsUtil.getAttractions()) {
//			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
//				nearbyAttractions.add(attraction);
//			}
//		}
//		
//		return nearbyAttractions;
//	}
//	
	
	
	/**
	 * this method will get all attractions, calculate the distance between a visitedLocation and each attraction
	 * then these values will be saved in a map which will be sorted by distance.
	 * @param visitedLocation
	 * @return a map <Attraction (attraction), Double (distance)> sorted by distance
	 */
	public Map<Attraction, Double> getAttractionsByDistance(VisitedLocation visitedLocation) {
		List<Attraction> attractions = gpsUtil.getAttractions();
		Map<Attraction, Double> attractionDistance = new HashMap<Attraction, Double>();

		for (Attraction attraction : attractions) {
			Double distance = rewardsService.getDistance(attraction, visitedLocation.location);
			attractionDistance.put(attraction, distance);
		}
		Map<Attraction, Double> mapSortedByValue = attractionDistance.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return mapSortedByValue;
	}
	
	
	/**
	 * 
	 * @param visitedLocation
	 * @return
	 * @throws JSONException 
	 */
	public JSONObject getFiveNearAttractionsWithDistanceAndRewardsFromCurrentUserLocation(User user) throws JSONException {
		VisitedLocation visitedLocation = trackUserLocation(user);
		Map<Attraction, Double> mapSorted = getAttractionsByDistance(visitedLocation);
		// on s�pare les attractions et les distances dans deux listes distinctes
		List<Attraction> attractionsSorted = new ArrayList<Attraction>(); 
		List<Double> distanceSorted = new ArrayList<Double>();
		for (Map.Entry<Attraction, Double> entry : mapSorted.entrySet()) {
			Attraction key = entry.getKey();
			Double value = entry.getValue();
			attractionsSorted.add(key);
			distanceSorted.add(value);
		}

		// on ne garde que les 5 premiers �l�ments
		List<Attraction> fiveAttractions = new ArrayList<Attraction>(attractionsSorted.subList(0, 5));
		List<Double> fiveDistances = new ArrayList<Double>(distanceSorted.subList(0, 5));	

		// cr�ation d'un array JSON contenant les attractions sous forme d'objets JSON
		JSONArray attractionsWanted = new JSONArray();
		for(int i=0; i<attractionsProposedToUser; i++) { // le nombre d'attractionsWanted pourrait �tre g�r� dans une variable
		JSONObject attraction = new JSONObject();	
		attraction.put("name", fiveAttractions.get(i).attractionName);
		attraction.put("latitude", fiveAttractions.get(i).latitude);
		attraction.put("longitude", fiveAttractions.get(i).longitude);
		RewardCentral rewardCentral = new RewardCentral();
		attraction.put("rewardPoints", JsonStream.serialize(rewardCentral.getAttractionRewardPoints(
				fiveAttractions.get(i).attractionId, user.getUserId())));
		attraction.put("distance", fiveDistances.get(i));
		attractionsWanted.put(attraction);
		}
		
		JSONObject result = new JSONObject();
		result.put("user's location",JsonStream.serialize(visitedLocation.location));
		result.put("attraction", attractionsWanted);
		
		return result;
		}
		
		
		
	
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 1).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
}

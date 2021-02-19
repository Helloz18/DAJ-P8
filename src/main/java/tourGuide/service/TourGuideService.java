package tourGuide.service;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
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
import tourGuide.user.UserPreferences;
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
	public UserPreferences userPreferences = new UserPreferences();
	
	int attractionsProposedToUser = 5; // pourrait être mise dans le fichier de propriété du projet
	
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
	
	/**
	 * cumulativeRewardPoints : quel que soit le chiffre, la liste affiche toujours 5 attractions
	 * 1 on récupère les points gagnés par l'utilisateur
	 * 2 en fonction de préférences de l'utilisateurs (nb d'adultes, nb d'enfants, durée du voyage) et des points cummulés, 
	 * on récupère une liste d'attractions potentielles.
	 * PB autres préférences ? : le montant maximum des préférences utilisateur n'est pas pris en compte
	 * 	Est-ce que le ticket quantity doit être pris en compte aussi ? : on va dire oui : il ne faut pas dépasser le tarif *
	 * 	quelque soit le nombre de tickets voulus. Si une attraction coute 100 et que le user a défini qu'il voulait toujours
	 * 	2 tickets, et qu'il a défini son max à 100, alors l'attraction ne doit pas lui être présentée.
	 * 	nb ticket * prix de l'attraction comparé au max value
	 * 	Prendre aussi en compte le prix minimal (si des gens veulent éviter les attractions gratuites (?) )
	 * 	il faudrait ajouter maintenant des conditions supplémentaires par rapport à la liste des providers reçus
	 * @param user
	 * @return
	 */
	public List<Provider> getTripDeals(User user) {
		
		///// V1
		TripPricerV2 trip = new TripPricerV2();
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = trip.getPrice(tripPricerApiKey, user.getUserId(),user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;

		///// V0
//		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
//		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),user.getUserPreferences().getNumberOfAdults(), 
//				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
//		user.setTripDeals(providers);
//		return providers;
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public VisitedLocation trackUserLocation(User user) {

		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}
	
	/**
	 * This method is used by product Owner to see all the user locations.
	 * @param users
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getAllUsersLocation(List<User> users) throws JSONException {
		JSONObject jsonAllUsersLocations = new JSONObject();
		//Locale.setDefault(Locale.US);
		////V2 - ok
		for(User user : users) {
		jsonAllUsersLocations.put(user.getUserId().toString(), JsonStream.serialize(trackUserLocation(user).location));
		}
		
		/////v1
//		users.parallelStream().forEach((user) -> {
//			try {
//				jsonAllUsersLocations.put(user.getUserId().toString(), JsonStream.serialize(trackUserLocation(user).location));
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		});
		
		return jsonAllUsersLocations;
	}

//////////////////////////
	//méthode d'origine à modifier
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
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public Map<Attraction, Double> getAttractionsByDistance(VisitedLocation visitedLocation) throws InterruptedException, ExecutionException {
		List<Attraction> attractions = gpsUtil.getAttractions();
		Map<Attraction, Double> attractionDistance = new HashMap<Attraction, Double>();

		attractions.parallelStream().forEach((attraction) -> {
			attractionDistance.put(attraction, rewardsService.getDistance(attraction, visitedLocation.location));
		});		

		Map<Attraction, Double> mapSortedByValue = attractionDistance.entrySet().parallelStream()
			.sorted(Map.Entry.comparingByValue())
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return mapSortedByValue;
	}
	
	
	/**
	 * 
	 * @param User
	 * @return a JSON array of five objects such as :
	 * [
	 * 	{
	 * 		"distance":8765.59744222102,
	 * 		"latitude":61.218887,
	 * 		"name":"McKinley Tower",
	 * 		"rewardPoints":"154",
	 * 		"longitude":-149.877502
	 * 	}, ...
	 * ]
	 * @throws JSONException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public JSONObject getFiveNearAttractionsWithDistanceAndRewardsFromCurrentUserLocation(User user) throws JSONException, InterruptedException, ExecutionException {
		VisitedLocation visitedLocation = trackUserLocation(user);
		Map<Attraction, Double> mapSorted = getAttractionsByDistance(visitedLocation);
		RewardCentral rewardCentral = new RewardCentral();
		JSONArray attractionsWanted = new JSONArray();
		
		int i=0;
		for (Map.Entry<Attraction, Double> entry : mapSorted.entrySet()) {		
			while( i < 5 ) { // le nombre d'attractionsWanted pourrait être géré dans une variable
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
	
		JSONObject result = new JSONObject();
		result.put("user's location",JsonStream.serialize(visitedLocation.location));
		result.put("attraction", attractionsWanted);
		
		return result;
		}
		
		
///////////////////////////	
	
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
			user.addToVisitedLocations(new VisitedLocation(
					user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
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

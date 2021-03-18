package tourGuide.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jsoniter.output.JsonStream;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.model.User;

@Service
public class AttractionsService {

	int attractionsProposedToUser = 5; // pourrait être mise dans le fichier de propriété du projet

	GpsUtil gpsUtil = new GpsUtil();
	RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

	LocationsService locationsService = new LocationsService();
	String URL = "http://localhost:5000";

	/**
	 * this method will get all attractions, calculate the distance between a
	 * visitedLocation and each attraction then these values will be saved in a map
	 * which will be sorted by distance.
	 * 
	 * @param visitedLocation
	 * @return a map <Attraction (attraction), Double (distance)> sorted by distance
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Map<Attraction, Double> getAttractionsByDistance(VisitedLocation visitedLocation)
			throws InterruptedException, ExecutionException {
		List<Attraction> attractions = gpsUtil.getAttractions();
		Map<Attraction, Double> attractionDistance = new HashMap<Attraction, Double>();

		attractions.parallelStream().forEach((attraction) -> {
			Location loc1 = new Location(attraction.longitude, attraction.latitude);
			String location1 = JsonStream.serialize(loc1);
			String location2 = JsonStream.serialize(visitedLocation.location);
			ResponseEntity<Double> reponse = new RestTemplate().getForEntity(
					URL + "/distance?location1={location1}&location2={location2}", Double.class, location1, location2);
			attractionDistance.put(attraction, reponse.getBody());
		});

		Map<Attraction, Double> mapSortedByValue = attractionDistance.entrySet().parallelStream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return mapSortedByValue;
	}

	/**
	 * 
	 * @param User
	 * @return a JSON array of five objects such as : [ {
	 *         "distance":8765.59744222102, "latitude":61.218887, "name":"McKinley
	 *         Tower", "rewardPoints":"154", "longitude":-149.877502 }, ... ]
	 * @throws JSONException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public JSONObject getFiveNearAttractionsWithDistanceAndRewardsFromCurrentUserLocation(User user)
			throws JSONException, InterruptedException, ExecutionException {
		VisitedLocation visitedLocation = locationsService.trackUserLocation(user);
		Map<Attraction, Double> mapSorted = getAttractionsByDistance(visitedLocation);
		RewardCentral rewardCentral = new RewardCentral();
		JSONArray attractionsWanted = new JSONArray();

		Iterator<Entry<Attraction, Double>> iterator = mapSorted.entrySet().iterator();
		int j = 0;
		while (iterator.hasNext() && j < attractionsProposedToUser) {
			Map.Entry<Attraction, Double> mapentry = iterator.next();

			Attraction key = (Attraction) mapentry.getKey();
			Double value = (Double) mapentry.getValue();

			JSONObject attraction = new JSONObject();
			attraction.put("name", key.attractionName);
			attraction.put("latitude", key.latitude);
			attraction.put("longitude", key.longitude);
			attraction.put("rewardPoints",
					JsonStream.serialize(rewardCentral.getAttractionRewardPoints(key.attractionId, user.getUserId())));
			attraction.put("distance", value);
			attractionsWanted.put(attraction);

			j++;
		}

		JSONObject result = new JSONObject();
		result.put("user's location", JsonStream.serialize(visitedLocation.location));
		result.put("attraction", attractionsWanted);

		return result;
	}

}

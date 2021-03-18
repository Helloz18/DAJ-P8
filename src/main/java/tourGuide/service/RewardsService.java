package tourGuide.service;

import java.util.List;

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
import tourGuide.model.UserReward;

@Service
public class RewardsService {

	String URL = "http://localhost:5000";

	// proximity in miles
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;

	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * for a user, return the sum of all rewardsPoints earned by his
	 * visitedLocations.
	 * 
	 * @param user
	 */
	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();

		/// V2 ok
		for (int i = 0; i < userLocations.size(); i++) {
			for (Attraction attraction : attractions) {
				if (user.getUserRewards().parallelStream()
						.filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if (nearAttraction(userLocations.get(i), attraction)) {
						user.addUserReward(
								new UserReward(userLocations.get(i), attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}
	}

	/**
	 * non utilisée pour le moment attractionProximityRange defines a perimeter
	 * around the user's location. If an attraction is within this perimeter the
	 * method return true.
	 * 
	 * @param attraction
	 * @param location
	 * @return
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		Location loc1 = new Location(attraction.longitude, attraction.latitude);
		String location1 = JsonStream.serialize(loc1);
		String location2 = JsonStream.serialize(location);
		ResponseEntity<Double> reponse = new RestTemplate().getForEntity(
				URL + "/distance?location1={location1}&location2={location2}", Double.class, location1, location2);
		return reponse.getBody() > attractionProximityRange ? false : true;
	}

	/**
	 * return true if the user's visitedLocation matches the attraction's location.
	 * 
	 * @param visitedLocation
	 * @param attraction
	 * @return
	 */
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		Location loc1 = new Location(attraction.longitude, attraction.latitude);
		String location1 = JsonStream.serialize(loc1);
		String location2 = JsonStream.serialize(visitedLocation.location);
		ResponseEntity<Double> reponse = new RestTemplate().getForEntity(
				URL + "/distance?location1={location1}&location2={location2}", Double.class, location1, location2);

		return reponse.getBody() > proximityBuffer ? false : true;
	}

	/**
	 * return the rewardpoints of an attraction for a user.
	 * 
	 * @param attraction
	 * @param user
	 * @return
	 */
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	/**
	 * 
	 * @param user
	 * @return list of user rewards
	 */
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

}

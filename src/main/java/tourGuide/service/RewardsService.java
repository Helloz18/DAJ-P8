package tourGuide.service;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jsoniter.output.JsonStream;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

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
	 * for a user, return the sum of all rewardsPoints earned by his visitedLocations.
	 * @param user
	 */
	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();	

		///V2 ok
		for(int i=0; i < userLocations.size(); i++) {
			for(Attraction attraction : attractions) {
				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
				
					//V2 en utilisant nearAttraction
					System.out.println("attraction "+attraction.longitude + " "+attraction.latitude);
//					if(nearAttraction(userLocations.get(i), attraction)) {
					//V4 en utilisant Get au lieu de POST
//					Location attractionL = new Location(attraction.longitude, attraction.latitude);
//					String attractionLocation = JsonStream.serialize(attractionL);
//					String visitorLocation = JsonStream.serialize(userLocations.get(i).location);
//					ResponseEntity<Boolean> reponse = new RestTemplate().getForEntity(URL+"/nearAttraction?visitorLocation={visitorLocation}&attractionLocation={attractionLocation}", Boolean.class, visitorLocation,attractionLocation);
			//	System.out.println("test "+reponse.getBody());
					//V3 en utilisant le service externe avec une autre méthode public
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
					MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
				
					map.add("userId", user.getUserId().toString());
					System.out.println(user.getUserId().toString());
					map.add("visitedDate", userLocations.get(i).timeVisited.toString());
					System.out.println(userLocations.get(i).timeVisited.toString());
					map.add("attraction", JsonStream.serialize(attraction));
					System.out.println(JsonStream.serialize(attraction));
					map.add("visitorLocation", JsonStream.serialize(userLocations.get(i).location));
					HttpEntity<MultiValueMap<String, String>> requeteHttp = new HttpEntity<MultiValueMap<String, String>>(map, headers);
					ResponseEntity<Boolean> reponse = new RestTemplate().postForEntity(URL+"/nearAttraction", requeteHttp , Boolean.class);
					System.out.println("test "+reponse.getBody());
					if(reponse.getBody()) {
						user.addUserReward(new UserReward(userLocations.get(i), attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}		
		
		///// V1 (de base)
		/// pour chaque lieu visité par l'utilisateur
		///pour chaque attraction existante
		///si dans les rewards obtenus par le user on ne trouve pas l'attraction dans les lieux visités par l'utilisateur
		///et si l'attraction correspond à la localisation de l'utilisateur
		//alors le reward de cet attraction est ajouté aux rewards de l'utilisateur
//		for(VisitedLocation visitedLocation : userLocations) {
//			for(Attraction attraction : attractions) {
//				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
//					if(nearAttraction(visitedLocation, attraction)) {
//						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
//					}
//				}
//			}
//		}
	}
	
	/**
	 * attractionProximityRange defines a perimeter around the user's location. If an attraction is within this 
	 * perimeter the method return true.
	 * @param attraction
	 * @param location
	 * @return
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	/**
	 * return true if the user's visitedLocation matches the attraction's location.
	 * @param visitedLocation
	 * @param attraction
	 * @return
	 */
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	/**
	 * return the rewardpoints of an attraction for a user.
	 * @param attraction
	 * @param user
	 * @return
	 */
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	/**
	 * this method calculate the distance in miles between two locations.
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}
	
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	

}

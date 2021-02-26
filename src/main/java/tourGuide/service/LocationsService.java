package tourGuide.service;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.jsoniter.output.JsonStream;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;

@Service
public class LocationsService {
	
	GpsUtil gpsUtil = new GpsUtil();
	RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
	
	
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
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

}

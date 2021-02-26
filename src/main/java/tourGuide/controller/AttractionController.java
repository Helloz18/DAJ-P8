package tourGuide.controller;

import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import tourGuide.service.AttractionsService;
import tourGuide.service.UserService;
import tourGuide.user.User;

@RestController
public class AttractionController {
	
	@Autowired
	AttractionsService attractionsService;
	
	@Autowired
	UserService userService;
	

//  TODO: Change this method to no longer return a List of Attractions.
 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
//    @RequestMapping("/getNearbyAttractions") 
//    public String getNearbyAttractions(@RequestParam String userName) {
//    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
//    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
//    }
 /**
  * use to propose the fifth attractions around a user despite the distance
  * @param userName
  * @return a Json object with some info :
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
         * {"attraction":
         * 	[{	"distance":5782.163566786617,
         * 		"latitude":26.890959,
         * 		"name":"Roger Dean Stadium",
         * 		"rewardPoints":"137",
         * 		"longitude":-80.116577
         * 	},{...}],
         * 	"user's location":
         * 		"{\"longitude\":-34.657846,\"latitude\":-46.0162}"}
  * @throws JSONException
 * @throws ExecutionException 
 * @throws InterruptedException 
  */
    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) 
    		throws JSONException, InterruptedException, ExecutionException {
    	User user = userService.getUser(userName);
    	return JsonStream.
    			serialize(attractionsService.getFiveNearAttractionsWithDistanceAndRewardsFromCurrentUserLocation(user));
    }
   
       
    
}

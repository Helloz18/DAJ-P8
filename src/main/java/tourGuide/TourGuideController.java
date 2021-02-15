//package tourGuide;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//
//import org.json.JSONException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.jsoniter.output.JsonStream;
//
//import gpsUtil.location.VisitedLocation;
//import tourGuide.service.TourGuideService;
//import tourGuide.user.User;
//import tripPricer.Provider;
//
//@RestController
//public class TourGuideController {
//
//	@Autowired
//	TourGuideService tourGuideService;
//	
//    @RequestMapping("/")
//    public String index() {
//        return "Greetings from TourGuide!";
//    }
//    
//    /**
//     * get the last visitedLocation of a user if there is one, or the current visited location if it's his first
//     * @param userName
//     * @return a string with the latitude and longitude of the user
//     */
//    @RequestMapping("/getLocation") 
//    public String getLocation(@RequestParam String userName) {
//    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
//		return JsonStream.serialize(visitedLocation.location);
//    }
//    
//    //  TODO: Change this method to no longer return a List of Attractions.
// 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
// 	//  Return a new JSON object that contains:
//    	// Name of Tourist attraction, 
//        // Tourist attractions lat/long, 
//        // The user's location lat/long, 
//        // The distance in miles between the user's location and each of the attractions.
//        // The reward points for visiting each Attraction.
//        //    Note: Attraction reward points can be gathered from RewardsCentral
////    @RequestMapping("/getNearbyAttractions") 
////    public String getNearbyAttractions(@RequestParam String userName) {
////    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
////    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
////    }
// /**
//  * use to propose the fifth attractions around a user despite the distance
//  * @param userName
//  * @return a Json object with some info :
//    	// Name of Tourist attraction, 
//        // Tourist attractions lat/long, 
//        // The user's location lat/long, 
//        // The distance in miles between the user's location and each of the attractions.
//        // The reward points for visiting each Attraction.
//         * {"attraction":
//         * 	[{	"distance":5782.163566786617,
//         * 		"latitude":26.890959,
//         * 		"name":"Roger Dean Stadium",
//         * 		"rewardPoints":"137",
//         * 		"longitude":-80.116577
//         * 	},{...}],
//         * 	"user's location":
//         * 		"{\"longitude\":-34.657846,\"latitude\":-46.0162}"}
//  * @throws JSONException
// * @throws ExecutionException 
// * @throws InterruptedException 
//  */
//    @RequestMapping("/getNearbyAttractions") 
//    public String getNearbyAttractions(@RequestParam String userName) 
//    		throws JSONException, InterruptedException, ExecutionException {
//    	User user = getUser(userName);
//    	return JsonStream.
//    			serialize(tourGuideService.getFiveNearAttractionsWithDistanceAndRewardsFromCurrentUserLocation(user));
//    }
//   
//    /**
//     * 
//     * @param userName
//     * @return a json object with a list of the user's rewards
//     */
//    @RequestMapping("/getRewards") 
//    public String getRewards(@RequestParam String userName) {
//    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
//    }
//    
//    /**
//     * 
//     * @return
//     * @throws JSONException 
//     */
//    @RequestMapping("/getAllCurrentLocations")
//    public String getAllCurrentLocations() throws JSONException {
//    	// TODO: Get a list of every user's most recent location as JSON
//    	//- Note: does not use gpsUtil to query for their current location, 
//    	//        but rather gathers the user's current location from their stored location history.
//    	//
//    	// Return object should be the just a JSON mapping of userId to Locations similar to:
//    	//     {
//    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
//    	//        ...
//    	//     }
//    	List<User> users = new ArrayList<User>();
//    	users = tourGuideService.getAllUsers();  	
//    	return JsonStream.serialize(tourGuideService.getAllUsersLocation(users));
//    }
//    
//    /**
//     * the user's rewards are sumed to suggest a list of trip deals available with this sum. The suggestion take in
//     * consideration the user's preferences (number of adult, children...)
//     * @param userName
//     * @return
//     */
//    @RequestMapping("/getTripDeals")
//    public String getTripDeals(@RequestParam String userName) {
//    	List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
//    	return JsonStream.serialize(providers);
//    }
//    
//    /**
//     * 
//     * @param userName
//     * @return a user by its name
//     */
//    private User getUser(String userName) {
//    	return tourGuideService.getUser(userName);
//    }
//   
//
//}
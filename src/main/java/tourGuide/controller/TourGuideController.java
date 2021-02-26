//package tourGuide.controller;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
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
//	@Autowired
//	TourGuideService tourGuideService;
//	
//    
//   
//    
//    
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
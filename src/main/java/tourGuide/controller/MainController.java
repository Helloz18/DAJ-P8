package tourGuide.controller;

import java.util.Locale;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

	  @RequestMapping("/")
	    public String index(Locale locale) {
	    	
	        return "Greetings from TourGuide!"+locale;
	    }
	  
}

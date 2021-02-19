package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import tripPricer.Provider;
import tripPricer.TripPricer;

public class TripPricerV2 extends TripPricer {
	
	TripPricer tripPricer = new TripPricer();

	@Override
	public List<Provider> getPrice(
			String apiKey, UUID attractionId, int adults, int children, int nightsStay, int rewardsPoints){
		List<Provider> providers = new ArrayList<Provider>();
		
		Double price = 0.0;
		
		for(int i = 0; i < 10; ++i) { // ça boucle 10 fois donc ça renvoie 10 résultats
            int multiple = ThreadLocalRandom.current().nextInt(100, 700); // tarif aléatoire entre 100 et 700
            
            if(children > 0) {
            	double childrenDiscount = (double)(children / 3); 
            	price = ((double)(multiple * adults) + (double)(multiple * childrenDiscount)) 
            			* (double)nightsStay + 0.99D - (double)rewardsPoints;
            } else {
            	price = (double)(multiple * adults) * (double)nightsStay + 0.99D - (double)rewardsPoints;
            }
            if (price < 0.0D) {
                price = 0.0D;
            }
            
            providers.add(new Provider(attractionId, tripPricer.getProviderName(apiKey, adults), price));
        }

		for(int i=0; i<providers.size(); i++) {
			System.out.println(providers.get(i).name);
			System.out.println(providers.get(i).price);
		}
		return providers;
		
	}
}

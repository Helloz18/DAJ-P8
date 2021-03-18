package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Value;

import tourGuide.model.User;
import tripPricer.Provider;
import tripPricer.TripPricer;

public class TripPricerService extends TripPricer {
	
	@Value("${tripPricerApiKey}")
	private String tripPricerApiKey;
	
	TripPricer tripPricer = new TripPricer();

	/**
	 * modification de la méthode getPrice pour qu'elle prenne en compte les préférences utilisateurs
	 * @param apiKey
	 * @param adults
	 * @param children
	 * @param nightsStay
	 * @param rewardsPoints
	 * @param lowerPrice
	 * @param higherPricer
	 * @return
	 */
	public List<Provider> getPrice(
			String apiKey, int adults, int children, int nightsStay, int rewardsPoints, 
			Money lowerPrice, Money higherPricer){
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
            if(price < higherPricer.getNumber().doubleValue() && price > lowerPrice.getNumber().doubleValue() ) {
            	 String providerName = getProviderName(apiKey, adults);
                 UUID tripId = UUID.randomUUID();
                 providers.add(new Provider(tripId, providerName, price));
            }
            
        }

		for(int i=0; i<providers.size(); i++) {
			System.out.println(providers.get(i).name);
			System.out.println(providers.get(i).price);
		}
		return providers;
		
	}
	
	/**
	 * @param user
	 * @return
	 */
	public List<Provider> getTripDeals(User user) {
		TripPricerService trip = new TripPricerService();
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = trip.getPrice(tripPricerApiKey,user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints,
				user.getUserPreferences().getLowerPricePoint(), user.getUserPreferences().getHighPricePoint());
		user.setTripDeals(providers);
		return providers;

	}

	}

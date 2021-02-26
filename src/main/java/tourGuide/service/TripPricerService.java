package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import tourGuide.user.User;
import tripPricer.Provider;
import tripPricer.TripPricer;

public class TripPricerService extends TripPricer {
	
	@Value("${tripPricerApiKey}")
	private String tripPricerApiKey;
	
	@Autowired
	TripPricer tripPricer;

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
	
	/**
	 * cumulativeRewardPoints : quel que soit le chiffre, la liste affiche toujours 5 attractions
	 * 1 on récupère les points gagnés par l'utilisateur
	 * 2 en fonction de préférences de l'utilisateurs (nb d'adultes, nb d'enfants, durée du voyage) et des points cummulés, 
	 * on récupère une liste d'attractions potentielles.
	 * PB autres préférences ? : le montant maximum des préférences utilisateur n'est pas pris en compte
	 * 	Est-ce que le ticket quantity doit être pris en compte aussi ? : on va dire oui : il ne faut pas dépasser le tarif *
	 * 	quelque soit le nombre de tickets voulus. Si une attraction coute 100 et que le user a défini qu'il voulait toujours
	 * 	2 tickets, et qu'il a défini son max à 100, alors l'attraction ne doit pas lui être présentée.
	 * 	nb ticket * prix de l'attraction comparé au max value
	 * 	Prendre aussi en compte le prix minimal (si des gens veulent éviter les attractions gratuites (?) )
	 * 	il faudrait ajouter maintenant des conditions supplémentaires par rapport à la liste des providers reçus
	 * @param user
	 * @return
	 */
	public List<Provider> getTripDeals(User user) {
		
		///// V1
		TripPricerService trip = new TripPricerService();
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = trip.getPrice(tripPricerApiKey, user.getUserId(),user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;

		///// V0
//		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
//		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),user.getUserPreferences().getNumberOfAdults(), 
//				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
//		user.setTripDeals(providers);
//		return providers;
	}

}

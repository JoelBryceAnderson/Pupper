package joelbryceanderson.com.pupperkeyboard.services;

import java.util.List;

import joelbryceanderson.com.pupperkeyboard.models.Pupper;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by JAnderson on 2/8/17.
 *
 * Retrofit Service to load puppers from Reddit.
 */
public interface PupperService {

    String PUPPER_ENDPOINT = "https://www.reddit.com/r/rarepuppers+puppysmiles+doggos+dogpictures+puppies+dogswearinghats+dogsdrivingcars+dogswearingties+dogssittinglikepeople+doge+lookatmydog+puppy/";

    @GET("random/.json")
    Observable<List<Pupper>> getRandomPupper();
}

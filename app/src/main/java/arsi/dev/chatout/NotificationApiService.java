package arsi.dev.chatout;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationApiService {

    @Headers({
            "Authorization: key=AAAAzHJBt1o:APA91bFdbFJepk9R8PMn4nuHNtqpaKI-nwcq8MGsxV4we6FevV2CAVPRCtLzXveJYWI2XOv6rgIZYzS-gbeFcds23BPystp4c9qwHAjC5vQW20jM2ewexXc-lXVLojFUZbz4smXrnIGq" ,
            "Content-Type: application/json"
    })
    @POST("fcm/send")
    Call<JsonObject> sendNotification(@Body JsonObject payload);
}
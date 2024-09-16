package com.telnyx.videodemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;

import com.google.common.util.concurrent.ListenableFuture;
import com.telnyx.video.sdk.Room;
import com.telnyx.video.sdk.model.AudioDevice;
import com.telnyx.video.sdk.utilities.CameraDirection;
import com.telnyx.video.sdk.utilities.PublishConfigHelper;
import com.telnyx.video.sdk.webSocket.model.send.ExternalData;
import com.telnyx.video.sdk.webSocket.model.ui.Participant;
import com.telnyx.video.sdk.webSocket.model.ui.ParticipantStream;
import com.telnyx.video.sdk.webSocket.model.ui.StreamConfig;
import com.telnyx.video.sdk.webSocket.model.ui.StreamStatus;
import com.telnyx.videodemo.models.DataResponse;
import com.telnyx.videodemo.models.OfflineRoom;
import com.telnyx.videodemo.models.SharedPref;
import com.telnyx.videodemo.models.createToken.CreateTokenRequest;
import com.telnyx.videodemo.models.createToken.GetTokenInfo;
import com.telnyx.webrtc.sdk.CredentialConfig;
import com.telnyx.webrtc.sdk.TelnyxClient;
import com.telnyx.webrtc.sdk.model.LogLevel;
import com.telnyx.webrtc.sdk.model.SocketMethod;
import com.telnyx.webrtc.sdk.model.SocketStatus;
import com.telnyx.webrtc.sdk.model.TxServerConfiguration;
import com.telnyx.webrtc.sdk.verto.receive.ReceivedMessageBody;
import com.telnyx.webrtc.sdk.verto.receive.SocketObserver;
import com.telnyx.webrtc.sdk.verto.receive.SocketResponse;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
 enum CapturerConstraints {
    WIDTH(256),
    HEIGHT(256),
    FPS(30);

    private final int value;

    CapturerConstraints(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    static String refreshToken;
    static int refreshTokenExpiresAt;
    SharedPref sharedPref;

    PreviewView previewLoginView;

    private TextView tvRoomId;
    private TextView tvParticipant;
    private Button btnJoin;

    private Button toggleCamera;
    private Button btnDisconnectRoom;

    private Button btnDelete;

    private CardView joinRomCard;
    private Participant selfParticipant;
    private Participant remoteParticipant;
    private Room room;


    private SurfaceViewRenderer remoteParticipantTileSurface;
    private TextView remoteParticipantTileName;
    private TextView remoteParticipantTileId;

    Handler handler = new Handler();


    private SurfaceViewRenderer selfTileSurface;
    private TextView participantTileName;
    private TextView participantTileId;

    // Could be Presentation for Screen Sharing
    private String LocalStreamKey = "self";
    private String SELF_STREAM_ID =  UUID.randomUUID().toString();
    private String VIDEO_TRACK_KEY = "000";
    private String AUDIO_TRACK_KEY = "001";



    private PublishConfigHelper publishConfigHelper;

    private Map<String, VideoTrack>  viewHolderMap = new ArrayMap<>();


    CardView progressBar;
    private void requestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES,
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }

        // Check for each permission to avoid asking for already granted permissions
        requestPermissionLauncher.launch(filterNotGrantedPermissions(permissions));
    }

    private String[] filterNotGrantedPermissions(String[] permissions) {
        return Arrays.stream(permissions)
                .filter(permission -> ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                .toArray(String[]::new);
    }

    private void onPermissionResult(Map<String, Boolean> grantResults) {
        boolean allPermissionsGranted = grantResults.values().stream().allMatch(granted -> granted);
        if (allPermissionsGranted) {
            // All requested permissions are granted
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_LONG).show();
        } else {
            // At least one permission is denied
            Toast.makeText(this, "One or more permissions denied", Toast.LENGTH_LONG).show();
        }
    }

    void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
    JoinRoomDialogFragment dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TelnyxClient txClient = new TelnyxClient(this);
        Timber.d("Connecting to Telnyx");
        txClient.connect(new TxServerConfiguration(), new CredentialConfig(
                "",
                "",
                "connectionId",
                "jwt",
                "username",
                null,
                null,
                LogLevel.ALL,
                true
        ),null,true);

        txClient.getSocketResponse().observe(this, new SocketObserver<ReceivedMessageBody>() {
            @Override
            public void onSocketDisconnect() {

            }

            @Override
            public void onError(@Nullable String s) {

            }

            @Override
            public void onLoading() {

            }

            @Override
            public void onMessageReceived(@Nullable ReceivedMessageBody receivedMessageBody) {
                assert receivedMessageBody != null;
                if (receivedMessageBody.getMethod().equals(SocketMethod.CLIENT_READY.getMethodName())) {
                    txClient.newInvite("isaac", "izac", "destinatiob","",null);
                }

            }

            @Override
            public void onConnectionEstablished() {

            }
        });

        sharedPref = new SharedPref(this);
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dialog  = new JoinRoomDialogFragment(this);
        progressBar =  findViewById(R.id.progressBarLayout);
        previewLoginView = findViewById(R.id.previewLoginView);
        //testCamera();
        joinRomCard = findViewById(R.id.roomCardView);
        // Initialize the views
        tvRoomId = findViewById(R.id.tvRoomId);
        tvParticipant = findViewById(R.id.tvParticipant);
        btnJoin = findViewById(R.id.btnJoin);
        toggleCamera = findViewById(R.id.btnToggleCamera);
        btnDisconnectRoom = findViewById(R.id.disconnectRoom);
        btnDelete = findViewById(R.id.btnDelete);
        if (sharedPref.getOfflineRoom() == null) {
            dialog.show(getSupportFragmentManager(), "joinRoomDialog");
            dialog.setCancelable(false);
        } else {
            initJoined();
        }
        requestPermissions();



        btnDisconnectRoom.setOnClickListener(v -> {
            // Handle the disconnect action
            if (room != null) {
                room.disconnect();
            }
        });


        toggleCamera.setOnClickListener(v -> {
            // Handle the reset action
            if (selfParticipant == null) return;
            if (!cameraStarted){
                startCameraCapture();
            }else  {
               stopCameraCapture();
            }
            cameraStarted = !cameraStarted;

        });

    }

    void initJoined() {
        OfflineRoom offlineRoom = sharedPref.getOfflineRoom();
        tvRoomId.setText(String.format("Room ID: %s", offlineRoom.getRoomId()));
        tvParticipant.setText(String.format("Participant: %s", offlineRoom.getParticipant()));

        selfTileSurface = findViewById(R.id.participant_tile_surface);
        participantTileName = findViewById(R.id.participant_tile_name);
        participantTileId = findViewById(R.id.participant_tile_id);
        // Set the OnClickListener for the buttons
        btnJoin.setOnClickListener(v -> {
            // Handle the join action
            createToken(UUID.fromString(offlineRoom.getRoomId()), offlineRoom.getParticipant());
        });

        btnDelete.setOnClickListener(v -> {
            // Handle the delete action
            Toast.makeText(this, "Not Implemented", Toast.LENGTH_SHORT).show();
        });
    }

    private void getObservers(){

        Timber.tag("RoomFragment").d("Room Joined");
        if (room != null) {

            /*
            * Observe for new participants joining the room
            * */
            room.getJoinedParticipant().observe(this, participantEvent -> {
                showProgress(false);

                Log.d("RoomFragment", "getJoinedParticipant() STARTED" );
                Participant participant = participantEvent.getContentIfNotHandled();

                if (selfParticipant != null && participant.getParticipantId().equals(selfParticipant.getParticipantId())) {
                    selfParticipant = participant;
                }

                if (remoteParticipant != null && participant.getParticipantId().equals(remoteParticipant.getParticipantId())) {
                    remoteParticipant = participant;
                }

                if (participant == null) {
                    Timber.tag("RoomFragment").e("getJoinedParticipant() %s", participantEvent);
                    return;
                }
                setupRemoteParticipant(participant);


            });

            room.getParticipantsObservable().observe(this, participants -> {
                showProgress(false);
                Log.d("RoomFragment", "getParticipantsObservable() STARTED "  + participants.size()) ;
                for(Participant participant: participants){
                    if (participant.isSelf()) {
                        selfParticipant = participant;
                    } else {
                        remoteParticipant = participant;
                    }

                    if (participant.isSelf()) {
                        setupSelfParticipant(participant);
                    } else {
                        setupRemoteParticipant(participant);
                        setupRemoteParticipant(participant);
                    }

                }
                if (cameraStarted){
                    return;
                }
                startCameraCapture();
                cameraStarted = true;

                // This is a hack to start the audio after 2 seconds
                // Starting the audio immediately after the video causes the video to not work
                // This is a temporary fix
                handler.postDelayed(this::startAudioCapture, 2000);
            });

            room.getParticipantStreamChanged().observe(this, participantStreamEvent -> {
                showProgress(false);
                Log.d("RoomFragment", "getParticipantStreamChanged() STARTED" );
                Participant participant = participantStreamEvent.getContentIfNotHandled();
                if (participant == null) {
                    Timber.tag("RoomFragment").e("getParticipantStreamChanged() %s", participantStreamEvent);
                    return;
                }

                if (participant.getParticipantId().equals(selfParticipant.getParticipantId())) {
                    selfParticipant = participant;
                    streamParticipant(selfParticipant,true);

                } else {
                    remoteParticipant = participant;
                    streamParticipant(remoteParticipant,false);
                }
            });
        }
    }


    private  void streamParticipant(Participant participant,Boolean isSelf){

        ParticipantStream stream = null;

        for (ParticipantStream s : participant.getStreams()) {
            if ("self".equals(s.getStreamKey())) {
                stream = s;
                break;
            }
        }

        if (stream != null && StreamStatus.ENABLED.equals(stream.getVideoEnabled())) {

            if (isSelf){
                Timber.tag("ParticipantTileAdapter").d("onBind() STARTED");

                room.addSubscription(participant.getParticipantId(), LocalStreamKey, new StreamConfig(true,true));
                room.setParticipantSurface(participant.getParticipantId(), selfTileSurface, LocalStreamKey);

                VideoTrack videoTrack = stream.getVideoTrack();
                if (videoTrack == null){
                    Timber.e("VideoTrack is null");
                    return;
                }

                if (viewHolderMap.get(participant.getParticipantId()) != videoTrack){

                    VideoTrack oldVideoTrack = viewHolderMap.get(participant.getParticipantId());
                    if (oldVideoTrack != null){
                        oldVideoTrack.removeSink(selfTileSurface);
                        selfTileSurface.release();
                    }

                    viewHolderMap.put(participant.getParticipantId(), videoTrack);
                    videoTrack.addSink(selfTileSurface);
                    videoTrack.setEnabled(true);
                }
            } else {
                Timber.tag("ParticipantTileAdapter").d("onBind() STARTED");
                room.addSubscription(participant.getParticipantId(), LocalStreamKey, new StreamConfig(true,true));
                room.setParticipantSurface(participant.getParticipantId(), remoteParticipantTileSurface, LocalStreamKey);

                VideoTrack videoTrack = stream.getVideoTrack();
                if (videoTrack == null){
                    Timber.e("VideoTrack is null");
                    return;
                }
                videoTrack.addSink(remoteParticipantTileSurface);
                videoTrack.setEnabled(true);
            }
        } else {
            Timber.tag("ParticipantTileAdapter").d("onBind() PAUSED");
            room.removeSubscription(participant.getParticipantId(), LocalStreamKey);
        }
    }

    private void setupRemoteParticipant(Participant participant){
        remoteParticipantTileSurface = findViewById(R.id.remote_participant_tile_surface);
        remoteParticipantTileName = findViewById(R.id.remote_participant_tile_name);
        remoteParticipantTileId = findViewById(R.id.remote_participant_tile_id);

        remoteParticipantTileName.setText( participant.getParticipantId().substring(0, 5));
        remoteParticipantTileName.setText(participant.getExternalUsername());

        ParticipantStream stream = null;
        for (ParticipantStream s : participant.getStreams()) {
            if ("self".equals(s.getStreamKey())) {
                stream = s;
                break;
            }
        }

        // Check if the stream is enabled and start the video
        if (stream != null && StreamStatus.ENABLED.equals(stream.getVideoEnabled())) {
            Timber.tag("ParticipantTileAdapter").d("onBind() STARTED");
            room.addSubscription(participant.getParticipantId(), LocalStreamKey, new StreamConfig(true,true));
            room.setParticipantSurface(participant.getParticipantId(), remoteParticipantTileSurface, LocalStreamKey);

            VideoTrack videoTrack = stream.getVideoTrack();
            videoTrack.addSink(selfTileSurface);
            videoTrack.setEnabled(true);
        } else {
            Timber.tag("ParticipantTileAdapter").d("onBind() PAUSED");
            room.removeSubscription(participant.getParticipantId(), LocalStreamKey);
        }
    }

    private void setupSelfParticipant(Participant participant){
        Timber.tag("RoomFragment").e("setupSelfParticipant() %s", participant);


        participantTileId.setText( participant.getParticipantId().substring(0, 5));
        participantTileName.setText(participant.getExternalUsername());

        ParticipantStream stream = null;
        for (ParticipantStream s : participant.getStreams()) {
            if ("self".equals(s.getStreamKey())) {
                stream = s;
                break;
            }
        }

        if (stream == null){
            Timber.tag("ParticipantTileAdapter").d("onBind() Null");
            return;
        }


        if (StreamStatus.ENABLED.equals(stream.getVideoEnabled())) {
            Timber.tag("ParticipantTileAdapter").d("onBind() STARTED");
            room.addSubscription(participant.getParticipantId(), LocalStreamKey, new StreamConfig(true,true));
            room.setParticipantSurface(participant.getParticipantId(), selfTileSurface, LocalStreamKey);

            VideoTrack videoTrack = stream.getVideoTrack();
            videoTrack.addSink(selfTileSurface);
            videoTrack.setEnabled(true);
        } else {
            Timber.tag("ParticipantTileAdapter").d("onBind() PAUSED");
            room.removeSubscription(participant.getParticipantId(), LocalStreamKey);
        }

    }
    boolean cameraStarted = false;

    boolean selfPreviewSet = false;

    boolean shouldPublish = true;
    private void startCameraCapture() {
        boolean shouldPublish = publishConfigHelper == null;
        if (shouldPublish) {
            publishConfigHelper = new PublishConfigHelper(
                    this,
                    CameraDirection.FRONT,
                    LocalStreamKey,
                    SELF_STREAM_ID
            );
        }

        try {
            publishConfigHelper.setSurfaceView(selfTileSurface);
        }       catch (Exception e) {
            e.printStackTrace();
        }


        if (publishConfigHelper != null) {
            publishConfigHelper.createVideoTrack(
                    CapturerConstraints.WIDTH.getValue(),
                    CapturerConstraints.HEIGHT.getValue(),
                    CapturerConstraints.FPS.getValue(), true, VIDEO_TRACK_KEY
            );
            if (shouldPublish) {
                room.addStream(publishConfigHelper);
            } else {
                room.updateStream(publishConfigHelper);
            }
        }
    }


    private void startAudioCapture() {
        boolean shouldPublish = publishConfigHelper == null;
        if (shouldPublish) {
            publishConfigHelper = new PublishConfigHelper(
                    MainActivity.this,
                    CameraDirection.FRONT,
                    LocalStreamKey,
                    SELF_STREAM_ID
            );
        }
        publishConfigHelper.createAudioTrack(true, AUDIO_TRACK_KEY);

        if (shouldPublish) {
            room.addStream(publishConfigHelper);
        } else {
            room.updateStream(publishConfigHelper);
        }
        room.setAudioOutputDevice(AudioDevice.LOUDSPEAKER);
    }

     ApiService getApiService() {
        return RetrofitClient.getRetrofitInstance().create(ApiService.class);
    }

     public int randomInt(int length) {
        String characters = "0123456789";
        StringBuilder stringBuilder = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return Integer.parseInt(stringBuilder.toString());
    }


    public  int calculateTokenExpireTime(String tokenExpire) {
        final double TIME_FRAGMENT = 0.90;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant now = Instant.now();
            long nowMillis = now.toEpochMilli();
            long tokenExpires = Instant.parse(tokenExpire).toEpochMilli();
            return (int) ((tokenExpires - nowMillis) * TIME_FRAGMENT);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                // formatting the dateString to convert it into a Date
                long tokenExpireMillis = sdf.parse(tokenExpire).getTime();
                long currentTimeMillis = System.currentTimeMillis();
                return (int) ((tokenExpireMillis - currentTimeMillis) * TIME_FRAGMENT);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

     void createToken(UUID roomId, String participantName) {
        // Create a token for the room
         showProgress(true);
        CreateTokenRequest createTokenRequest = new CreateTokenRequest(86400, 3600);
        Call<DataResponse<GetTokenInfo>> call = getApiService().createClientToken(roomId.toString(), createTokenRequest);
        call.enqueue(new Callback<DataResponse<GetTokenInfo>>() {
            @Override
            public void onResponse(Call<DataResponse<GetTokenInfo>> call, Response<DataResponse<GetTokenInfo>> response) {
                if (response.isSuccessful()) {
                    GetTokenInfo tokenInfo = response.body().getData();
                    refreshToken = tokenInfo.getToken();
                    refreshTokenExpiresAt = calculateTokenExpireTime(tokenInfo.getRefresh_token_expires_at());

                    startActivity(new Intent(MainActivity.this, TelnyxVisitationActivity.class)
                            .putExtra("roomName", roomId.toString())
                            .putExtra("token", refreshToken)
                            .putExtra("idVideoCall", new Random().nextLong())
                            .putExtra("jwt", "jwt")
                            .putExtra("url", "https://api.telnyx.com/v2/")
                            .putExtra("oldIdentity", new Random().toString())
                    );

                    return;
       /*             room = new Room(MainActivity.this, roomId,refreshToken, new ExternalData(randomInt(7), participantName), false);
                    joinRomCard.setVisibility(View.GONE);
                    room.getStateObservable().observe(MainActivity.this, state -> {

                    });
                    room.getJoinedRoomObservable().observe(MainActivity.this, event -> {
                        if (event.getContentIfNotHandled().getJoined()) {
                            getObservers();
                            startRefreshTokenJob();
                        }
                    });
                    room.connect();*/

                } else {
                    System.out.println("Request Error :: " + response.errorBody());
                    showProgress(false);
                }
            }

            @Override
            public void onFailure(Call<DataResponse<GetTokenInfo>> call, Throwable t) {
                System.out.println("Request Error :: " + t.getMessage());
                showProgress(false);
            }
        });
    }


    private  Handler mHandler;

    private  void startRefreshTokenJob() {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("RoomFragment", "Starting refresh");

            }
        }, refreshTokenExpiresAt);
    }

    public void stopRefreshTokenJob() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        selfTileSurface.release();
    }

    private void stopCameraCapture() {
        if (publishConfigHelper == null) return;
        publishConfigHelper.stopCapture();
        room.updateStream(publishConfigHelper);
        publishConfigHelper.releaseSurfaceView(selfTileSurface);
    }

    private void stopAudioCapture() {
        if (publishConfigHelper == null) return;
        publishConfigHelper.disposeAudio();
        room.updateStream(publishConfigHelper);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRefreshTokenJob();
    }
}
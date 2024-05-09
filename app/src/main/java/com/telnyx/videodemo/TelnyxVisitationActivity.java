package com.telnyx.videodemo;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.telnyx.video.sdk.*;
import com.telnyx.video.sdk.utilities.CameraDirection;
import com.telnyx.video.sdk.utilities.PublishConfigHelper;
import com.telnyx.video.sdk.webSocket.model.send.ExternalData;
import com.telnyx.video.sdk.webSocket.model.ui.Participant;
import com.telnyx.video.sdk.webSocket.model.ui.ParticipantStream;
import com.telnyx.video.sdk.webSocket.model.ui.StreamConfig;
import com.telnyx.video.sdk.webSocket.model.ui.StreamStatus;
import com.telnyx.videodemo.R;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;



public class TelnyxVisitationActivity extends AppCompatActivity {

    Room room;
    private Participant selfParticipant;
    private Participant remoteParticipant;
    private SurfaceViewRenderer remoteParticipantTileSurface;
    private SurfaceViewRenderer selfTileSurface;

    private String LocalStreamKey = "self";
    private String SELF_STREAM_ID = UUID.randomUUID().toString();
    private String VIDEO_TRACK_KEY = "000";
    private String AUDIO_TRACK_KEY = "001";

    private String jwt;
    private String url;
    private String oldIdentity;

    boolean cameraStarted = false;
    long idVideoCall = 0;
    private PublishConfigHelper publishConfigHelper;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_telnyx);

        if (getSupportActionBar() != null)
            this.getSupportActionBar().hide();

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        runOnUiThread(() -> getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));

        selfTileSurface = findViewById(R.id.participant_tile_surface);

        Bundle b = getIntent().getExtras();
        String roomName = b.getString("roomName");
        String token = b.getString("token");
        idVideoCall = b.getLong("idVideoCall");
        jwt = b.getString("jwt");
        url = b.getString("url");
        oldIdentity = b.getString("oldIdentity");
        UUID parsed = UUID.fromString(roomName);

        LocalStreamKey = token;
        room = new Room(getBaseContext(), parsed, token, new ExternalData(randomInt(7), token), false);
        room.getJoinedRoomObservable().observe(TelnyxVisitationActivity.this, event -> {
            if (event.getContentIfNotHandled().getJoined()) {
                setup();
            }
        });

        FloatingActionButton endCallButton = findViewById(R.id.btn_end_call);
        endCallButton.setOnClickListener(view -> {
            runOnUiThread(() -> getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
            room.disconnect();
            finish();
        });
        room.connect();
    }

    private void setup() {
        getObservers();
    }

    public void updateVideoIdentity(long idVideoCall, String oldIdentity, String updatedIdentity, String baseURL) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = HttpUrl.parse(baseURL).newBuilder()
                .addPathSegment("video")
                .addPathSegment(String.valueOf(idVideoCall))
                .addPathSegment("update-identity")
                .addQueryParameter("old", oldIdentity)
                .addQueryParameter("new", updatedIdentity)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", String.format("Bearer %s", jwt))
                .patch(RequestBody.create("", MediaType.get("application/json"))) // Empty body for PATCH request
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace(); // Handle the error
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }

    private void setupSelfParticipant(Participant participant) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            setupCamera();
        }).start();
        ParticipantStream stream = null;
        for (ParticipantStream s : participant.getStreams()) {
            if (LocalStreamKey.equals(s.getStreamKey())) {
                stream = s;
                break;
            }
        }

        if (stream == null) {
            return;
        }

        if (StreamStatus.ENABLED.equals(stream.getVideoEnabled())) {
            room.addSubscription(participant.getParticipantId(), LocalStreamKey, new StreamConfig(true, true));
            room.setParticipantSurface(participant.getParticipantId(), selfTileSurface, LocalStreamKey);

            VideoTrack videoTrack = stream.getVideoTrack();
            videoTrack.addSink(selfTileSurface);
            videoTrack.setEnabled(true);
        } else {
            room.removeSubscription(participant.getParticipantId(), LocalStreamKey);
        }

        if (selfParticipant == null) return;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void streamParticipant(Participant participant) {
        ParticipantStream stream = null;

        for (ParticipantStream s : participant.getStreams()) {
            if (LocalStreamKey.equals(s.getStreamKey())) {
                stream = s;
                break;
            }
        }

        if (stream != null && StreamStatus.ENABLED.equals(stream.getVideoEnabled())) {
            room.addSubscription(participant.getParticipantId(), LocalStreamKey, new StreamConfig(true, true));
            room.setParticipantSurface(participant.getParticipantId(), selfTileSurface, LocalStreamKey);

            VideoTrack videoTrack = stream.getVideoTrack();
            if (videoTrack == null) {
                return;
            }
            videoTrack.addSink(selfTileSurface);
            videoTrack.setEnabled(true);
        } else {
            room.removeSubscription(participant.getParticipantId(), LocalStreamKey);
        }
    }

    private void setupCamera() {
        if (!cameraStarted) {
            startCameraCapture();
            handler.postDelayed(this::startAudioCapture, 2000);
        } else {
            stopCameraCapture();
        }
        cameraStarted = !cameraStarted;
    }

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

        if (shouldPublish) {
            publishConfigHelper.setSurfaceView(selfTileSurface);
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

        selfTileSurface.setMirror(true);
    }

    private void startAudioCapture() {
        boolean shouldPublish = publishConfigHelper == null;
        if (shouldPublish) {
            publishConfigHelper = new PublishConfigHelper(
                    TelnyxVisitationActivity.this,
                    CameraDirection.FRONT,
                    LocalStreamKey,
                    SELF_STREAM_ID
            );
        }
        if (shouldPublish) {
            publishConfigHelper.createAudioTrack(true, AUDIO_TRACK_KEY);
        }
        if (shouldPublish) {
            room.addStream(publishConfigHelper);
        } else {
            room.updateStream(publishConfigHelper);
        }
    }

    private void stopCameraCapture() {
        if (publishConfigHelper == null) return;
        publishConfigHelper.stopCapture();
        room.updateStream(publishConfigHelper);
        publishConfigHelper.releaseSurfaceView(selfTileSurface);
    }

    private void getObservers() {
        if (room != null) {
            room.getJoinedParticipant().observe(this, participantEvent -> {
                Participant participant = participantEvent.getContentIfNotHandled();
                if (selfParticipant != null && participant.getParticipantId().equals(selfParticipant.getParticipantId())) {
                    selfParticipant = participant;
                    updateVideoIdentity(idVideoCall, oldIdentity, participant.getParticipantId(), url);
                }

                if (remoteParticipant != null && participant.getParticipantId().equals(remoteParticipant.getParticipantId())) {
                    remoteParticipant = participant;
                }

                setupRemoteParticipant(participant);
            });

            room.getParticipantsObservable().observe(this, participants -> {
                for (Participant participant : participants) {
                    if (participant.isSelf()) {
                        selfParticipant = participant;
                        updateVideoIdentity(idVideoCall, oldIdentity, participant.getParticipantId(), url);
                    } else {
                        remoteParticipant = participant;
                    }

                    if (participant.isSelf()) {
                        setupSelfParticipant(participant);
                    } else {
                        setupRemoteParticipant(participant);
                    }
                }
            });

            room.getParticipantStreamChanged().observe(this, participantStreamEvent -> {
                Participant participant = participantStreamEvent.getContentIfNotHandled();
                if (participant == null) {
                    Timber.tag("RoomFragment").e("getParticipantStreamChanged() %s", participantStreamEvent);
                    return;
                }

                if (participant.getParticipantId().equals(selfParticipant.getParticipantId())) {
                    selfParticipant = participant;
                    streamParticipant(selfParticipant);
                } else {
                    remoteParticipant = participant;
                    setupRemoteParticipant(remoteParticipant);
                }
            });
        }
    }

    private void setupRemoteParticipant(Participant participant) {
        remoteParticipantTileSurface = findViewById(R.id.remote_participant_tile_surface);

        ParticipantStream stream = null;
        for (ParticipantStream s : participant.getStreams()) {
            String sk = s.getStreamKey();
            if (!LocalStreamKey.equals(sk)) {
                stream = s;
                break;
            }
        }

        if (stream == null) {
            return;
        }

        StreamStatus status = stream.getVideoEnabled();
        String key = stream.getStreamKey();
        if (StreamStatus.ENABLED.equals(status)) {
            room.addSubscription(participant.getParticipantId(), key, new StreamConfig(true, true));
            room.setParticipantSurface(participant.getParticipantId(), remoteParticipantTileSurface, key);

            VideoTrack videoTrack = stream.getVideoTrack();
            videoTrack.addSink(remoteParticipantTileSurface);
            videoTrack.setEnabled(true);
        }
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
}

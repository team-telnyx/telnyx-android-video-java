package com.telnyx.videodemo;


import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.telnyx.video.sdk.Room;
import com.telnyx.video.sdk.webSocket.model.send.ExternalData;
import com.telnyx.videodemo.models.CreateRoomRequest;
import com.telnyx.videodemo.models.CreateRoomResponse;
import com.telnyx.videodemo.models.DataResponse;
import com.telnyx.videodemo.models.OfflineRoom;
import com.telnyx.videodemo.models.createToken.CreateTokenRequest;
import com.telnyx.videodemo.models.createToken.GetTokenInfo;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

interface OnResponseListener<T> {
    void onResponse(T response);
}

public class JoinRoomDialogFragment extends BottomSheetDialogFragment {

    MainActivity mainActivity;

    public JoinRoomDialogFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    Button buttonJoin;
    EditText roomNameEditText;
    LinearLayout joinRoomLayout;
    RelativeLayout createRoomLayout;

    EditText participantNameEditText;


    EditText roomUuidEditText;

    SwitchMaterial joinOrCreateSwitch;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.join_room_dialog, container, false);
        buttonJoin = (Button) view.findViewById(R.id.buttonJoinRoom);
        roomNameEditText = view.findViewById(R.id.room_name_et);
        roomUuidEditText = view.findViewById(R.id.room_uuid_et);
        participantNameEditText = view.findViewById(R.id.participant_name_ti_et);
        joinOrCreateSwitch = view.findViewById(R.id.joinOrCreateSwitch);
        joinRoomLayout = view.findViewById(R.id.join_room_layout);
        createRoomLayout = view.findViewById(R.id.create_room_rl);
        roomNameEditText.setText("roomDemo1");
        String deviceName = android.os.Build.MODEL;
        participantNameEditText.setText("Android User" + deviceName);
        roomUuidEditText.setText("382adb91-aa28-4815-a9a7-48fd227466f3");
        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            dialog.setDismissWithAnimation(false);
        }

        buttonJoin = view.findViewById(R.id.buttonJoinRoom);
        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle join room
                if (joinOrCreateSwitch.isChecked()) {
                    if (roomUuidEditText.getText().toString().isEmpty()) {
                        roomUuidEditText.setError("Room UUID is required");
                        return;
                    }

                    UUID roomUuid = UUID.fromString(roomUuidEditText.getText().toString());
                    String participantName = participantNameEditText.getText().toString();
                    mainActivity.sharedPref.saveOfflineRoom(new OfflineRoom(roomUuid.toString(), participantName));
                    mainActivity.createToken(roomUuid, participantName);
                    mainActivity.initJoined();
                    dismiss();
                    return;
                }


                // Handle create room
                if (roomNameEditText.getText().toString().isEmpty()) {
                    roomNameEditText.setError("Room name is required");
                    return;
                }

                if (participantNameEditText.getText().toString().isEmpty()) {
                    participantNameEditText.setError("Participant name is required");
                    return;
                }

                String roomName = roomNameEditText.getText().toString();
                CreateRoomRequest roomRequest = new CreateRoomRequest(roomName, 10);
                createRoom(roomRequest, new OnResponseListener<CreateRoomResponse>() {
                    @Override
                    public void onResponse(CreateRoomResponse response) {


                        UUID roomUuid = UUID.fromString(response.getRoomDetails().getId());
                        String participantName = participantNameEditText.getText().toString();
                        mainActivity.sharedPref.saveOfflineRoom(new OfflineRoom(roomUuid.toString(), participantName));
                        mainActivity.createToken(roomUuid, participantName);
                        mainActivity.initJoined();
                    }
                });
            }
        });

        joinOrCreateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                joinRoomLayout.setVisibility(View.VISIBLE);
                createRoomLayout.setVisibility(View.INVISIBLE);
                buttonJoin.setText("Join Room  ");
            } else {
                joinRoomLayout.setVisibility(View.INVISIBLE);
                createRoomLayout.setVisibility(View.VISIBLE);
                buttonJoin.setText("Create Room   ");
            }
        });

    }




    private void createRoom(CreateRoomRequest roomRequest, OnResponseListener<CreateRoomResponse> onResponseListener) {
        ApiService apiService = mainActivity.getApiService();
        Call<CreateRoomResponse> call = apiService.createRoom(roomRequest);
        mainActivity.showProgress(true);
        call.enqueue(new Callback<CreateRoomResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateRoomResponse> call, @NonNull Response<CreateRoomResponse> response) {
                if (response.isSuccessful()) {
                    onResponseListener.onResponse(response.body());
                    System.out.println("Response: " + response.body());
                    Toast.makeText(mainActivity, "Room created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("Request Error :: " + response.errorBody());
                    Toast.makeText(mainActivity, "Failed to create room", Toast.LENGTH_SHORT).show();
                }
                mainActivity.showProgress(false);
            }

            @Override
            public void onFailure(Call<CreateRoomResponse> call, Throwable t) {
                System.out.println("Request Error :: " + t.getMessage());
                Toast.makeText(mainActivity, "Failed to create room", Toast.LENGTH_SHORT).show();
                mainActivity.showProgress(false);

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

        behavior.setPeekHeight(0);
        dialog.setCanceledOnTouchOutside(false);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    void setRefreshData(String refreshToken, int refreshTokenExpiresAt) {
        MainActivity.refreshToken = refreshToken;
        MainActivity.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

}

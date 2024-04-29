# Telnyx Android Video SDK

This guide will walk you through the process of using the Telnyx Android Video SDK to create a video chat application. The flow includes creating a room, getting a token, refreshing the token, connecting to the room, and managing the stream flow.

## API Flow

1. **Create Room**: The first step is to create a room where the video chat will take place. This is done by making a request to the `createRoom` API endpoint.

2. **Get Token**: After creating a room, you need to get a token. This token is used for authentication and authorization when connecting to the room. You can get a token by making a request to the `getToken` API endpoint.

3. **Refresh Token**: Tokens have an expiry time, so you need to refresh your token at regular intervals. You can do this by making a request to the `refreshToken` API endpoint.


## Stream Flow

1. **Add Participant to Room**: After connecting to the room, you can add participants to the room. This is done by creating a `Participant` object and adding it to the room.

2. **Subscribe Participant**: After adding a participant to the room, you need to subscribe to the participant's stream. 




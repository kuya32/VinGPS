package com.luckycharms.vingps.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Car;
import com.amplifyframework.datastore.generated.model.Client;
import com.amplifyframework.datastore.generated.model.User;
import com.luckycharms.vingps.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Handler handleCheckedLoggedIn;
    public static  String clientId;
    public static Client currentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handleCheckedLoggedIn = new Handler(Looper.getMainLooper(), message -> {
            if (message.arg1 == 0) {
                Log.i("Amplify.login", "Handler: They are not logged in");
            } else if (message.arg1 == 1) {
                Log.i("Amplify.login", "Handler: They were logged in");
                startActivity(new Intent(MainActivity.this, FeedActivity.class));
            } else {
                Log.i("Amplify.login", "Send true or false");
            }
            return false;
        });

        configureAws();

//        mockUsers();
//        verifyMockUsers();
        addLoginListener();
        getIsSignedIn();
        addMocks();
        addClientMocks();
        getClient(clientId);


    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        setContentView(R.layout.activity_main);
//
//    }

    public void configureAws() {
        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
            Log.i("MainActivityAmplify", "Initialized Amplify");
        } catch (AmplifyException e) {
            Log.e("MainActivityAmplify", "Could not initialize Amplify", e);
        }
    }

    public void getIsSignedIn() {
        Amplify.Auth.fetchAuthSession(
                result -> {
                    Log.i("Amplify.login", result.toString());
                    Message message = new Message();

                    if (result.isSignedIn()) {
                        message.arg1 = 1;
                    } else {
                        message.arg1 = 0;
                    }
                    handleCheckedLoggedIn.sendMessage(message);
                },
                error -> Log.e("Amplify.login", error.toString())
        );
    }

    public void addLoginListener() {
        ((Button) findViewById(R.id.userLoginButton)).setOnClickListener(view -> {
            String username = ((TextView) findViewById(R.id.emailLoginInput)).getText().toString();
            String password = ((TextView) findViewById(R.id.passwordLoginInput)).getText().toString();

            Amplify.Auth.signIn(
                    username,
                    password,
                    result -> {
                        Log.i("Amplify.login", result.isSignInComplete() ? "Login succeeded" : "Login not complete");
                        startActivity(new Intent(MainActivity.this, FeedActivity.class));
                    },
                    error -> Log.e("Amplify.login", error.toString())
            );
        });
    }

    public void mockUsers() {
        Amplify.Auth.signUp(
                "kamit.satkeev@gmail.com",
                "123456789",
                AuthSignUpOptions.builder().userAttribute(AuthUserAttributeKey.email(), "kamit.satkeev@gmail.com").build(),
                result -> {
                    Log.i("Amplify.signup", result.toString());
                },
                error -> Log.e("Amplify.signup", error.toString())
        );
    }

    public void verifyMockUsers() {
        Amplify.Auth.confirmSignUp(
                "kamit.satkeev@gmail.com",
                "777000",
                result -> {
                    Log.i("Amplify.confirm", result.toString());
                },
                error -> Log.e("Amplify.confirm", error.toString())
        );

    }

    public static void addMocks() {
        Client client = Client.builder().firstName("Ted")
                .lastName("Talks")
                .phone("206-234-6231")
                .email("thisplace@gmail.com")
                .license("temp")
                .licenseImageUrl("temp")
                .build();
        Amplify.API.mutate(
                ModelMutation.create(Car.builder()
                        .make("Ford")
                        .model("Focus")
                        .color("Red")
                        .price("$14,000")
                        .vin("AFGERGAEDFG235WEF23F21")
                        .lat("43.126323")
                        .lon("-122.456126")
                        .status(false)
                        .imageUrl("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.motortrend.com%2Fcars%2Fford%2Ffocus%2F&psig=AOvVaw1q4jRkkNhTzSyZXDEg6a-s&ust=1605738298957000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCLDMqbrPiu0CFQAAAAAdAAAAABAD")
                        .lastUserCheckedOut("Bill")
                        .client(client)
                        .build()),
                success -> Log.i("Amplify", "Car added"),
                error -> Log.e("Amplify", error.toString())
        );
    }

    public static void addClientMocks() {

        Client clnt = Client.builder()
                .firstName("Ted")
                .lastName("Talks")
                .phone("206-234-6231")
                .lastSalesPerson("Kamit")
                .email("thisplace@gmail.com")
                .license("temp")
                .licenseImageUrl("temp")
                .build();

        Amplify.API.mutate(

                ModelMutation.create(clnt),

                success -> {
                    Log.i("Amplify", success.getData().getId());
                    clientId = success.getData().getId();

                },
                error -> Log.e("Amplify", error.toString())
        );

    }

    public static void getClient(String id) {
//        we need id from dynamo db or change the query to all
        Amplify.API.query(
                ModelQuery.get(Client.class, id),
                response -> {
//                    Log.i("MyAmplifyApp", ((Client) response.getData()).toString());
                    Log.i("MyAmplifyApp",  response.toString());
                    currentClient = response.getData();
                    Car car = Car.builder()
                            .make("Ford")
                            .model("Focus")
                            .color("Red")
                            .price("$14,000")
                            .vin("AFGERGAEDFG235WEF23F21")
                            .lat("43.126323")
                            .lon("-122.456126")
                            .status(false)
                            .imageUrl("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.motortrend.com%2Fcars%2Fford%2Ffocus%2F&psig=AOvVaw1q4jRkkNhTzSyZXDEg6a-s&ust=1605738298957000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCLDMqbrPiu0CFQAAAAAdAAAAABAD")
                            .lastUserCheckedOut("Bill")
                            .build();
//
//                    currentClient.getCars().add(car);
                },
                error -> Log.e("MyAmplifyApp", error.toString(), error)
        );
    }

          }




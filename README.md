# RxLocation

An Android library to get location updates with a nice and clean [RxJava](https://github.com/ReactiveX/RxJava) API.

RxLocation lets you enjoy location updates while relieving the hassles of handling Android M runtime permissions and Google Play Services connection management.

## Features
 - Transparent management of GoogleApiClient: Connection to GoogleApiClient is automatically opened and closed when needed.
 - Location permission request (Android M+): Shall the app not have been granted location permission, the permission request dialog will pop-up automatically.
 - RxJava2 API: Get location updates using io.reactivex.Observable and compose it with all Rx's operators.
 - Convenient error handling: In case the user doesn't grant location permission an error condition will be issued via rxJava's onError() API.
 - Google Play Services connection failure handling: When encountering recoverable GoogleApiClient connection failures, UI with the proper resolution steps will automatically pop up. 

## Installation

Add this to your module's build.gradle:

```groovy
dependencies {
    compile 'net.kjulio.RxLocation:rxlocation:0.11.0-beta'
}
```

## Usage

```java
public class MainActivity extends AppCompatActivity {

    private final LocationRequest defaultLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            
    private TextView textView;
    private Button button;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick();
            }
        });
    }

    private void onButtonClick() {
        RxLocation.locationUpdates(this, defaultLocationRequest)
                        .firstElement()
                        .subscribe(new Consumer<Location>() {
                            @Override
                            public void accept(Location location) throws Exception {
                                textView.setText(location.toString());
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                textView.setText(throwable.getLocalizedMessage());
                            }
                        });
    }
}
```

## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

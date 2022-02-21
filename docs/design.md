# Design

In this section we explain the main decisions made during the application design.

#### Splash Screen: 
we have decided to create a welcome screen when the user accesses the application to compensate for the waiting time until the app is ready. If we did not set duration, the splash screen did not stay long enough, so we implemented a Handler to establish duration of 2 seconds (Figure a).

##### Text To Speech: 
we have implemented the Text To Speech function to convert the text into voice. We have decided to allow this functionality using the proximity sensor, and thus it allows a blind person to activate it. However, we have thought that it would be better to add a switch button to deactivate the TTS in case the person does not need it.

#### Speech To Text: 
we have determined to add this function in order to introduce the nickname, so a blind person does not need to write it. This function is activated when the proximity sensor detects something near, and a microphone icon appears. The STT starts to work when the user clicks on the microphone or when he/she presses the volume down button (Figure c).

#### Choose the meeting location: 
This functionality is developed in CreateMeetActivity. Two main design decision were taken :
+ While downloading the web content to show in the list using a background thread (class LoadWebContent), an object of the class ProgressDialog is implemented in order to make the app more responsive (Figure d).
+ For showing the list of theatres we select RecyclerView class because it allows us to show the titles of the places while keeping the information of the object (Place) in an easy way (Figure e).

#### Confirm the meeting location: 
ConfirmMeetActivity is the class that deals with this functionality. As it was stated before, it contains basically a map fragment to show the location of the meeting point. To develop this class the following decisions were made:
+ Implements a FragmentContainerView where mapFragments are added. With this design decision, we embedded the fragment in the activity.
+ In the MapsFragment class, the latitude and longitude of the place are represented. These coordinates are received through an Intent from the previous activity and then set in the setupMapFragment method (Figure f).

#### Meeting Screen: 
MeetingActivity is the most important class in the app because it groups a high number of elements (Figure i). The main decisions taken are:

+ Each event has an unique identifier through UUID, preventing other users from accessing the same meeting. Below this ID, location of the meeting point and distance from the user to the location are added, so the topics are ID/location, where the coordinates are notified, and ID/distance, where the distance of each user is published. 
+ Due to the complexity of the MeetingActivity, a MeetingActivityViewModel was implemented. The purpose of using the ViewModel is to wrap multiple methods so it can easily be consumed by view, especially those related with the MQTT messages.
+ PahoClient and PahoClientListener. These two classes build the MQTT scenario. We decided to create them to create clients from the ViewModel and manage the message flow from there.
+ Request current location. The function getLastKnownLocation in MeetingActivityViewModel is in charge of getting the current location changes and through its callbacks(onLocationChanged) calculate the distance to the meeting point and publish it in the MQTT topic (publishCurrentDistanceToMeetingPointFrom). 
Another decision made is the fact that getLastKnownLocation checks two different Location providers in order to make the app more robust.

#### Landscape orientation. 
In order to make this activity survive orientation changes, a landscape layout was designed (Figure j).

#### Copy to clipboard function
It has been implemented in this screen in order to copy the event ID and paste it so it is easier to send the ID to the rest of the participants so that they join the meeting. This function can be done by clicking on the top right blue button.

![screens](https://github.com/mariasanzs/HowFar/blob/main/docs/img/screens.png)



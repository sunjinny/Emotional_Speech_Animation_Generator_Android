# Emotional Facial Animation Client (for Android)
This is a part of Emotional Facial Animation module for Flagship project by [Visual Media Lab, KAIST](http://vml.kaist.ac.kr).
It provides a fragment view for animating avatar, which can be used in other activities.

## Authors
 - Sunjin Jung (<sunjin225@kaist.ac.kr>)
 - Base codes written by Roger Blanco i Ribera


## Prerequisites
 - Android SDK v26 (MinSDK v15)
 - OpenGL ES 2.0
 - Latest Android Build Tools
 - Android Support Repository 


## Getting started

### Building the Docker image
1. Clone this repo first. 
2. Include the `.jar` file in your Android Studio project.
3. Make a new fragment(Containers) in the origin layout `.xml` file and set the name as a path of `AvatarFragment`.
4. In your MainActivity, follow the steps below.
   We provide `MainActivity.java` for testing and showing how to use in the activity.  
- Get the fragment manager for the AvatarFragment.  
  ```
  AvatarFragment fragment = (AvatarFragment) getFragmentManager().findFragmentById()
  ```
- Create `AvatarAnimation` instance with the fragment.  
  ```
  AvatarAnimation avatarAnimation = new AvatarAnimation(fragment);
  ```
- If a new animation data `.xml` is given by the server, set the animation by `InputStream` data.  
  ```
  avatarAnimation.setAnimation()
  ```
- Play a new audio corresponding to the animation data.  
- Update the animation by getting the current audio timing.  
  ```
  avatarAnimation.updateAnimation(MediaPlayer.getCurrentPosition())
  ```
- Finally, when the audio ends, play idle motion.  
  ```
  avatarAnimation.playIdleMotion();
  ```

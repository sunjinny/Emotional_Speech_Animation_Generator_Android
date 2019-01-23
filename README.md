# Emotional Facial Animation Client (for Android)
This is a part of Emotional Facial Animation module for Flagship project by [Visual Media Lab, KAIST](http://vml.kaist.ac.kr).  
It provides a `vml-animation.aar` file for animating avatar, which can be used with a fragment view of other projects.

## Authors
 - Sunjin Jung (<sunjin225@kaist.ac.kr>)
 - Base codes written by Roger Blanco i Ribera


## Tested Environment
 - Android SDK v26 (MinSDK v15)
 - Android SDK Platform-Tools v28.0.1
 - Android SDK tools v26.1.1
 - Android AppCompat v7:26.1.0
 - OpenGL ES 2.0


## Getting started
- Clone this repository.  
- Copy the `vml-animation.aar` file into the `app/libs` folder in your Android project.  
- In your `build.gradle` file, add following codes.

```
dependencies {
    implementation(name: 'vml-animation', ext: 'aar')
}  

repositories {
    flatDir {
        dirs 'libs'
    }
}
```
- Sync the project.


## How to use the library
- Create a new fragment(Containers) in your `.xml` layout and connect it with `vml.com.animation.AvatarFragment`.
- In Activity, follow these steps.  
   (We provide `MainActivity.java` for testing and showing how to use in the activity.)  
- Get the fragment manager of the AvatarFragment.  

  ```
  AvatarFragment fragment = (AvatarFragment) getFragmentManager().findFragmentById(..);
  ```
  
- Create `AvatarAnimation` instance with the fragment.  

  ```
  AvatarAnimation avatarAnimation = new AvatarAnimation(fragment);
  ```
  
- If a new animation data `.xml` is given from the server, set the animation.  

  ```
  avatarAnimation.setAnimation(..);
  ```
  
- Play a new audio corresponding to the animation.  

- While playing the audio, update the animation by getting the current audio timing.  

  ```
  avatarAnimation.updateAnimation(MediaPlayer.getCurrentPosition());
  ```
  
- Finally, when the audio ends, play idle motion.  

  ```
  avatarAnimation.playIdleMotion();
  ```
  
  
## How to run the example codes
- In `app/build.gradle` file, follow the steps.
- Change `apply plugin: 'com.android.library'` to `apply plugin: 'com.android.application'`.
- Delete the comment for `applicationId "vml.com.animation"`.
- Delete the following codes.

```
libraryVariants.all { variant ->
    variant.outputs.all { output ->
        if (outputFile != null && outputFileName.endsWith('.aar')) {
            outputFileName = "vml-animation.aar"
        }
    }
}
```

- Sync the project and run 'app'.

# Emotional Facial Animation Client (for Android)
This is a part of Emotional Facial Animation module for Flagship project by [Visual Media Lab, KAIST](http://vml.kaist.ac.kr).  
It provides a `vml-animation.aar` file for animating avatar, which can be used with a fragment view of other projects.

## Authors
 - Sunjin Jung (<sunjin225@kaist.ac.kr>)
 - Base codes written by Roger Blanco i Ribera


## Prerequisites
 - Android SDK v26 (MinSDK v15)
 - OpenGL ES 2.0
 - Latest Android Build Tools
 - Android Support Repository 


## Getting started
1. Clone this repository.  
2. Include the `.aar` file in your android project.
- Copy the `vml-animation.aar` file to the `app/libs` folder in the Project.  
- In `build.gradle` file, add following codes.  

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


## Usage
1. Create a new fragment(Containers) in the `.xml` layout and connect it with `vml.com.animation.AvatarFragment`.
2. In Activity, follow these steps.  
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
  

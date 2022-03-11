# LifeRec
---
LifeRec is an Android (7.0+) application which will allow researchers to record multi-modal lifelog data of participants and perform personalized recommendations by communicating with the remote server. The App helps users collect various lifelog information (e.g., location, diet, activity, and mood) and receive
real-time recommendation with privacy protection concerns and little effort. 


## Installation

Please install the apk at LifeRec/app/release/lifelog_recorder.apk.

## Features

Once installed, LifeRec will start to record the following information at local devices automatically:

|   Information   |   Detail   |   Code   |
|   :----   |   :----   |   :----   |
|   GPS location   |   Once per 15 minutes   |   GpsAlarmReceiver.java, GpsServer.java   |
|   Weather   |   Once per 15 minutes, required from an API https://dev.qweather.com/docs/api/   |   WeatherAPI.java   |

Users are allowed to record the following logs:

|   Log   |   Detail   |   Code   |
|   :----   |   :----   |   :----   |
|   Current mood by the two-dimensional Thayer mood model   |   The users are notified to report their moods at the time they have chosen in the configuration. Ongoing activities and environmental context are also recorded. If there are important events, they will be recorded along with the above informations. (We recommand restarting the app before you record your mood.)   |   MoodRequest.java, MoodActivity.java, MusicActivity.java, ContextActivity.java   |
|   Previous emotion aroused by accidencts   |   Important events, time of event, mood and environmental context are recorded. (We recommand restarting the app before you record your mood.)   |   MoodSubmit.java, MoodActivity.java, MusicActivity.java, ContextActivity.java   |
|   Daily Activity in timeline   |   At the end of the day, the user can recall the whole schedule and annotate daily activities in time order. (The time representation is wierd, but it won't affect the performance of LifeRec.)   |   Trace.java, TraceListAdapter.java, TraceActivity.java, MoodActivity.java, RecordDialog.java   |
|   Diets with smartphone camera   |   Users can record their diets by taking pictures.   |   openCamera() in MainActivity.java   |

At present, item recommendation module will show up when users finish recording their moods. We collect users' feedbacks to evaluate our models. We will reorgnalize the dependency among modules to help researchers customize the App more flexibly.

The following featuers are still under development:

- Language pack
- Plug-and-play port to connetct with multi-modal portable sensors
- Local personalized optimization for data processing and recommendation

If you wish to know more details about the motivation, usage, and user feedbak of LifeRec, please refer to our paper at https://doi.org/10.1145/3498366.3505837

## Codes

The relationships between the models are shown below
[![structure.png](https://i.postimg.cc/J493HB5p/structure.png)](https://postimg.cc/RNLJjh8t)

## Contributors
Jiayu Li (JiayuLi-997), Yumeng Cui (Cuiym715), Peixuan Han (Hak_Han)

## Citation

If you find the LifeRec App useful, please cite our work at CHIIR2022.
```
@inproceddings{
  Coming soon.
}
```

## Contact with us

If you have any problems, please feel free to open an issue on our issue tracker, or contact with me at jy-li20@mails.tsinghua.edu.cn.

Moreover, we are continuing to develop the LifeRec, so if you have new ideas when using it, please do let us know ASAP.
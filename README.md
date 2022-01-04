# LifeRec
---
LifeRec is an Android (7.0+) application which will allow researchers to record multi-modal lifelog data of participants and perform personalized recommendations by communicating with the remote server. The App helps users collect various lifelog information (e.g., location, diet, activity, and mood) and receive
real-time recommendation with privacy protection concerns and little effort. 


## Installation

Please install the apk at LifeRec/app/release/lifelog_recorder.apk.

## Features

Once installed, LifeRec will start to record the following information at local devices automatically:

- GPS location (once per 15 minutes)
- Weather (once per 15 minutes, required from an API https://dev.qweather.com/docs/api/)

Users are allowed to record the following logs:

- Current mood by the two-dimensional Thayer mood model.
- Previous emotion aroused by accidencts.
- Daily Activity in timeline.
- Diets with smartphone camera.

At present, item recommendation module will show up when users finish recording their moods. We will reorgnalize the dependency among modules to help researchers customize the App more flexibly.

If you wish to know more details about the motivation, usage, and user feedbak of LifeRec, please refer to our paper at https://doi.org/10.1145/3498366.3505837

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

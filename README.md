# CustomVideo
一个自定义相机录制压缩的demo 如有问题和改进可以发邮件到437220638@qq.com

# Screenshots
![image](/screenshots/photo1.png) ![image](/screenshots/photo2.png) ![image](/screenshots/photo3.png) ![image](/screenshots/photo4.png)

# FFmpeg
 关于本地视频压缩，可以使用ffmpeg,以下指令是目前我尝试的压得最快的写法：</br>
 "ffmpeg -y -i /mnt/sdcard/demo1.mp4 -strict -2 -vcodec libx264 -preset ultrafast -crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 360x640 -aspect 16:9 /mnt/sdcard/democompress.mp4"</br>
## 介绍
关于画面比例和宽高根据不同视频参数做调整，这里消耗时间最多的是 -vcodec 重编码的时间，-crf 量化比例的范围为0~51，其中0为无损模式，23为缺省值，51可能是最差的。该数字越小，图像质量越好。</br>从主观上讲，18~28是一个合理的范围。-preset 预设按照编码速度降序排列为：</br>ultrafast,superfast,veryfast,faster,fast,medium,slow,slower,veryslow,placebo 经测试在魅族MX5上20几M的消耗1分钟左右，在电脑上则是秒完成。</br>现在关于视频压缩主要有2块大问题就是so库的大小导致apk可能过大,另外一个是在手机上的压缩速度并不理想.

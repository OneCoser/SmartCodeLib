package ch.smart.code.util.videocompress.mp4;

import android.media.MediaMetadataRetriever;

/**
 * Created by Administrator on 2019/3/13.
 */

public class VideoInfo {
    private long startTime;
    private long endTime;
    private int rotationValue;
    private int originalWidth;
    private int originalHeight;
    private int resultWidth;
    private int resultHeight;
    private int bitrate;
    private int framerate;
    private String originalPath;
    private String attachPath;
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public int getRotationValue() {
        return rotationValue;
    }
    
    public int getOriginalWidth() {
        return originalWidth;
    }
    
    public int getOriginalHeight() {
        return originalHeight;
    }
    
    public int getResultWidth() {
        return resultWidth;
    }
    
    public int getResultHeight() {
        return resultHeight;
    }
    
    public int getBitrate() {
        return bitrate;
    }
    
    public int getFramerate() {
        return framerate;
    }
    
    public String getOriginalPath() {
        return originalPath;
    }
    
    public String getAttachPath() {
        return attachPath;
    }
    
    public static class Builder {
        
        private long startTime;
        private long endTime;
        private int rotationValue;
        private int originalWidth;
        private int originalHeight;
        private int resultWidth;
        private int resultHeight;
        private int bitrate;
        private int framerate;
        private String originalPath;
        private String attachPath;
        
        
        public Builder setStartTime(long startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder setEndTime(long endTime) {
            this.endTime = endTime;
            return this;
        }
        
        public Builder setRotationValue(int rotationValue) {
            this.rotationValue = rotationValue;
            return this;
        }
        
        public Builder setOriginalWidth(int originalWidth) {
            this.originalWidth = originalWidth;
            return this;
        }
        
        public Builder setOriginalHeight(int originalHeight) {
            this.originalHeight = originalHeight;
            return this;
        }
        
        public Builder setResultWidth(int resultWidth) {
            this.resultWidth = resultWidth;
            return this;
        }
        
        public Builder setResultHeight(int resultHeight) {
            this.resultHeight = resultHeight;
            return this;
        }
        
        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }
        
        public Builder setFramerate(int framerate) {
            this.framerate = framerate;
            return this;
        }
        
        public Builder setOriginalPath(String originalPath) {
            this.originalPath = originalPath;
            return this;
        }
        
        
        public Builder setAttachPath(String attachPath) {
            this.attachPath = attachPath;
            return this;
        }
        
        public VideoInfo bulid() {
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.startTime = this.startTime;
            videoInfo.endTime = this.endTime;
            videoInfo.rotationValue = this.rotationValue;
            videoInfo.originalWidth = this.originalWidth;
            videoInfo.originalHeight = this.originalHeight;
            videoInfo.resultWidth = this.resultWidth;
            videoInfo.resultHeight = this.resultHeight;
            videoInfo.bitrate = this.bitrate;
            videoInfo.framerate = this.framerate;
            videoInfo.originalPath = this.originalPath;
            videoInfo.attachPath = this.attachPath;
            return videoInfo;
        }
        
    }
    
    public static final int VIDEOINFO_240P = 0;
    public static final int VIDEOINFO_360P = 1;
    public static final int VIDEOINFO_480P = 2;
    public static final int VIDEOINFO_540P = 3;
    public static final int VIDEOINFO_720P = 4;
    
    /**
     * @param path
     * @param outPath
     * @return
     */
    public static VideoInfo getVideoInfo(String path, String outPath) {
        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(path);
        int originalHeight = Integer.parseInt(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        int originalWidth = Integer.parseInt(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int selectedCompression;
        if (originalWidth > 0 && originalWidth <= 360) {
            selectedCompression = VIDEOINFO_240P;
        } else if (originalWidth > 360 && originalWidth <= 480) {
            selectedCompression = VIDEOINFO_360P;
        } else if (originalWidth > 480 && originalWidth <= 540) {
            selectedCompression = VIDEOINFO_480P;
        } else if (originalWidth > 540 && originalWidth <= 720) {
            selectedCompression = VIDEOINFO_540P;
        } else {
            selectedCompression = VIDEOINFO_720P;
        }
        
        float maxSize;
        int targetBitrate;
        switch (selectedCompression) {
            case VIDEOINFO_240P:
                maxSize = 426.0f;
                targetBitrate = 400000;
                break;
            case VIDEOINFO_360P:
                maxSize = 640.0f;
                targetBitrate = 900000;
                break;
            case VIDEOINFO_480P:
                maxSize = 854.0f;
                targetBitrate = 1100000;
                break;
            case VIDEOINFO_540P:
                maxSize = 960.0f;
                targetBitrate = 1250000;
                break;
            case VIDEOINFO_720P:
            default:
                targetBitrate = 2500000;
                maxSize = 1280.0f;
                break;
        }
        
        int rotation = Integer.parseInt(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
        int originalBitrate = Integer.parseInt(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
        float scale = originalWidth > originalHeight ? maxSize / originalWidth : maxSize / originalHeight;
        int resultWidth = Math.round(originalWidth * scale / 2) * 2;
        int resultHeight = Math.round(originalHeight * scale / 2) * 2;
        int bitrate = Math.min(targetBitrate, (int) (originalBitrate / scale));
        
        return new Builder()
            .setStartTime(-1)
            .setEndTime(-1)
            .setResultWidth(resultWidth)
            .setResultHeight(resultHeight)
            .setRotationValue(rotation)
            .setOriginalWidth(originalWidth)
            .setOriginalHeight(originalHeight)
            .setFramerate(25)
            .setBitrate(bitrate)
            .setOriginalPath(path)
            .setAttachPath(outPath).bulid();
    }
    
}

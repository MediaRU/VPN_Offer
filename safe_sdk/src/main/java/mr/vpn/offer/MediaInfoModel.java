package mr.vpn.offer;

import java.util.ArrayList;

public class MediaInfoModel {

    private String url;
    private String title;
    private String pid;
    private long size;
    private String pictureUrl;
    private String subtitlesFileUrl;
    private String subtitlesJsonUrl;
    private String subtitlesLanguage;
    private String mediaId;
    private String fileName;
    private String mediaType;
    private String subId;
    private String mode;
    private String genre;
    private String season;
    private String episode;
    private ArrayList<String> backdrops = new ArrayList<>();

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getPid() {
        return pid;
    }

    public long getSize() {
        return size;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getSubtitlesJsonUrl() {
        return subtitlesJsonUrl;
    }

    public String getSubtitlesLanguage() {
        return subtitlesLanguage;
    }

    public String getMediaId() {
        return mediaId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getSubId() {
        return subId;
    }

    public String getSubtitlesFileUrl() {
        return subtitlesFileUrl;
    }

    public String getGenre() {
        return genre;
    }

    public String getSeason() {
        return season;
    }

    public String getEpisode() {
        return episode;
    }

    private MediaInfoModel() {

    }

    String getMode() {
        return mode;
    }

    void setMode(String mode) {
        this.mode = mode;
    }

    void setPid(String pid) {
        this.pid = pid;
    }

    void setSid(String sid) {
        this.subId = sid;
    }

    public ArrayList<String> getBackdrops() {
        return backdrops;
    }

    public static class Builder {

        private final MediaInfoModel mediaInfo;

        public Builder() {
            mediaInfo = new MediaInfoModel();
        }

        public MediaInfoModel build() {
            return mediaInfo;
        }

        public Builder url(String url) {
            mediaInfo.url = url;
            return this;
        }

        public Builder title(String title) {
            mediaInfo.title = title;
            return this;
        }

        Builder pid(String pid) {
            mediaInfo.pid = pid;
            return this;
        }

        public Builder size(long size) {
            mediaInfo.size = size;
            return this;
        }

        public Builder poster(String poster) {
            mediaInfo.pictureUrl = poster;
            return this;
        }

        public Builder subtitlesFile(String subtitlesFile) {
            mediaInfo.subtitlesFileUrl = subtitlesFile;
            return this;
        }

        public Builder subtitlesJson(String subtitlesJson) {
            mediaInfo.subtitlesJsonUrl = subtitlesJson;
            return this;
        }

        public Builder subtitlesLang(String subtitlesLang) {
            mediaInfo.subtitlesLanguage = subtitlesLang;
            return this;
        }

        public Builder imdbid(String imdbid) {
            mediaInfo.mediaId = imdbid;
            return this;
        }

        public Builder fileName(String fileName) {
            mediaInfo.fileName = fileName;
            return this;
        }

        public Builder mediaType(String mediaType) {
            mediaInfo.mediaType = mediaType;
            return this;
        }

        Builder subid(String subid) {
            mediaInfo.subId = subid;
            return this;
        }

        public Builder genre(String genre) {
            mediaInfo.genre = genre;
            return this;
        }

        public Builder season(String season) {
            mediaInfo.season = season;
            return this;
        }

        public Builder episode(String episode) {
            mediaInfo.episode = episode;
            return this;
        }

        public Builder backdrops(String backdrops) {
            if (backdrops != null)
                mediaInfo.backdrops.add(backdrops);
            return this;
        }

        public Builder backdrops(ArrayList<String> backdrops) {
            if (backdrops != null)
                mediaInfo.backdrops.addAll(backdrops);
            return this;
        }

    }
}

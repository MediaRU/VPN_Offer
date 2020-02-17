package mr.vpn.offer;

import android.text.TextUtils;

import java.util.ArrayList;

class MaskParser {

    static final String MODE_WATCH = "watch";
    static final String MODE_DOWNLOAD = "download";

    private static final String URL_MASK = "%MEDIA_URL%";
    private static final String PID_MASK = "%PID%";
    private static final String SID_MASK = "%SID%";
    private static final String TITLE_MASK = "%MEDIA_TITLE%";
    private static final String SIZE_MASK = "%MEDIA_SIZE%";
    private static final String PIC_MASK = "%MEDIA_POSTER%";
    private static final String SUB_MASK = "%SUBTITLES_URL%";
    private static final String LANG_MASK = "%SUBTITLES_LANG%";
    private static final String MEDIA_ID_MASK = "%MEDIA_ID%";
    private static final String FILE_NAME_MASK = "%MEDIA_FILE_NAME%";
    private static final String MEDIA_TYPE_MASK = "%MEDIA_TYPE%";
    private static final String MODE_MASK = "%MODE%";

    private static final String TEST_MASK = "safewatch://?url=%MEDIA_URL%&pid=%PID%&subId=%SID%&tlt=%MEDIA_TITLE%&size=%MEDIA_SIZE%&pic=%MEDIA_POSTER%&sub=%SUBTITLES_URL%&subLang=%SUBTITLES_LANG%&mid=%MEDIA_ID%&fnm=%MEDIA_FILE_NAME%&mtp=%MEDIA_TYPE%&mod=%MODE%";
    private static final String BACKDROPS_SEPARATOR = ";";

    static String parseMask(String mask, MediaInfoModel info) {
        return replaceMask(mask == null ? TEST_MASK : mask, info);
    }

    private static String replaceMask(String mask, MediaInfoModel info) {
        String result = "";
        try {
            if (info != null) {
                result = replace(mask, URL_MASK, TextUtils.isEmpty(info.getUrl()) ? "" : info.getUrl());
                result = replace(result, TITLE_MASK, info.getTitle() != null ? info.getTitle() : "");
                result = replace(result, PID_MASK, info.getPid() != null ? info.getPid() : "");
                result = replace(result, SID_MASK, info.getSubId() != null ? info.getSubId() : "");
                result = replace(result, SIZE_MASK, String.valueOf(info.getSize()));
                result = replace(result, PIC_MASK, info.getPictureUrl() != null ? info.getPictureUrl() : "");
                result = replace(result, SUB_MASK, !TextUtils.isEmpty(info.getSubtitlesJsonUrl()) ? info.getSubtitlesJsonUrl() : (!TextUtils.isEmpty(info.getSubtitlesFileUrl()) ? info.getSubtitlesFileUrl() : ""));
                result = replace(result, LANG_MASK, info.getSubtitlesLanguage() != null ? info.getSubtitlesLanguage() : "");
                result = replace(result, MEDIA_ID_MASK, info.getMediaId() != null ? info.getMediaId() : "");
                result = replace(result, FILE_NAME_MASK, info.getFileName() != null ? info.getFileName() : "");
                result = replace(result, MEDIA_TYPE_MASK, info.getMediaType() != null ? info.getMediaType() : "");
                result = replace(result, MODE_MASK, info.getMode() != null ? info.getMode() : MODE_WATCH);

                result += "&gnr=" + Utils.encodeURL((info.getGenre() != null ? info.getGenre() : "")) +
                        "&sn=" + Utils.encodeURL((info.getSeason() != null ? info.getSeason() : "")) +
                        "&ep=" + Utils.encodeURL((info.getEpisode() != null ? info.getEpisode() : ""));
                result += buildBackDrops(info.getBackdrops());
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private static String buildBackDrops(ArrayList<String> backdrops) {
        StringBuilder builder = new StringBuilder("&bds=");
        if (backdrops.size() > 0)
            for (String link : backdrops)
                if (!TextUtils.isEmpty(link))
                    builder.append(link).append(BACKDROPS_SEPARATOR);

        return Utils.encodeURL(builder.toString());
    }

    private static String replace(String mask, String target, String replacement) {
        return mask.replace(target, Utils.encodeURL(replacement));
    }
}
